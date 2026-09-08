package by.mitchamador.volksroutenrechner.journal.object;

public enum JournalSize {
    SIZE_768(768, new int[] {20, 12, 6, 5}),
    SIZE_2048(2048, new int[] {64, 30, 12, 10})
    ;

    private final int size;
    private final int[] sizes;

    public static JournalSize get(int size) {
        for (JournalSize j : values()) {
            if (j.getSize() == size) {
                return j;
            }
        }
        throw new IllegalArgumentException("Illegal journal binary size: " + size);
    }

    public int getSize() {
        return size;
    }

    public int[] getSizes() {
        return sizes;
    }

    JournalSize(int size, int[] sizes) {
        this.size = size;
        this.sizes = sizes;
    }

}
