package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Database implements AutoCloseable {
    private final Connection conn;
    private final String userName;
    private final String password;
    private ProgressBar progressDataBar;

    public Database(String user, String password) throws SQLException {
        this.userName = user;
        this.password = password;
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", user, password);
    }

    public void setProgressDataBar(ProgressBar progressDataBar) {
        this.progressDataBar = progressDataBar;
    }

    public ObservableList<String> showDatabase() throws SQLException {
        try (Statement cursor = conn.createStatement();
             ResultSet resultSet = cursor.executeQuery("SHOW DATABASES")
        ) {
            ObservableList<String> databases = FXCollections.observableArrayList();
            while (resultSet.next()) {
                databases.add(resultSet.getString(1));
            }
            return databases;
        }
    }

    public ObservableList<String> showTables(String databaseName) throws SQLException {
        conn.setCatalog(databaseName);
        try (Statement fetchTableQuery = conn.createStatement();
             ResultSet tables = fetchTableQuery.executeQuery("SHOW TABLES")
        ) {
            ObservableList<String> tableList = FXCollections.observableArrayList();
            while (tables.next()) {
                tableList.add(tables.getString(1));
            }
            return tableList;
        }
    }

    public Column showData(String tableName) throws SQLException {
        progressDataBar.setProgress(0);
        progressDataBar.setVisible(true);
        try (Statement executeTableQuery = conn.createStatement();
             ResultSet resultSet = executeTableQuery.executeQuery("SELECT * FROM " + tableName)
        ) {
            ResultSetMetaData rs = resultSet.getMetaData();
            Column columns = new Column();

            ObservableList<String> heading = FXCollections.observableArrayList();
            int totalSize = count(tableName, null);
//            System.out.println(totalSize);

            columns.setHsize(rs.getColumnCount());
//            columns.setVsize(resultSet.getRow());
            for (int i = 1; i <= rs.getColumnCount(); i++) {
                heading.add(rs.getColumnName(i));
//            System.out.print(rs.getColumnName(i) + " ");
            }
            columns.setHeading(heading);
            columns.setType(descSingle(tableName, 2));
            return getColumn(columns, resultSet, rs, totalSize);
        }
    }

    public int dropDatabase(String databaseName) throws SQLException {
        try (Statement cursor = conn.createStatement()) {
            return cursor.executeUpdate("DROP DATABASE " + databaseName);
        }
    }

    public int createDatabase(String databaseName) throws SQLException {
        try (Statement cursor = conn.createStatement()) {
            return cursor.executeUpdate("CREATE DATABASE " + databaseName);
        }
    }

    public int dropTable(String tableName) throws SQLException {
        try (Statement cursor = conn.createStatement()) {
            return cursor.executeUpdate("DROP TABLE " + tableName);
        }
    }

    public int createTable(String tableName,
                           List<String> name, List<String> datatype,
                           List<String> primaryKey, Map<String,String> foreignKeys) throws SQLException {
        try (Statement cursor = conn.createStatement()) {
//        create table c(id int,primary key(id))
            System.out.println(foreignKeys);
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
//            System.out.println(primaryKey.size());
                for (int i = 0; i < primaryKey.size(); i++) {
                    s.append(primaryKey.get(i))
                            .append(" ");
                    if (i < primaryKey.size() - 1) {
                        s.append(", ");
                    }
                }
                s.append(" )");
            }
//            create table shampoo(pop int , FOREIGN KEY (pop) REFERENCES vika(id));
            if (foreignKeys.size() > 0) {
                for (Map.Entry<String,String> stringEntry: foreignKeys.entrySet()) {
                    s.append(", FOREIGN KEY (")
                            .append(stringEntry.getKey())
                            .append(") REFERENCES ")
                            .append(stringEntry.getValue());
                }
            }
            s.append(")");
            System.out.println(s.toString());
            return cursor.executeUpdate(s.toString());
        }

    }

    public int insertIntoTable(String tableName,
                               List<Object> columnDatas
    ) throws SQLException {
        List<String> columnData = columnDatas.stream().map(s -> {
            if (s == null)
                return null;
            return s.toString();
        }).collect(Collectors.toList());
//        INSERT INTO sampling(id,roll,name) VALUES('23','32','wde');
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(tableName)
                .append(" VALUES (");
        for (int i = 0; i < columnData.size(); ) {
            sb.append(" ?");
            if (i < columnData.size() - 1) {
                sb.append(",");
            }
            i++;
        }
        sb.append(")");
//        System.out.println(sb.toString());
        try (PreparedStatement cursor = conn.prepareStatement(sb.toString())) {
            return insertion(columnData, cursor, 0);
        }
    }

    private int insertion(List<String> columnData, PreparedStatement cursor, int y) throws SQLException {

        for (int i = 1; i <= columnData.size(); i++) {

            cursor.setString(i + y, columnData.get(i - 1));
        }
        System.out.println(cursor.toString());
        return cursor.executeUpdate();
    }


    @Override
    public void close() throws SQLException {
        conn.close();
    }

    public List<String> primaryKey(String tableName) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW KEYS FROM " + tableName + " WHERE Key_name = 'PRIMARY'")
        ) {
            List<String> keys = new ArrayList<>();
            while (resultSet.next()) {
                keys.add(resultSet.getString(5));
            }
            return keys;
        }
    }

    public int deleteData(String tableName, List<String> value, List<String> primaryKey) throws SQLException {
//        delete from s where id='90';
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("DELETE FROM ")
                .append(tableName);
        try (PreparedStatement cursor = conn.prepareStatement(
                wherePrimaryKey(stringBuilder, primaryKey).toString())) {
            return insertion(value, cursor, 0);
        }

    }

    public int updateData(String tableName,
                          String columnModified, String newValue,
                          List<String> value, List<String> primaryKey) throws SQLException {
//        update c set id=1 where id='2';
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ")
                .append(tableName)
                .append(" SET ")
                .append(columnModified)
                .append(" = ")
                .append("?");

        try (PreparedStatement cursor = conn.prepareStatement(
                wherePrimaryKey(sb, primaryKey).toString())) {
            cursor.setString(1, newValue);
            return insertion(value, cursor, 1);
        }

    }

    private StringBuilder wherePrimaryKey(StringBuilder sb, List<String> strings) {
        sb.append(" WHERE");
        for (int i = 0; i < strings.size(); i++) {
            sb.append(" ")
                    .append(strings.get(i))
                    .append(" = ?");
            if (i < strings.size() - 1) {
                sb.append(" AND");
            }
        }
        return sb;
    }

    public ObservableList<String> descSingle(String tableName, int index) throws SQLException {
        try (Statement cursor = conn.createStatement();
             ResultSet resultSet = cursor.executeQuery("DESC " + tableName)
        ) {
            ObservableList<String> observableList = FXCollections.observableArrayList();
            while (resultSet.next()) {
                observableList.add(resultSet.getString(index));
            }
            return observableList;
        }
    }

    public ObservableList<ObservableList<String>> descAll(String tableNAme) throws SQLException {
        try (Statement cursor = conn.createStatement();
             ResultSet resultSet = cursor.executeQuery("DESC " + tableNAme)
        ) {
            ObservableList<ObservableList<String>> observableLists = FXCollections.observableArrayList();
            while (resultSet.next()) {
                ObservableList<String> mid = FXCollections.observableArrayList();
                mid.add(resultSet.getString(1));
                mid.add(resultSet.getString(2));
                mid.add(resultSet.getString(3));
                mid.add(resultSet.getString(4));
                mid.add(resultSet.getString(5));
                mid.add(resultSet.getString(6));
                observableLists.add(mid);
            }
            return observableLists;
        }

    }

    private int count(String tableName, String whereClause) throws SQLException {
        try (Statement cursor = conn.createStatement()) {
            ResultSet resultSet;
            if (whereClause == null) {
                resultSet = cursor.executeQuery("SELECT COUNT(*) FROM " + tableName);
            } else {
                resultSet = cursor.executeQuery("SELECT COUNT(*) FROM " + tableName + " WHERE " + whereClause);
            }
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    public Column filterQuery(String tableName, String whereClause) throws SQLException {
        progressDataBar.setProgress(0);
        progressDataBar.setVisible(true);
        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(tableName)
                .append(" WHERE ")
                .append(whereClause);
        System.out.println(sb.toString());
        try (Statement statement = conn.createStatement()) {
            Column columns = new Column();
            ResultSet resultSet = statement.executeQuery(sb.toString());
            ResultSetMetaData rs = resultSet.getMetaData();
            columns.setHsize(rs.getColumnCount());
//            columns.setVsize(resultSet.getRow());
            ObservableList<String> heading = FXCollections.observableArrayList();
            for (int i = 1; i <= rs.getColumnCount(); i++) {
                heading.add(rs.getColumnName(i));
//            System.out.print(rs.getColumnName(i) + " ");
            }
            columns.setHeading(heading);
            columns.setType(descSingle(tableName, 2));
            int totalSize = count(tableName, whereClause);
            return getColumn(columns, resultSet, rs, totalSize);
        }
    }

    private Column getColumn(Column columns, ResultSet resultSet, ResultSetMetaData rs, int totalSize) throws SQLException {
        while (resultSet.next()) {
            progressDataBar.setProgress((double) resultSet.getRow() / totalSize);
            ObservableList<Object> observableList = FXCollections.observableArrayList();
            for (int i = 1; i <= rs.getColumnCount(); i++) {
                observableList.add(resultSet.getString(i));
            }
            columns.add(observableList);
        }
        progressDataBar.setVisible(false);
        return columns;
    }

    public boolean changeTableName(String oldTableName, String newTableName) throws SQLException {
        StringBuilder sb = new StringBuilder("RENAME TABLE ");
        sb.append(oldTableName)
                .append(" TO ")
                .append(newTableName);
        try (Statement statement = conn.createStatement()) {
            return statement.execute(sb.toString());
        }
    }

    public boolean saveBackup(String databaseName, File toBeSavedAt) throws IOException, InterruptedException, SQLException {
//        mysqldump -uroot -pvikalp sample -rvika\[DB_Name].sql

        StringBuilder sb = new StringBuilder("mysqldump -u");
        sb.append(userName)
                .append(" -p")
                .append(password)
                .append(" ")
                .append(databaseName)
                .append(" -r ")
                .append(toBeSavedAt);
        System.out.println(sb.toString());
        ProcessBuilder builder = new ProcessBuilder(
                "powershell.exe", sb.toString()
        );
//        builder.redirectErrorStream(true);
        return processStart(builder);
    }

    private boolean processStart(ProcessBuilder builder) throws SQLException, IOException, InterruptedException {
        Process p = builder.start();
        p.getOutputStream().close();
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                System.out.println(line);
//            }
//        }
        if (p.waitFor() == 0) {
            return true;
        } else {
            throw new SQLException(Arrays.toString(p.getErrorStream().readAllBytes()));
        }
    }

    public boolean loadSavedDatabase(String databaseName, File toBeloaded) throws SQLException, InterruptedException, IOException {
//        mysqladmin –u [UserName] –p[Pasword] create [New_DB_Name]
//        Get-content C:\Users\vikal\Desktop\vika\pump.sql | mysql -uroot -pvikalp vik;
        createDatabase(databaseName);
        StringBuilder sb = new StringBuilder("Get-content ");
        sb.append(toBeloaded)
                .append(" | mysql -u")
                .append(userName)
                .append(" -p")
                .append(password)
                .append(" ")
                .append(databaseName);

        System.out.println(sb.toString());

        ProcessBuilder builder = new ProcessBuilder(
                "powershell.exe", sb.toString()
        );
        return processStart(builder);
    }

    public Map<String, String> nameAndTypeDESC(String tableName) throws SQLException {
        Map<String, String> map = new HashMap<>();
        try (Statement st = conn.createStatement()) {
            ResultSet resultSet = st.executeQuery("DESC " + tableName);
            while (resultSet.next()) {
                map.put(resultSet.getString(1) + " " +
                        resultSet.getString(2), resultSet.getString(1));
            }
        }
        return map;
    }

    public static class Column {
        ObservableList<String> heading;
        ObservableList<ObservableList<Object>> column;
        ObservableList<String> type;
        int hsize;

        public Column() {
            column = FXCollections.observableArrayList();
        }

        public ObservableList<String> getType() {
            return type;
        }

        public void setType(ObservableList<String> type) {
            this.type = type;
        }

        public int getHsize() {
            return hsize;
        }

        public void setHsize(int hsize) {
            this.hsize = hsize;
        }

        public ObservableList<String> getHeading() {
            return heading;
        }

        public void setHeading(ObservableList<String> heading) {
            this.heading = heading;
        }


        public void add(ObservableList<Object> f) {
            column.add(f);
        }

        public ObservableList<ObservableList<Object>> getColumn() {
            return column;
        }

    }
}