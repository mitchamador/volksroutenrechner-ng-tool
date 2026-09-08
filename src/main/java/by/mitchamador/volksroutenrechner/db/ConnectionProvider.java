package by.mitchamador.volksroutenrechner.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Абстракция над конкретной БД (SQLite/H2) - открытие соединения и создание схемы.
 * Repository-классы работают с этим интерфейсом, не завися от движка.
 */
public interface ConnectionProvider {

    Connection getConnection() throws SQLException;

    void init() throws SQLException;
}
