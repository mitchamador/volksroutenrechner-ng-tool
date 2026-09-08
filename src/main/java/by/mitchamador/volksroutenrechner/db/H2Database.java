package by.mitchamador.volksroutenrechner.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class H2Database implements ConnectionProvider {

    private static final String SCHEMA_TRIP_RECORDS =
            "CREATE TABLE IF NOT EXISTS trip_records (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "trip_type VARCHAR(1) NOT NULL, " +
                    "time BIGINT NOT NULL, " + // epoch millis
                    "odo DOUBLE NOT NULL, " +
                    "average_speed DOUBLE NOT NULL, " +
                    "average_fuel DOUBLE NOT NULL, " +
                    "total_fuel DOUBLE NOT NULL, " +
                    "total_minutes INT NOT NULL" +
                    ")";

    private static final String SCHEMA_ACCEL_RECORDS =
            "CREATE TABLE IF NOT EXISTS accel_records (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "start_time BIGINT NOT NULL, " + // epoch millis
                    "lower_speed INT NOT NULL, " +
                    "upper_speed INT NOT NULL, " +
                    "result_cs INT NOT NULL" +
                    ")";

    // см. комментарий в SqliteDatabase - поле "since" из заголовка journal, по одному на trip_type
    private static final String SCHEMA_JOURNAL_META =
            "CREATE TABLE IF NOT EXISTS journal_meta (" +
                    "trip_type VARCHAR(1) PRIMARY KEY, " +
                    "since_time BIGINT NOT NULL" + // epoch millis
                    ")";

    private static final String INDEX_TRIP_RECORDS =
            "CREATE INDEX IF NOT EXISTS idx_trip_records_type_time ON trip_records (trip_type, time)";

    private static final String INDEX_ACCEL_RECORDS =
            "CREATE INDEX IF NOT EXISTS idx_accel_records_time ON accel_records (start_time)";

    private final String url;

    public H2Database(String dbFilePath) {
        // H2 сам добавляет расширение (.mv.db) к указанному пути файла
        this.url = "jdbc:h2:file:" + dbFilePath;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
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
