package by.mitchamador.volksroutenrechner.db;

import by.mitchamador.volksroutenrechner.journal.object.Journal;

import java.sql.SQLException;

/**
 * Временный инструмент для проверки слоя БД+импорта без веб-сервера.
 * Usage: ImportCli <db-file> <journal-bin-file>
 */
public class ImportCli {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: ImportCli <db-file> <journal-bin-file>");
            return;
        }

        String dbFile = args[0];
        String journalFile = args[1];

        H2Database database = new H2Database(dbFile);
        database.init();

        JournalRepository repository = new H2JournalRepository(database);
        JournalImporter importer = new JournalImporter(repository);

        Journal journal = Journal.create(journalFile);
        JournalImporter.ImportResult result = importer.importJournal(journal);

        System.out.println(result);

        // повторный запуск с тем же файлом должен показать skipped == общему числу записей
        printCounts(repository);
    }

    private static void printCounts(JournalRepository repository) throws SQLException {
        for (char type : new char[]{'C', 'A', 'B'}) {
            System.out.println("trip " + type + ": " + repository.countTrips(type, null, null) + " записей в БД");
        }
        System.out.println("accel: " + repository.countAccels(null, null) + " записей в БД");
    }
}
