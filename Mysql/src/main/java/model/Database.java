package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
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

//    public void insertIntoTable(String tableName,
//                                List<String> columnName, List<String> columnType,
//                                List<String> columnData
//    ) throws SQLException {
////        INSERT INTO table_name1 ( column_1 ) values('viak');
//        StringBuilder sb = new StringBuilder("INSERT INTO ");
//        sb.append(tableName)
//                .append(" (");
//        for (int i = 0; i < columnName.size(); i++) {
//            sb.append(" ")
//                    .append(columnName.get(i));
//            if (i < columnName.size() - 1) {
//                sb.append(",");
//            }
//        }
//        sb.append(") ")
//                .append("VALUES (");
//        for (int i = 0; i < columnData.size(); i++) {
//            sb.append(" ");
//            if (columnType.get(i).matches("^int")) {
//                sb.append(columnData.get(i));
//            } else if (columnType.get(i).matches("^double")) {
//                sb.append(columnData.get(i));
//            } else if (columnType.get(i).matches("^char\\(\\s*\\d+\\s*\\)$")) {
//                sb.append("'")
//                        .append(columnData.get(i))
//                        .append("'");
//            } else if (columnType.get(i).matches("^varchar\\(\\s*\\d+\\s*\\)$")) {
//                sb.append("'")
//                        .append(columnData.get(i))
//                        .append("'");
//            }
//            if (i < columnData.size() - 1) {
//                sb.append(",");
//            }
//        }
//        sb.append(")");
//        System.out.println(sb.toString());
////        Statement cursor = conn.createStatement();
////        cursor.execute(sb.toString());
////        cursor.close();
//    }


    @Override
    public void close() throws SQLException {
        conn.close();
    }

    public List<String> primaryKey(String tableName) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery("SHOW KEYS FROM " + tableName + " WHERE Key_name = 'PRIMARY'");
        List<String> keys = new ArrayList<>();
        while (resultSet.next()) {
            keys.add(resultSet.getString(5));
        }
        return keys;
    }

    public List<Integer> positionPrimaryKey(String tableName) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery("SHOW KEYS FROM " + tableName + " WHERE Key_name = 'PRIMARY'");
        List<Integer> keys = new ArrayList<>();
        while (resultSet.next()) {
            keys.add(resultSet.getInt(4));
        }
        return keys;
    }

    public void deleteData(String tableName, List<String> value) throws SQLException {
//        delete from s where id='90';
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("DELETE FROM ")
                .append(tableName);
        wherePrimaryKey(stringBuilder,tableName,value);

    }

    public void updateData(String tableName,
                           String columnModified, String newValue,
                           List<String> value) throws SQLException {
//        update c set id=1 where id='2';
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ")
                .append(tableName)
                .append(" SET ")
                .append(columnModified)
                .append(" = ")
                .append("'")
                .append(newValue)
                .append("'");
        wherePrimaryKey(sb,tableName,value);
    }
    private void wherePrimaryKey(StringBuilder sb, String tableName,List<String> value) throws SQLException {
        sb.append(" WHERE");
        List<String> strings = primaryKey(tableName);
        for (int i = 0; i < strings.size(); i++) {
            sb.append(" ")
                    .append(strings.get(i))
                    .append(" = ?");
            if (i < strings.size() - 1) {
                sb.append(" AND");
            }
        }
        PreparedStatement preparedStatement = conn.prepareStatement(sb.toString());
        for (int i = 1; i <= value.size(); i++) {
            preparedStatement.setString(i, value.get(i - 1));
        }
        System.out.println(preparedStatement.toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
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