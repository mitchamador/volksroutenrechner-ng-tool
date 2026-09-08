package by.mitchamador.volksroutenrechner.journal.object;

import java.util.ArrayList;
import java.util.List;

public class JournalEntry {

    private final int current;
    private final int total;
    private final int length;
    private final List<JournalItem> itemsList;
    private final int index;
    private Time time;

    public int getTotal() {
        return total;
    }

    public int getLength() {
        return length;
    }

    public int getIndex() {
        return index;
    }

    public static String[] getHeaders() {
        return headers;
    }

    public List<JournalItem> getItems() {
        return itemsList;
    }

    public JournalEntry(int index) {
        this(index, null, -1);
    }

    public JournalEntry(int index, byte[] array, int arrayIndex) {
        this.index = index;
        if (array != null) {
            this.current = Byte.toUnsignedInt(array[arrayIndex++]);
            this.total = Byte.toUnsignedInt(array[arrayIndex++]);
        } else {
            this.current = 0;
            this.total = 0;
        }
        this.length = index < 3 ? TripItem.LENGTH : AccelItem.LENGTH;
        this.itemsList = new ArrayList<>();
    }

    public void fillTime(byte[] array, int arrayIndex) {
        this.time = new Time(Journal.getArray(array, arrayIndex, 5));
    }

    public void fillItemsList(byte[] array) {
        for (int i = 0; i < total; i++) {
            int c = current - i;
            if (c < 0) {
                c += total;
            }
            JournalItem journalItem = createItem(array, c * length, index);
            if (journalItem.isValid()) {
                itemsList.add(journalItem);
            }
        }
    }

    public void addAllItems(List<? extends JournalItem> items) {
        for (JournalItem item : items) {
            if (!findItem(item)) {
                itemsList.add(item);
            }
        }
    }

    private boolean findItem(JournalItem item) {
        for (JournalItem i : itemsList) {
            if (i.timeEquals(item)) {
                return true;
            }
        }
        return false;
    }

    public JournalItem createItem(byte[] array, int pos, int index) {
        if (index < 3) {
            TripItem tripItem = new TripItem(Journal.getArray(array, pos, TripItem.LENGTH));
            tripItem.fixTime(index == 0 ? "16.09.2022" : "01.09.2022");
            return tripItem;
        } else {
            return new AccelItem(Journal.getArray(array, pos, AccelItem.LENGTH));
        }
    }

    private final static String[] headers = new String[]{"trip C journal", "trip A journal", "trip B journal", "accel journal"};

    public String getPrintableString(boolean units) {
        StringBuilder s = new StringBuilder();
        s.append(headers[index]);
        if (index < 3) {
            s.append("; since ").append(time.getPrintableString());
        }
        s.append("\n");
        for (JournalItem item : itemsList) {
            s.append(item.getPrintableString(units)).append("\n");
        }
        return s.toString();
    }

    public Time getTime() {
        return time != null ? time : new Time();
    }

    public void setTime(JournalEntry item) {
        if (getTime().compareTo(item.getTime()) < 0) {
            time = item.getTime();
        }
    }

    public void addDataEntry(JournalEntry entry) {
        setTime(entry);
        addAllItems(entry.getItems());
    }
}
