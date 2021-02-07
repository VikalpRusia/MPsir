package model;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class Database implements AutoCloseable {
    private final Connection conn;

    public Database() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        this.conn = DriverManager.getConnection("jdbc:sqlite:passwords.sqlite");
    }

    @Override
    public void close() throws Exception {
        conn.close();
    }
}
