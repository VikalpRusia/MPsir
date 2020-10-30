package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.List;

public class Database implements AutoCloseable {
    private final Connection conn;

    public Database(String user, String password) throws SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", user, password);
    }

    public ObservableList<String> showDatabase() throws SQLException {
        Statement cursor = conn.createStatement();
        ResultSet resultSet = cursor.executeQuery("SHOW DATABASES");
        ObservableList<String> databases = FXCollections.observableArrayList();
        while (resultSet.next()) {
            databases.add(resultSet.getString(1));
        }
        cursor.close();
        return databases;
    }

    public ObservableList<String> showTables(String databaseName) throws SQLException {
        conn.setCatalog(databaseName);
        Statement fetchTableQuery = conn.createStatement();
        ResultSet tables = fetchTableQuery.executeQuery("SHOW TABLES");
        ObservableList<String> tableList = FXCollections.observableArrayList();
        while (tables.next()) {
            tableList.add(tables.getString(1));
        }
        fetchTableQuery.close();
        return tableList;
    }

    public Column showData(String tableName) throws SQLException {
        Statement executeTableQuery = conn.createStatement();
        ResultSet resultSet = executeTableQuery.executeQuery("SELECT * FROM " + tableName);
        ResultSetMetaData rs = resultSet.getMetaData();
        Column columns = new Column();
        ObservableList<String> heading = FXCollections.observableArrayList();
        columns.setHsize(rs.getColumnCount());
        columns.setVsize(resultSet.getFetchSize());
        for (int i = 1; i <= rs.getColumnCount(); i++) {
            heading.add(rs.getColumnName(i));
//            System.out.print(rs.getColumnName(i) + " ");
        }
        columns.setHeading(heading);
        while (resultSet.next()) {
            ObservableList<Object> observableList = FXCollections.observableArrayList();
            for (int i = 1; i <= rs.getColumnCount(); i++) {
                observableList.add(resultSet.getString(i));
            }
            columns.add(observableList);
        }
        executeTableQuery.close();
        return columns;
    }

    public void dropDatabase(String databaseName) throws SQLException {
        Statement cursor = conn.createStatement();
        cursor.execute("DROP DATABASE " + databaseName);
        cursor.close();
    }

    public void createDatabase(String databaseName) throws SQLException {
        Statement cursor = conn.createStatement();
        cursor.execute("CREATE DATABASE " + databaseName);
        cursor.close();
    }

    public void dropTable(String tableName) throws SQLException {
        Statement cursor = conn.createStatement();
        cursor.execute("DROP TABLE " + tableName);
        cursor.close();
    }

    public void createTable(String tableName,
                            List<String> name, List<String> datatype,
                            List<String> primaryKey) throws SQLException {
        Statement cursor = conn.createStatement();
//        create table c(id int,primary key(id))
        StringBuilder s = new StringBuilder("CREATE TABLE " + tableName + " ( ");
        for (int i = 0; i < name.size(); i++) {
            s.append(name.get(i))
                    .append(" ")
                    .append(datatype.get(i));
            if (i < name.size() - 1) {
                s.append(", ");
            }
        }
        if (primaryKey.size() > 0) {
            s.append(", PRIMARY KEY( ");
            System.out.println(primaryKey.size());
            for (int i = 0; i < primaryKey.size(); i++) {
                s.append(primaryKey.get(i))
                        .append(" ");
                if (i < primaryKey.size() - 1) {
                    s.append(", ");
                }
            }
            s.append(" )");
        }
        s.append(")");
        System.out.println(s.toString());
        cursor.execute(s.toString());
        cursor.close();

    }


    @Override
    public void close() throws SQLException {
        conn.close();
    }

    public static class Column {
        ObservableList<String> heading;
        ObservableList<ObservableList<Object>> column;
        int hsize;
        int vsize;

        public int getHsize() {
            return hsize;
        }

        public void setHsize(int hsize) {
            this.hsize = hsize;
        }

        public void setVsize(int vsize) {
            this.vsize = vsize;
        }

        public ObservableList<String> getHeading() {
            return heading;
        }

        public void setHeading(ObservableList<String> heading) {
            this.heading = heading;
        }

        public Column() {
            column = FXCollections.observableArrayList();
        }

        public void add(ObservableList<Object> f) {
            column.add(f);
        }

        public ObservableList<ObservableList<Object>> getColumn() {
            return column;
        }

    }
}