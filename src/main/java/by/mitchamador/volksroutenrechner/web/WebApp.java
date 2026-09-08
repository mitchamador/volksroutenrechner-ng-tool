package by.mitchamador.volksroutenrechner.web;

import by.mitchamador.volksroutenrechner.db.Database;
import by.mitchamador.volksroutenrechner.db.JournalRepository;
import by.mitchamador.volksroutenrechner.db.SqliteJournalRepository;

public class WebApp {

    public static void main(String[] args) throws Exception {
        String dbFile = args.length > 0 ? args[0] : "journal.db";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 7000;

        Database database = new Database(dbFile);
        database.init();

        JournalRepository repository = new SqliteJournalRepository(database);

        new WebServer(repository).start(port);

        System.out.println("Открыть в браузере: http://localhost:" + port);
    }
}
