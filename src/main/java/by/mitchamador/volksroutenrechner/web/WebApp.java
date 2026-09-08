package by.mitchamador.volksroutenrechner.web;

import by.mitchamador.volksroutenrechner.db.ConnectionProvider;
import by.mitchamador.volksroutenrechner.db.H2Database;
import by.mitchamador.volksroutenrechner.db.H2JournalRepository;
import by.mitchamador.volksroutenrechner.db.JournalRepository;
import by.mitchamador.volksroutenrechner.db.SqliteDatabase;
import by.mitchamador.volksroutenrechner.db.SqliteJournalRepository;

public class WebApp {

    public static void main(String[] args) throws Exception {
        String dbFile = args.length > 0 ? args[0] : "./journal";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 7000;
        String engine = args.length > 2 ? args[2] : "h2";

        JournalRepository repository;
        if ("sqlite".equalsIgnoreCase(engine)) {
            ConnectionProvider database = new SqliteDatabase(dbFile);
            database.init();
            repository = new SqliteJournalRepository(database);
        } else {
            ConnectionProvider database = new H2Database(dbFile);
            database.init();
            repository = new H2JournalRepository(database);
        }

        new WebServer(repository).start(port);

        System.out.println("Открыть в браузере: http://localhost:" + port + " (БД: " + engine + ")");
    }
}
