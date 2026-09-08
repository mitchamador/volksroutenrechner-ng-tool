package by.mitchamador.volksroutenrechner.journal.object;

import org.apache.commons.io.FileUtils;

import java.io.IOException;

public class Journal {

    /*
        typedef struct {
            uint8_t current;
            uint8_t max;
        } journal_type_pos_t;
    */

    private final static String JOURNAL_HEADER = "JOURTRIP";
    private static final int MAX_ITEMS = 4;
    private final JournalEntry[] entries = new JournalEntry[MAX_ITEMS];

    public JournalEntry[] getEntries() {
        return entries;
    }

    public Journal() {
        for (int i = 0; i < 4; i++) {
            entries[i] = new JournalEntry(i);
        }
    }

    public Journal(byte[] array) {
        if (checkHeader(array)) {
            for (int i = 0; i < 4; i++) {
                entries[i] = new JournalEntry(i, array, 8 + i * 2);
                entries[i].fillItemsList(Journal.getArray(array, getStartPos(i), entries[i].getTotal() * entries[i].getLength()));
                if (i < 3) {
                    entries[i].fillTime(array, 16 + 5 * i);
                }
            }
        }
    }

    public int getStartPos(int index) {
        int startPos = START_DATA_POSITION;
        for (int i = 0; i <= index; i++) {
            if (i > 0) {
                startPos += entries[i - 1].getTotal() * entries[i - 1].getLength();
            }
        }
        return startPos;
    }

    public static Journal create(String filename) throws IOException {
        return new Journal(FileUtils.readFileToByteArray(FileUtils.getFile(filename)));
    }

    public static byte[] getArray(byte[] src, int pos, int len) {
        byte[] res = new byte[len];
        System.arraycopy(src, pos, res, 0, len);
        return res;
    }

    public boolean checkHeader(byte[] array) throws IllegalArgumentException {
        for (int c = 0; c < 8; c++) {
            if (array[c] != JOURNAL_HEADER.charAt(c)) {
                return false;
            }
        }
        return true;
    }

    private static final int START_DATA_POSITION = 32;

    public void addEntryData(JournalEntry entry) {
        JournalEntry e = entries[entry.getIndex()];
        e.addDataEntry(entry);
    }

    public int createHeader(JournalSize journalSize, byte[] array) {
        // mark
        for (int c = 0; c < 8; c++) {
            array[c] = (byte) JOURNAL_HEADER.charAt(c);
        }
        // sizes
        int index = 8;
        for (int i = 0; i < 4; i++) {
            int total = journalSize.getSizes()[i];
            int count = entries[i].getItems().size();
            if (count > total) {
                count = total;
            }
            index += JournalItem.putByteToArray(count == 0 ? 0xFF : count - 1, array, index);
            index += JournalItem.putByteToArray(total, array, index);
        }

        // trips' time
        for (int i = 0; i < 3; i++) {
            index += entries[i].getTime().toByteArray(array, index);
        }

        return START_DATA_POSITION;
    }

    public byte[] toByteArray(int size) {
        return toByteArray(size, false);
    }

    public byte[] toByteArray(int size, boolean oldVersion) {
        byte[] array = new byte[size];

        JournalSize journalSize = JournalSize.get(size);
        int index = createHeader(journalSize, array);

        for (int i = 0; i < 4; i++) {
            JournalEntry entry = entries[i];
            int startIndex = index;
            int total = journalSize.getSizes()[i];
            int count = entries[i].getItems().size();
            if (count > total) {
                count = total;
            }
            while (--count >= 0) {
                index += entries[i].getItems().get(count).toByteArray(array, index, oldVersion ? JournalItem.ITEM_V1 : JournalItem.ITEM_V2);
            }
            index = startIndex + total * entry.getLength();
        }

        return array;
    }

    public void sortEntries() {
        for (JournalEntry entry : entries) {
            // reverse sort by date
            entry.getItems().sort((o1, o2) -> o2.timeCompare(o1));
        }
    }

    public String getPrintableString(boolean units) {
        StringBuilder s = new StringBuilder();
        for (JournalEntry item : getEntries()) {
            s.append(item.getPrintableString(units)).append("\n");
        }
        return s.toString();
    }
}
