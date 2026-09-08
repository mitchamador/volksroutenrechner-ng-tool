package by.mitchamador.volksroutenrechner.mcu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Минимальный парсер файлов формата Intel HEX (см. описание формата:
 * https://en.wikipedia.org/wiki/Intel_HEX). Подряд идущие data-записи (тип 00) объединяются
 * в непрерывные {@link MemorySegment} - так же, как это делала стороння com.jtstand.intelhex.IntelHex,
 * от которой отказались из-за отсутствия публикации в Maven Central.
 * <p>
 * Поддерживаются типы записей: 00 (data), 01 (EOF), 02 (extended segment address),
 * 04 (extended linear address). Остальные (03/05 - start address) не влияют на выбор
 * EEPROM-сегментов и игнорируются.
 */
public final class IntelHexParser {

    private IntelHexParser() {
    }

    public static List<MemorySegment> parse(String content) throws IntelHexFormatException {
        List<MemorySegment> segments = new ArrayList<>();

        long extendedAddress = 0;
        long currentSegmentAddress = -1;
        ByteAccumulator currentData = null;

        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        int lineNo = 0;
        try {
            while ((line = reader.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (line.charAt(0) != ':') {
                    throw new IntelHexFormatException("строка " + lineNo + ": не начинается с ':'");
                }
                if (line.length() < 11) {
                    throw new IntelHexFormatException("строка " + lineNo + ": слишком короткая");
                }

                int length = hexByte(line, lineNo, 1);
                int address = hexWord(line, lineNo, 3);
                int type = hexByte(line, lineNo, 7);

                int expectedLen = 1 + 8 + length * 2 + 2;
                if (line.length() < expectedLen) {
                    throw new IntelHexFormatException("строка " + lineNo + ": длина строки не совпадает с полем длины данных (LL)");
                }

                int checksum = length + ((address >> 8) & 0xFF) + (address & 0xFF) + type;
                byte[] data = new byte[length];
                for (int i = 0; i < length; i++) {
                    data[i] = (byte) hexByte(line, lineNo, 9 + i * 2);
                    checksum += data[i] & 0xFF;
                }
                int fileChecksum = hexByte(line, lineNo, 9 + length * 2);
                if (((checksum + fileChecksum) & 0xFF) != 0) {
                    throw new IntelHexFormatException("строка " + lineNo + ": неверная контрольная сумма");
                }

                switch (type) {
                    case 0x00: { // data
                        long effectiveAddress = extendedAddress + address;
                        if (currentData != null && effectiveAddress == currentSegmentAddress + currentData.size()) {
                            currentData.append(data);
                        } else {
                            flushSegment(segments, currentSegmentAddress, currentData);
                            currentSegmentAddress = effectiveAddress;
                            currentData = new ByteAccumulator();
                            currentData.append(data);
                        }
                        break;
                    }
                    case 0x01: // end of file
                        flushSegment(segments, currentSegmentAddress, currentData);
                        return segments;
                    case 0x02: // extended segment address
                        flushSegment(segments, currentSegmentAddress, currentData);
                        currentData = null;
                        extendedAddress = (((data[0] & 0xFFL) << 8) | (data[1] & 0xFFL)) << 4;
                        break;
                    case 0x04: // extended linear address
                        flushSegment(segments, currentSegmentAddress, currentData);
                        currentData = null;
                        extendedAddress = (((data[0] & 0xFFL) << 8) | (data[1] & 0xFFL)) << 16;
                        break;
                    default:
                        // 03/05 (start segment/linear address) - на выбор EEPROM-данных не влияют
                        break;
                }
            }
        } catch (IOException e) {
            throw new IntelHexFormatException("ошибка чтения файла: " + e.getMessage());
        }

        // файл без завершающей записи EOF (0x01) - собираем то, что успели накопить
        flushSegment(segments, currentSegmentAddress, currentData);
        return segments;
    }

    private static void flushSegment(List<MemorySegment> segments, long address, ByteAccumulator data) {
        if (data != null && data.size() > 0) {
            segments.add(new MemorySegment((int) address, data.toByteArray()));
        }
    }

    private static int hexByte(String line, int lineNo, int index) throws IntelHexFormatException {
        try {
            return Integer.parseInt(line.substring(index, index + 2), 16);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new IntelHexFormatException("строка " + lineNo + ": некорректный hex-байт на позиции " + index);
        }
    }

    private static int hexWord(String line, int lineNo, int index) throws IntelHexFormatException {
        try {
            return Integer.parseInt(line.substring(index, index + 4), 16);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new IntelHexFormatException("строка " + lineNo + ": некорректный hex-адрес на позиции " + index);
        }
    }

    /** Простой растущий буфер байт, чтобы не тянуть java.io.ByteArrayOutputStream ради двух методов. */
    private static final class ByteAccumulator {
        private byte[] buffer = new byte[64];
        private int size = 0;

        void append(byte[] data) {
            if (size + data.length > buffer.length) {
                byte[] grown = new byte[Math.max(buffer.length * 2, size + data.length)];
                System.arraycopy(buffer, 0, grown, 0, size);
                buffer = grown;
            }
            System.arraycopy(data, 0, buffer, size, data.length);
            size += data.length;
        }

        int size() {
            return size;
        }

        byte[] toByteArray() {
            byte[] result = new byte[size];
            System.arraycopy(buffer, 0, result, 0, size);
            return result;
        }
    }
}
