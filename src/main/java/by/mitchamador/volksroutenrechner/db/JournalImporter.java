package by.mitchamador.volksroutenrechner.db;

import by.mitchamador.volksroutenrechner.db.record.AccelRecord;
import by.mitchamador.volksroutenrechner.db.record.TripRecord;
import by.mitchamador.volksroutenrechner.journal.object.AccelItem;
import by.mitchamador.volksroutenrechner.journal.object.Journal;
import by.mitchamador.volksroutenrechner.journal.object.JournalEntry;
import by.mitchamador.volksroutenrechner.journal.object.JournalItem;
import by.mitchamador.volksroutenrechner.journal.object.TripItem;

import java.sql.SQLException;

/**
 * Импортирует уже распарсенный {@link Journal} (см. journal.object) в БД,
 * пропуская записи, которые там уже есть (см. {@link TripRecord#sameDataAs}
 * / {@link AccelRecord#sameDataAs}).
 */
public class JournalImporter {

    // индексы 0,1,2 в Journal.getEntries() соответствуют trip C / A / B (см. JournalEntry.headers)
    private static final char[] TRIP_TYPES = {'C', 'A', 'B'};

    private final JournalRepository repository;

    public JournalImporter(JournalRepository repository) {
        this.repository = repository;
    }

    public ImportResult importJournal(Journal journal) throws SQLException {
        int imported = 0;
        int skipped = 0;

        JournalEntry[] entries = journal.getEntries();
        for (int i = 0; i < entries.length; i++) {
            for (JournalItem item : entries[i].getItems()) {
                boolean inserted = (i < 3)
                        ? repository.insertTripIfNotExists(toTripRecord(TRIP_TYPES[i], (TripItem) item))
                        : repository.insertAccelIfNotExists(toAccelRecord((AccelItem) item));
                if (inserted) {
                    imported++;
                } else {
                    skipped++;
                }
            }
        }

        return new ImportResult(imported, skipped);
    }

    private TripRecord toTripRecord(char tripType, TripItem item) {
        return new TripRecord(
                tripType,
                item.getTime().getDate().getTime(),
                item.getPOdo() / 10.0,
                item.getPAverageSpeed() / 10.0,
                item.getPAverageFuel() / 10.0,
                item.getPTotalFuel() / 10.0,
                item.getPTime()
        );
    }

    private AccelRecord toAccelRecord(AccelItem item) {
        return new AccelRecord(
                item.getTime().getDate().getTime(),
                item.getLower(),
                item.getUpper(),
                item.getResult()
        );
    }

    public static class ImportResult {
        private final int imported;
        private final int skipped;

        public ImportResult(int imported, int skipped) {
            this.imported = imported;
            this.skipped = skipped;
        }

        public int getImported() {
            return imported;
        }

        public int getSkipped() {
            return skipped;
        }

        @Override
        public String toString() {
            return "импортировано: " + imported + ", пропущено (дубликаты): " + skipped;
        }
    }
}
