package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database implements AutoCloseable {
    private final Connection conn;

    public Database() throws SQLException { this.conn = DriverManager.getConnection("jdbc:sqlite:passwords.sqlite");
    }

    @Override
    public void close() throws Exception {
        conn.close();
    }
}
