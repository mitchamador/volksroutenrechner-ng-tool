package by.mitchamador.volksroutenrechner.db;

import by.mitchamador.volksroutenrechner.db.record.AccelRecord;
import by.mitchamador.volksroutenrechner.db.record.TripRecord;
import by.mitchamador.volksroutenrechner.journal.object.AccelItem;
import by.mitchamador.volksroutenrechner.journal.object.Journal;
import by.mitchamador.volksroutenrechner.journal.object.JournalEntry;
import by.mitchamador.volksroutenrechner.journal.object.Time;
import by.mitchamador.volksroutenrechner.journal.object.TripItem;

import java.sql.SQLException;
import java.util.List;

/**
 * Собирает {@link Journal} из текущего состояния БД, чтобы дальше воспользоваться
 * уже существующими Journal.toByteArray()/toByteArray(size, version) для экспорта.
 */
public class JournalExporter {

    // индексы 0,1,2 в Journal.getEntries() соответствуют trip C / A / B (см. JournalEntry.getHeaders())
    private static final char[] TRIP_TYPES = {'C', 'A', 'B'};

    private final JournalRepository repository;

    public JournalExporter(JournalRepository repository) {
        this.repository = repository;
    }

    public Journal buildJournal() throws SQLException {
        Journal journal = new Journal();
        JournalEntry[] entries = journal.getEntries();

        for (int i = 0; i < TRIP_TYPES.length; i++) {
            List<TripRecord> records = repository.findTrips(TRIP_TYPES[i], null, null, null, null);
            JournalEntry entry = entries[i];
            Time latest = null;
            for (TripRecord r : records) {
                Time time = new Time(r.getTime());
                entry.getItems().add(new TripItem(
                        time,
                        (int) Math.round(r.getOdo() * 10),
                        (int) Math.round(r.getAverageSpeed() * 10),
                        (int) Math.round(r.getAverageFuel() * 10),
                        (int) Math.round(r.getTotalFuel() * 10),
                        r.getTotalMinutes()
                ));
                if (latest == null || latest.compareTo(time) < 0) {
                    latest = time;
                }
            }

            Long since = repository.getSinceTime(TRIP_TYPES[i]);
            if (since != null) {
                entry.setTime(new Time(since));
            } else if (latest != null) {
                // на случай, если since ещё не сохранён (например, старая БД без импорта через новый код)
                entry.setTime(latest);
            }
        }

        JournalEntry accelEntry = entries[3];
        for (AccelRecord r : repository.findAccels(null, null, null, null)) {
            accelEntry.getItems().add(new AccelItem(new Time(r.getStartTime()), r.getLowerSpeed(), r.getUpperSpeed(), r.getResultCs()));
        }

        journal.sortEntries();
        return journal;
    }
}
