package by.mitchamador.volksroutenrechner.journal.object;

public abstract class JournalItem {
    protected final static int ITEM_V1 = 0xA5;
    protected final static int ITEM_V2 = 0xAA;

    protected int status;
    protected final Time time;

    public JournalItem(byte[] array) {
        status = Byte.toUnsignedInt(array[0]);
        time = new Time(Journal.getArray(array, 1, 5));
    }

    public boolean isValid() {
        return status == ITEM_V1 || status == ITEM_V2;
    }

    public abstract String getPrintableString(boolean units);

    public boolean timeEquals(JournalItem item) {
        return time.compareTo(item.time) == 0;
    }

    public int timeCompare(JournalItem item) {
        return time.compareTo(item.time);
    }

    public static int putByteToArray(int v, byte[] array, int index) {
        array[index + 0] = (byte) (v >> 0 & 0xFF);
        return 1;
    }

    public static int putShortToArray(int v, byte[] array, int index) {
        array[index + 0] = (byte) (v >> 0 & 0xFF);
        array[index + 1] = (byte) (v >> 8 & 0xFF);
        return 2;
    }

    public static int putLongToArray(long v, byte[] array, int index) {
        array[index + 0] = (byte) (v >>  0 & 0xFF);
        array[index + 1] = (byte) (v >>  8 & 0xFF);
        array[index + 2] = (byte) (v >> 16 & 0xFF);
        array[index + 3] = (byte) (v >> 24 & 0xFF);
        return 4;
    }

    public abstract int toByteArray(byte[] array, int index, int version);

    public int headerToByteArray(byte[] array, int index, int version) {
        index += putByteToArray(version, array, index);
        return time.toByteArray(array, index) + 1;
    }
}
