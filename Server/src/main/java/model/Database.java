package model;

import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.URL;
import java.sql.*;

@Component
public class Database implements AutoCloseable {
    private final Connection conn;
    private final PreparedStatement searchStatement;

    public Database() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        URL res = getClass().getClassLoader().getResource("passwords.sqlite");
        if (res == null) {
            throw new RuntimeException(
                    "Database not present in resource folder with name passwords.sqlite"
            );
        }
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + res.getPath());
        this.searchStatement = conn.prepareStatement(
                "SELECT * FROM passwords WHERE phone_number = ? OR mail_id=?"
        );
    }

    public Boolean search(String search) throws SQLException {

        searchStatement.setString(1, search);
        searchStatement.setString(2, search);
        try (
                ResultSet resultSet = searchStatement.executeQuery()) {
            return resultSet.next();
        }
    }

    @PreDestroy
    @Override
    public void close() throws Exception {
        conn.close();
    }
}
