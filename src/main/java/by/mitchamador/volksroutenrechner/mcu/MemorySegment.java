package by.mitchamador.volksroutenrechner.mcu;

/**
 * Один непрерывный блок байт из Intel HEX файла (объединённые подряд идущие data-записи).
 * Аналог com.jtstand.intelhex.Memory из исходной утилиты, но без стороннего кода.
 */
public final class MemorySegment {

    public final int address;
    public final byte[] data;

    public MemorySegment(int address, byte[] data) {
        this.address = address;
        this.data = data;
    }
}
