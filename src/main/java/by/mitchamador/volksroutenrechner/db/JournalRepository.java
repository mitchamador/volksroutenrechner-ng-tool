package by.mitchamador.volksroutenrechner.db;

import by.mitchamador.volksroutenrechner.db.record.AccelRecord;
import by.mitchamador.volksroutenrechner.db.record.TripRecord;

import java.sql.SQLException;
import java.util.List;

public interface JournalRepository {

    /**
     * @param from нижняя граница периода (epoch millis, включительно), null - без ограничения
     * @param to   верхняя граница периода (epoch millis, включительно), null - без ограничения
     */
    List<TripRecord> findTrips(char tripType, Long from, Long to) throws SQLException;

    List<AccelRecord> findAccels(Long from, Long to) throws SQLException;

    void deleteTrip(long id) throws SQLException;

    void deleteAccel(long id) throws SQLException;

    void updateTrip(TripRecord record) throws SQLException;

    void updateAccel(AccelRecord record) throws SQLException;

    /**
     * Вставляет запись, если такой (совпадение по всем полям данных) для этого типа трипа ещё нет.
     *
     * @return true, если запись была вставлена; false, если пропущена как дубликат
     */
    boolean insertTripIfNotExists(TripRecord record) throws SQLException;

    boolean insertAccelIfNotExists(AccelRecord record) throws SQLException;

    /**
     * @return сохранённое значение поля "since" для этого типа трипа (epoch millis), или null, если ещё не задано
     */
    Long getSinceTime(char tripType) throws SQLException;

    /**
     * Обновляет "since" для трипа, только если переданное значение новее уже сохранённого
     * (или значения ещё нет).
     */
    void updateSinceTimeIfNewer(char tripType, long sinceTime) throws SQLException;
}
