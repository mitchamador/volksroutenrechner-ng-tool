package by.mitchamador.volksroutenrechner.journal.object;

import java.util.Locale;

public class AccelItem extends JournalItem {
    public static final int LENGTH = 10;
    /*
        typedef struct {
            uint8_t status; // 1 byte
            trip_time_t start_time; // 5 byte
            uint8_t lower; // 1 byte
            uint8_t upper; // 1 byte
            uint16_t time; // 2 byte
        } journal_accel_item_t; // 10 bytes total

     */

    private final int lower;
    private final int upper;
    private final int result;

    public AccelItem(byte[] array) {
        super(array);
        lower = Byte.toUnsignedInt(array[6]);
        upper = Byte.toUnsignedInt(array[7]);
        result = ((Byte.toUnsignedInt(array[9]) << 8) + Byte.toUnsignedInt(array[8]));
    }

    @Override
    public String getPrintableString(boolean units) {
        return time.getPrintableString() + ";"
                + String.format(Locale.ROOT, units ? " %d км/ч - %d км/ч: %.2f с": "%d;%d;%.2f" , lower, upper, result / 100.0);
    }

    public int toByteArray(byte[] array, int index, int version) {
        index += headerToByteArray(array, index, version);
        index += putByteToArray(lower, array, index);
        index += putByteToArray(upper, array, index);
        putShortToArray(result, array, index);
        return LENGTH;
    }


}
