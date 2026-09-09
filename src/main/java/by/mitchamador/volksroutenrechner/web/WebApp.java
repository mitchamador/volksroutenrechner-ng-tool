package by.mitchamador.volksroutenrechner.web;

import by.mitchamador.volksroutenrechner.db.ConnectionProvider;
import by.mitchamador.volksroutenrechner.db.H2Database;
import by.mitchamador.volksroutenrechner.db.H2JournalRepository;
import by.mitchamador.volksroutenrechner.db.JournalRepository;
import by.mitchamador.volksroutenrechner.db.SqliteDatabase;
import by.mitchamador.volksroutenrechner.db.SqliteJournalRepository;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class WebApp {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder("d").longOpt("db").hasArg().argName("path")
                .desc("путь к файлу БД (без расширения - см. движок), по умолчанию: ./journal").build());
        options.addOption(Option.builder("p").longOpt("port").hasArg().argName("port")
                .desc("порт, по умолчанию: 7000").build());
        options.addOption(Option.builder().longOpt("host").hasArg().argName("host")
                .desc("адрес для прослушивания, по умолчанию: localhost").build());
        options.addOption(Option.builder("r").longOpt("root-path").hasArg().argName("path")
                .desc("префикс пути, если инструмент работает не в корне сайта (например /tool) - " +
                        "полезно за reverse proxy").build());
        options.addOption(Option.builder("e").longOpt("engine").hasArg().argName("sqlite|h2")
                .desc("движок БД, по умолчанию: h2 (sqlite доступен только если jar собран с профилем sqlite)").build());
        options.addOption(Option.builder("h").longOpt("help").desc("показать эту справку").build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            printHelp(options);
            return;
        }

        if (cmd.hasOption("help")) {
            printHelp(options);
            return;
        }

        String dbFile = cmd.getOptionValue("db", "./journal");
        int port = Integer.parseInt(cmd.getOptionValue("port", "7000"));
        String host = cmd.getOptionValue("host", "localhost");
        String rootPath = normalizeRootPath(cmd.getOptionValue("root-path", ""));
        String engine = cmd.getOptionValue("engine", "h2");

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

        new WebServer(repository).start(host, port, rootPath);

        System.out.println("Открыть в браузере: http://" + host + ":" + port + rootPath + "/ (БД: " + engine + ")");
    }

    private static String normalizeRootPath(String rootPath) {
        if (rootPath == null || rootPath.isEmpty() || "/".equals(rootPath)) {
            return "";
        }
        String path = rootPath.startsWith("/") ? rootPath : "/" + rootPath;
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private static void printHelp(Options options) {
        new HelpFormatter().printHelp("java -jar volksroutenrechner-tool.jar [опции]", options);
    }
}
