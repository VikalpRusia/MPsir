package model;

import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class Database implements AutoCloseable {
    private final Connection conn;
    private final PreparedStatement searchStatement;
    private final PreparedStatement mail_with_password;
    private final PreparedStatement mail_with_UUID;

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
        this.mail_with_password = conn.prepareStatement(
                "SELECT mail_id,password,date,phone_number FROM passwords WHERE phone_number = ? OR mail_id=?"
        );
        this.mail_with_UUID = conn.prepareStatement(
                "SELECT mail_id FROM passwords WHERE UUID = ?"
        );
    }

    public String search(String search) throws SQLException {

        searchStatement.setString(1, search);
        searchStatement.setString(2, search);
        try (
                ResultSet resultSet = searchStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString(3);
            }
        }
        return null;
    }

    public String[] sendMail_Passcode_DOB_Phone(String search) throws SQLException {
        mail_with_password.setString(1, search);
        mail_with_password.setString(2, search);
        String[] strings = new String[4];
        try (
                ResultSet resultSet = mail_with_password.executeQuery()) {
            if (resultSet.next()) {
                strings[0] = resultSet.getString(1);
                strings[1] = resultSet.getString(2);
                strings[2] = resultSet.getString(3);
                strings[3] = resultSet.getString(4);
                return strings;
            }
        }
        return null;//Not possible
    }

    public List<String> getMail(String UUID) throws SQLException {
        mail_with_UUID.setString(1, UUID);
        try (ResultSet resultSet = mail_with_UUID.executeQuery()) {
            List<String> mails = new ArrayList<>();
            while (resultSet.next()) {
                mails.add(resultSet.getString(1));
            }
            return mails;
        }
    }

    @PreDestroy
    @Override
    public void close() throws Exception {
        conn.close();
    }
}