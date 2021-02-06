package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;
import logger.ProjectLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Database implements AutoCloseable {
    private final Connection conn;
    private final Logger logger = LoggerFactory.getLogger(ProjectLogger.class);
    private final String userName;
    private final String password;
    private ProgressBar progressDataBar;

    public Database(String user, String password) throws SQLException {
        this.userName = user;
        this.password = password;
        logger.atDebug().log("Connecting user = {}", user);
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", user, password);
    }

    public void setProgressDataBar(ProgressBar progressDataBar) {
        this.progressDataBar = progressDataBar;
    }

    public void autoCommit(boolean commit) throws SQLException {
        conn.setAutoCommit(commit);
    }

    public void commit() throws SQLException {
        conn.commit();
    }

    public void rollback() throws SQLException {
        conn.rollback();
    }

    public ObservableList<String> showDatabase() throws SQLException {
        logger.atDebug().log("SQL Query: SHOW DATABASES");
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
        logger.atDebug().log("SQL Query: SHOW TABLES");
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
        logger.atDebug().log("SQL Query: SELECT * FROM {}", tableName);
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
            logger.atDebug().log("SQL Query: DROP DATABASE {}", databaseName);
            return cursor.executeUpdate("DROP DATABASE " + databaseName);
        }
    }

    public int createDatabase(String databaseName) throws SQLException {
        try (Statement cursor = conn.createStatement()) {
            logger.atDebug().log("SQL Query: CREATE DATABASE {}", databaseName);
            return cursor.executeUpdate("CREATE DATABASE " + databaseName);
        }
    }

    public int dropTable(String tableName) throws SQLException {
        try (Statement cursor = conn.createStatement()) {
            logger.atDebug().log("SQL Query: DROP TABLE {}", tableName);
            return cursor.executeUpdate("DROP TABLE " + tableName);
        }
    }

    public int createTable(String tableName,
                           List<String> name, List<String> datatype,
                           List<String> primaryKey, List<String> uniqueKey, Map<String, String> foreignKeys,
                           List<String> notNull, List<String> autoIncrement, List<List<String>> uniqueTogether
    ) throws SQLException {
        try (Statement cursor = conn.createStatement()) {
//        create table c(id int,primary key(id))
            System.out.println(foreignKeys);
            int u = 0, n = 0, a = 0;
            StringBuilder s = new StringBuilder("CREATE TABLE " + tableName + " ( ");
            for (int i = 0; i < name.size(); i++) {
                s.append(name.get(i))
                        .append(" ")
                        .append(datatype.get(i));
                if (u < uniqueKey.size() && name.get(i).equals(uniqueKey.get(u))) {
                    u++;
                    s.append(" UNIQUE");
                }
                if (n < notNull.size() && name.get(i).equals(notNull.get(n))) {
                    n++;
                    s.append(" NOT NULL");
                }
                if (a < autoIncrement.size() && name.get(i).equals(autoIncrement.get(a))) {
                    a++;
                    s.append(" AUTO_INCREMENT");
                }
                if (i < name.size() - 1) {
                    s.append(", ");
                }
            }
            if (primaryKey.size() > 0) {
                s.append(", PRIMARY KEY( ");
//            System.out.println(primaryKey.size());
                keyAdder(primaryKey, s);
            }
//            create table shampoo(pop int , FOREIGN KEY (pop) REFERENCES vika(id));
//            if (uniqueKey.size() > 0) {
//                s.append(", UNIQUE KEY( ");
//            System.out.println(primaryKey.size());
//                keyAdder(uniqueKey, s);
//            }
            if (foreignKeys.size() > 0) {
                for (Map.Entry<String, String> stringEntry : foreignKeys.entrySet()) {
                    s.append(", FOREIGN KEY (")
                            .append(stringEntry.getKey())
                            .append(") REFERENCES ")
                            .append(stringEntry.getValue());
                }
            }
            if (uniqueTogether.size() > 0) {
                for (List<String> uniqueOneTogether : uniqueTogether) {
                    s.append(", UNIQUE( ");
                    keyAdder(uniqueOneTogether, s);
                }
            }
            s.append(")");
            System.out.println(s.toString());
            logger.atDebug().log("SQL Query: {}", s);
            return cursor.executeUpdate(s.toString());
        }

    }

    private void keyAdder(List<String> uniqueKey, StringBuilder s) {
        for (int i = 0; i < uniqueKey.size(); i++) {
            s.append(uniqueKey.get(i))
                    .append(" ");
            if (i < uniqueKey.size() - 1) {
                s.append(", ");
            }
        }
        s.append(" )");
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
        try (PreparedStatement cursor = conn.prepareStatement(sb.toString(), Statement.RETURN_GENERATED_KEYS)) {
            return insertion(columnData, cursor, 0, true);
        }
    }

    private int insertion(List<String> columnData, PreparedStatement cursor, int y, boolean insertion) throws SQLException {

        for (int i = 1; i <= columnData.size(); i++) {

            cursor.setString(i + y, columnData.get(i - 1));
        }
        System.out.println(cursor.toString());
        logger.atDebug().log("{}", cursor);
        int q = cursor.executeUpdate();
        if (!insertion) {
            return q;
        } else {
            ResultSet resultSet = cursor.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return -1;
            }
        }
    }


    @Override
    public void close() throws SQLException {
        logger.atDebug().log("Closing Connection");
        conn.close();
        logger.atDebug().log("Successfully closed connection");
    }

    public List<String> primaryKey(String tableName) throws SQLException {
        logger.atDebug().log("SQL Query: SHOW KEYS FROM {} WHERE Key_name = 'PRIMARY'", tableName);
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

    public List<String> foreignKey(String tableName) throws SQLException {
        String query = "SELECT\n" +
                "    `column_name`, \n" +
                "    `referenced_table_schema` AS foreign_db, \n" +
                "    `referenced_table_name` AS foreign_table, \n" +
                "    `referenced_column_name`  AS foreign_column \n" +
                "FROM\n" +
                "    `information_schema`.`KEY_COLUMN_USAGE`\n" +
                "WHERE\n" +
                "    `constraint_schema` = SCHEMA()\n" +
                "AND\n" +
                "    `table_name` = ?\n" +
                "AND\n" +
                "    `referenced_column_name` IS NOT NULL";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, tableName);
            logger.atDebug().log("{}", preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<String> keys = new ArrayList<>();
            while (resultSet.next()) {
                keys.add(resultSet.getString(1));
            }
            return keys;
        }
    }

    public List<String> getUniqueKeys(String tableName) throws SQLException {
        logger.atDebug().log("SQL Query: DESC {}", tableName);
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("DESC " + tableName)
        ) {
            List<String> uniqueKey = new ArrayList<>();
            while (resultSet.next()) {
                if (resultSet.getString(4).equals("UNI")) {
                    uniqueKey.add(resultSet.getString(1));
                }
            }
            return uniqueKey;
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
            return insertion(value, cursor, 0, false);
        }

    }

    public int updateData(String tableName,
                          String columnModified, String newValue,
                          List<String> value, List<String> primaryKey) throws SQLException {
//        update c set id=1 where id='2';
        if (primaryKey.size() == 0) {
            throw new SQLException("No Primary Key for identification");
        }
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
            return insertion(value, cursor, 1, false);
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
        logger.atDebug().log("SQL Query: DESC {} index = {}", tableName, index);
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

    public ObservableList<ObservableList<String>> descAll(String tableName) throws SQLException {
        logger.atDebug().log("SQL Query: DESC {}", tableName);
        try (Statement cursor = conn.createStatement();
             ResultSet resultSet = cursor.executeQuery("DESC " + tableName)
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
                logger.atDebug().log("SELECT COUNT(*) FROM {}", tableName);
                resultSet = cursor.executeQuery("SELECT COUNT(*) FROM " + tableName);
            } else {
                logger.atDebug().log("SELECT COUNT(*) FROM {} WHERE {}", tableName, whereClause);
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
        logger.atDebug().log("SQL Query: {}", sb);
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
        logger.atDebug().log("SQL Query: {}", sb);
        try (Statement statement = conn.createStatement()) {
            return statement.execute(sb.toString());
        }
    }

    public int addColumn(String tableName, String dataType, String newColumn,
                         boolean autoIncrement, boolean notNull, boolean primaryKey, boolean unique) throws SQLException {
//        ALTER TABLE table_name
//        ADD column_name datatype;
        StringBuilder sb = new StringBuilder("ALTER TABLE ");
        sb.append(tableName)
                .append(" ADD ")
                .append(newColumn)
                .append(" ")
                .append(dataType);
        if (primaryKey) {
            sb.append(" PRIMARY KEY");
        } else if (unique) {
            sb.append(" UNIQUE");
        }
        if (notNull) {
            sb.append(" NOT NULL");
        }
        if (autoIncrement) {
            sb.append(" AUTO_INCREMENT");
        }
        System.out.println(sb.toString());
        try (Statement statement = conn.createStatement()) {
            return statement.executeUpdate(sb.toString());
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
        logger.atDebug().log("Starting PowerShell and executing command = {}", sb);
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
        logger.atDebug().log("Starting PowerShell and executing command = {}", sb);

        ProcessBuilder builder = new ProcessBuilder(
                "powershell.exe", sb.toString()
        );
        return processStart(builder);
    }

    public Map<String, String> nameAndTypeDESC(String tableName) throws SQLException {
        Map<String, String> map = new HashMap<>();
        logger.atDebug().log("DESC " + tableName);
        try (Statement st = conn.createStatement()) {
            ResultSet resultSet = st.executeQuery("DESC " + tableName);
            while (resultSet.next()) {
                map.put(resultSet.getString(1) + " " +
                        resultSet.getString(2), resultSet.getString(1));
            }
        }
        return map;
    }

    public int changeColumnName(String tableName, String oldColumnName, String newColumnName) throws SQLException {
//        ALTER TABLE your_table_name RENAME COLUMN original_column_name TO new_column_name;
        StringBuilder sb = new StringBuilder("ALTER TABLE ");
        sb.append(tableName)
                .append(" RENAME COLUMN ")
                .append(oldColumnName)
                .append(" TO ")
                .append(newColumnName);
        System.out.println(sb.toString());
        try (Statement cursor = conn.createStatement()) {
            return cursor.executeUpdate(sb.toString());
        }
    }

    public int getAutoIncrementColumn(String tableName) throws SQLException {
//        SELECT Ordinal_position
//        FROM INFORMATION_SCHEMA.COLUMNS
//        WHERE TABLE_NAME = 'we'
//        AND EXTRA like '%auto_increment%'
        StringBuilder sb = new StringBuilder("SELECT Ordinal_position FROM INFORMATION_SCHEMA.COLUMNS");
        sb.append(" WHERE TABLE_NAME = '")
                .append(tableName)
                .append("'")
                .append(" AND EXTRA like '%auto_increment%'");
        System.out.println(sb.toString());
        try (Statement cursor = conn.createStatement()) {
            ResultSet resultSet = cursor.executeQuery(sb.toString());
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return -1;
        }
    }

    public int deleteColumn(String tableName, String columnName) throws SQLException {
//        ALTER TABLE table_name
//        DROP COLUMN column_name;
        StringBuilder sb = new StringBuilder("ALTER TABLE ");
        sb.append(tableName)
                .append(" DROP COLUMN ")
                .append(columnName);
        System.out.println(sb.toString());
        try (Statement cursor = conn.createStatement()) {
            return cursor.executeUpdate(sb.toString());
        }
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