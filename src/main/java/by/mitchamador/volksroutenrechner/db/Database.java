package by.mitchamador.volksroutenrechner.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String SCHEMA_TRIP_RECORDS =
            "CREATE TABLE IF NOT EXISTS trip_records (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "trip_type TEXT NOT NULL, " +
                    "time INTEGER NOT NULL, " + // epoch millis
                    "odo REAL NOT NULL, " +
                    "average_speed REAL NOT NULL, " +
                    "average_fuel REAL NOT NULL, " +
                    "total_fuel REAL NOT NULL, " +
                    "total_minutes INTEGER NOT NULL" +
                    ")";

    private static final String SCHEMA_ACCEL_RECORDS =
            "CREATE TABLE IF NOT EXISTS accel_records (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "start_time INTEGER NOT NULL, " + // epoch millis
                    "lower_speed INTEGER NOT NULL, " +
                    "upper_speed INTEGER NOT NULL, " +
                    "result_cs INTEGER NOT NULL" +
                    ")";

    // хранит поле "since" из заголовка journal (по одному значению на trip_type: C/A/B) -
    // это отдельное поле устройства, которое не выводится из самих записей
    private static final String SCHEMA_JOURNAL_META =
            "CREATE TABLE IF NOT EXISTS journal_meta (" +
                    "trip_type TEXT PRIMARY KEY, " +
                    "since_time INTEGER NOT NULL" + // epoch millis
                    ")";

    private static final String INDEX_TRIP_RECORDS =
            "CREATE INDEX IF NOT EXISTS idx_trip_records_type_time ON trip_records (trip_type, time)";

    private static final String INDEX_ACCEL_RECORDS =
            "CREATE INDEX IF NOT EXISTS idx_accel_records_time ON accel_records (start_time)";

    private final String url;

    public Database(String dbFilePath) {
        this.url = "jdbc:sqlite:" + dbFilePath;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public void init() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(SCHEMA_TRIP_RECORDS);
            statement.execute(SCHEMA_ACCEL_RECORDS);
            statement.execute(SCHEMA_JOURNAL_META);
            statement.execute(INDEX_TRIP_RECORDS);
            statement.execute(INDEX_ACCEL_RECORDS);
        }
    }
}
