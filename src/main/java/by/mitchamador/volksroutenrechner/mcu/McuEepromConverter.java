package by.mitchamador.volksroutenrechner.mcu;

import java.util.List;

/**
 * Конвертирует EEPROM-сегменты (см. {@link MemorySegment}), извлечённые из Intel HEX файла
 * прошивки маршрутного компьютера, в C-исходник (const-массив __EEDATA(...)).
 * Логика выбора сегментов и форматирования - точный перенос оригинальной консольной
 * утилиты TripComputerEepromConverter, без изменений в самих правилах.
 */
public enum McuEepromConverter {

    INSTANCE;

    public enum McuType {
        AUTO, PIC16F876A, PIC16F193X, PIC18F252, ATMEGA328P
    }

    /**
     * @param fileName исходное имя загруженного файла - используется только в режиме AUTO
     *                 для того же эвристического правила, что было в оригинале ("*.eep" -> atmega)
     */
    public String convert(List<MemorySegment> segments, String requestedType, String fileName) {
        McuType type = parseMcuType(requestedType);
        if (type == null) {
            type = McuType.AUTO;
        }
        if (type == McuType.AUTO && fileName != null && fileName.toLowerCase().endsWith(".eep")) {
            type = McuType.ATMEGA328P;
        }

        StringBuilder result = new StringBuilder();
        for (MemorySegment m : segments) {
            if (matches(m, type)) {
                result.append(formatSegment(m));
            }
        }
        return result.toString();
    }

    private McuType parseMcuType(String raw) {
        if (raw == null) {
            return McuEepromConverter.McuType.AUTO;
        }
        switch (raw.toLowerCase()) {
            case "pic16f876a":
                return McuEepromConverter.McuType.PIC16F876A;
            case "pic16f193x":
                return McuEepromConverter.McuType.PIC16F193X;
            case "pic18f252":
                return McuEepromConverter.McuType.PIC18F252;
            case "atmega328p":
                return McuEepromConverter.McuType.ATMEGA328P;
            default:
                return McuEepromConverter.McuType.AUTO;
        }
    }

    private boolean matches(MemorySegment m, McuType type) {
        switch (type) {
            case PIC16F876A:
                return (m.address >> 1) == 0x2100;
            case PIC16F193X:
                return (m.address >> 1) == 0xf000;
            case PIC18F252:
                return m.address == 0xf00000;
            case ATMEGA328P:
                return true;
            case AUTO:
            default:
                return ((m.address >> 1) == 0x2100 && ((m.address + m.data.length) >> 1) <= 0x2200)
                        || ((m.address >> 1) == 0xf000 && ((m.address + m.data.length) >> 1) <= 0xf100)
                        || (m.address == 0xf00000 && (m.address + m.data.length) <= 0xf00100);
        }
    }

    private String formatSegment(MemorySegment m) {
        StringBuilder sb = new StringBuilder();
        int c = 0;
        boolean byteAddressed = (m.address == 0 || m.address == 0xF00000);
        for (int i = 0; i < (0xc8 << (byteAddressed ? 0 : 1)); i++) {
            int word;
            if ((c & 0x07) == 0) {
                sb.append("__EEDATA(");
            }
            if (i >= m.data.length) {
                i++;
                word = 0xFF;
            } else {
                if (byteAddressed) {
                    word = m.data[i] & 0x000000FF;
                } else {
                    word = ((m.data[i + 1] << 8) & 0x0000FF00) | (m.data[i] & 0x000000FF);
                    i++;
                }
            }
            sb.append((c & 0x07) == 0 ? "" : ",").append(String.format("0x%02X", word & 0x000000FF));
            if ((c & 0x07) == 0x07) {
                sb.append(")");
                switch ((byteAddressed ? i : i >> 1) & ~0x07) {
                    case 0x00:
                        sb.append(" /*config*/");
                        break;
                    case 0x10:
                        sb.append(" /*trips*/");
                        break;
                    case 0x40:
                        sb.append(" /*services*/");
                        break;
                    case 0x58:
                        sb.append("\n\n// ds18b20 serial numbers (OUT, IN, ENGINE)");
                        break;
                    case 0x60:
                        sb.append(" /*ds18b20 serial numbers*/");
                        break;
                    case 0x70:
                        sb.append("\n\n// custom lcd characters");
                        break;
                    case 0x78:
                        sb.append(" // 0x00 - kmh[0]");
                        break;
                    case 0x80:
                        sb.append(" // 0x01 - kmh[1]");
                        break;
                    case 0x88:
                        sb.append(" // 0x02 - omin[0]");
                        break;
                    case 0x90:
                        sb.append(" // 0x03 - omin[1]");
                        break;
                    case 0x98:
                        sb.append(" // 0x04 - L100[0]");
                        break;
                    case 0xA0:
                        sb.append(" // 0x05 - L100[1]");
                        break;
                    case 0xA8:
                        sb.append(" // 0x06 - l/h[0]");
                        break;
                    case 0xB0:
                        sb.append(" // 0x07 - l/h[1]");
                        sb.append("\n\n// continuous data");
                        break;
                }
                sb.append("\n");
            }
            c++;
        }
        return sb.toString();
    }
}
