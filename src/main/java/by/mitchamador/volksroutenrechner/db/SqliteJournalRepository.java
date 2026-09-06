package by.mitchamador.volksroutenrechner.db;

import by.mitchamador.volksroutenrechner.db.record.AccelRecord;
import by.mitchamador.volksroutenrechner.db.record.TripRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqliteJournalRepository implements JournalRepository {

    private final Database database;

    public SqliteJournalRepository(Database database) {
        this.database = database;
    }

    @Override
    public List<TripRecord> findTrips(char tripType, Long from, Long to, Integer limit, Integer offset) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM trip_records WHERE trip_type = ?");
        List<Object> params = new ArrayList<>();
        params.add(String.valueOf(tripType));
        appendPeriod(sql, params, "time", from, to);
        sql.append(" ORDER BY time DESC");
        appendLimitOffset(sql, params, limit, offset);

        List<TripRecord> result = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParams(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapTrip(rs));
                }
            }
        }
        return result;
    }

    @Override
    public int countTrips(char tripType, Long from, Long to) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM trip_records WHERE trip_type = ?");
        List<Object> params = new ArrayList<>();
        params.add(String.valueOf(tripType));
        appendPeriod(sql, params, "time", from, to);
        return executeCount(sql, params);
    }

    @Override
    public List<AccelRecord> findAccels(Long from, Long to, Integer limit, Integer offset) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM accel_records WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        appendPeriod(sql, params, "start_time", from, to);
        sql.append(" ORDER BY start_time DESC");
        appendLimitOffset(sql, params, limit, offset);

        List<AccelRecord> result = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParams(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapAccel(rs));
                }
            }
        }
        return result;
    }

    @Override
    public int countAccels(Long from, Long to) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM accel_records WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        appendPeriod(sql, params, "start_time", from, to);
        return executeCount(sql, params);
    }

    @Override
    public void deleteTrip(long id) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM trip_records WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public void deleteAccel(long id) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM accel_records WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public void updateTrip(TripRecord record) throws SQLException {
        String sql = "UPDATE trip_records SET trip_type=?, time=?, odo=?, average_speed=?, average_fuel=?, total_fuel=?, total_minutes=? WHERE id=?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, String.valueOf(record.getTripType()));
            statement.setLong(2, record.getTime());
            statement.setDouble(3, record.getOdo());
            statement.setDouble(4, record.getAverageSpeed());
            statement.setDouble(5, record.getAverageFuel());
            statement.setDouble(6, record.getTotalFuel());
            statement.setInt(7, record.getTotalMinutes());
            statement.setLong(8, record.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public void updateAccel(AccelRecord record) throws SQLException {
        String sql = "UPDATE accel_records SET start_time=?, lower_speed=?, upper_speed=?, result_cs=? WHERE id=?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, record.getStartTime());
            statement.setInt(2, record.getLowerSpeed());
            statement.setInt(3, record.getUpperSpeed());
            statement.setInt(4, record.getResultCs());
            statement.setLong(5, record.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public boolean insertTripIfNotExists(TripRecord record) throws SQLException {
        try (Connection connection = database.getConnection()) {
            String selectSql = "SELECT * FROM trip_records WHERE trip_type = ? AND time = ?";
            try (PreparedStatement select = connection.prepareStatement(selectSql)) {
                select.setString(1, String.valueOf(record.getTripType()));
                select.setLong(2, record.getTime());
                try (ResultSet rs = select.executeQuery()) {
                    while (rs.next()) {
                        if (record.sameDataAs(mapTrip(rs))) {
                            return false; // дубликат, пропускаем
                        }
                    }
                }
            }
            String insertSql = "INSERT INTO trip_records (trip_type, time, odo, average_speed, average_fuel, total_fuel, total_minutes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                insert.setString(1, String.valueOf(record.getTripType()));
                insert.setLong(2, record.getTime());
                insert.setDouble(3, record.getOdo());
                insert.setDouble(4, record.getAverageSpeed());
                insert.setDouble(5, record.getAverageFuel());
                insert.setDouble(6, record.getTotalFuel());
                insert.setInt(7, record.getTotalMinutes());
                insert.executeUpdate();
            }
            return true;
        }
    }

    @Override
    public boolean insertAccelIfNotExists(AccelRecord record) throws SQLException {
        try (Connection connection = database.getConnection()) {
            String selectSql = "SELECT * FROM accel_records WHERE start_time = ?";
            try (PreparedStatement select = connection.prepareStatement(selectSql)) {
                select.setLong(1, record.getStartTime());
                try (ResultSet rs = select.executeQuery()) {
                    while (rs.next()) {
                        if (record.sameDataAs(mapAccel(rs))) {
                            return false;
                        }
                    }
                }
            }
            String insertSql = "INSERT INTO accel_records (start_time, lower_speed, upper_speed, result_cs) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                insert.setLong(1, record.getStartTime());
                insert.setInt(2, record.getLowerSpeed());
                insert.setInt(3, record.getUpperSpeed());
                insert.setInt(4, record.getResultCs());
                insert.executeUpdate();
            }
            return true;
        }
    }

    @Override
    public Long getSinceTime(char tripType) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT since_time FROM journal_meta WHERE trip_type = ?")) {
            statement.setString(1, String.valueOf(tripType));
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong("since_time") : null;
            }
        }
    }

    @Override
    public void updateSinceTimeIfNewer(char tripType, long sinceTime) throws SQLException {
        String sql = "INSERT INTO journal_meta (trip_type, since_time) VALUES (?, ?) " +
                "ON CONFLICT(trip_type) DO UPDATE SET since_time = excluded.since_time " +
                "WHERE excluded.since_time > journal_meta.since_time";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, String.valueOf(tripType));
            statement.setLong(2, sinceTime);
            statement.executeUpdate();
        }
    }

    private void appendLimitOffset(StringBuilder sql, List<Object> params, Integer limit, Integer offset) {
        if (limit != null) {
            sql.append(" LIMIT ?");
            params.add(limit);
            if (offset != null) {
                sql.append(" OFFSET ?");
                params.add(offset);
            }
        }
    }

    private int executeCount(StringBuilder sql, List<Object> params) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParams(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void appendPeriod(StringBuilder sql, List<Object> params, String column, Long from, Long to) {
        if (from != null) {
            sql.append(" AND ").append(column).append(" >= ?");
            params.add(from);
        }
        if (to != null) {
            sql.append(" AND ").append(column).append(" <= ?");
            params.add(to);
        }
    }

    private void bindParams(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    private TripRecord mapTrip(ResultSet rs) throws SQLException {
        TripRecord record = new TripRecord();
        record.setId(rs.getLong("id"));
        record.setTripType(rs.getString("trip_type").charAt(0));
        record.setTime(rs.getLong("time"));
        record.setOdo(rs.getDouble("odo"));
        record.setAverageSpeed(rs.getDouble("average_speed"));
        record.setAverageFuel(rs.getDouble("average_fuel"));
        record.setTotalFuel(rs.getDouble("total_fuel"));
        record.setTotalMinutes(rs.getInt("total_minutes"));
        return record;
    }

    private AccelRecord mapAccel(ResultSet rs) throws SQLException {
        AccelRecord record = new AccelRecord();
        record.setId(rs.getLong("id"));
        record.setStartTime(rs.getLong("start_time"));
        record.setLowerSpeed(rs.getInt("lower_speed"));
        record.setUpperSpeed(rs.getInt("upper_speed"));
        record.setResultCs(rs.getInt("result_cs"));
        return record;
    }
}
