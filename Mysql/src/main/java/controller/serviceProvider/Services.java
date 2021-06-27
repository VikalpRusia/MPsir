package controller.serviceProvider;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.util.Pair;
import model.Database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public class Services {
    private static Database database;

    private Services() {
    }

    public static void setDatabase(Database database) {
        Services.database = database;
    }

    public static void setProgressDataBar(ProgressBar progressDataBar) {
        database.setProgressDataBar(progressDataBar);
    }

    public static void setAutoCommit(boolean autoCommit) throws Exception {
        database.autoCommit(autoCommit);
    }

    public static void rollback() throws Exception {
        database.rollback();
    }

    public static void commit() throws Exception {
        database.commit();
    }

    public static class PrimaryKeyService extends Service<List<String>> {
        private String tableName;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        @Override
        protected Task<List<String>> createTask() {
            return new Task<>() {
                @Override
                protected List<String> call() throws Exception {
                    return database.primaryKey(
                            tableName
                    );
                }
            };
        }
    }

    public static class ForeignKeyService extends Service<List<String>> {
        private String tableName;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        @Override
        protected Task<List<String>> createTask() {
            return new Task<>() {
                @Override
                protected List<String> call() throws Exception {
                    return database.foreignKey(
                            tableName
                    );
                }
            };
        }
    }

    public static class UniqueKeyService extends Service<List<String>> {
        private String tableName;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        @Override
        protected Task<List<String>> createTask() {
            return new Task<>() {
                @Override
                protected List<String> call() throws Exception {
                    return database.getUniqueKeys(
                            tableName
                    );
                }
            };
        }
    }

    public static class DatabaseListProvider extends Service<ObservableList<String>> {

        @Override
        protected Task<ObservableList<String>> createTask() {
            return new Task<>() {
                @Override
                protected ObservableList<String> call() throws Exception {
                    return database.showDatabase();
                }
            };
        }

    }

    public static class TableListProvider extends Service<ObservableList<String>> {
        private String databaseName;

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        @Override
        protected Task<ObservableList<String>> createTask() {
            return new Task<>() {
                @Override
                protected ObservableList<String> call() throws Exception {
                    return database.showTables(
                            databaseName);
                }
            };
        }
    }

    public static class ColumnDetailsProvider extends Service<Database.Column> {
        private String tableName;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        @Override
        protected Task<Database.Column> createTask() {
            return new Task<>() {
                @Override
                protected Database.Column call() throws Exception {

                    return database.showData(tableName);

                }
            };
        }
    }

    public static class DeleteDatabaseService extends Service<Integer> {
        private String databaseName;

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return database.dropDatabase(databaseName);
                }
            };
        }
    }

    public static class DeleteTableService extends Service<Integer> {
        private String tableName;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return database.dropTable(tableName);
                }
            };
        }
    }

    public static class AddDatabase extends Service<Integer> {
        private String databaseName;

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {

                @Override
                protected Integer call() throws Exception {
                    return database.createDatabase(databaseName);
                }
            };
        }
    }

    public static class CreateTable extends Service<Integer> {
        private String tableName;
        private List<String> columnsName;
        private List<String> columnsType;
        private List<String> primaryKeys;
        private List<String> uniqueKeys;
        private Map<String, String> foreignKeys;
        private List<String> notNull;
        private List<String> autoIncrement;
        private List<List<String>> uniqueTogether;

        public void setNotNull(List<String> notNull) {
            this.notNull = notNull;
        }

        public void setAutoIncrement(List<String> autoIncrement) {
            this.autoIncrement = autoIncrement;
        }

        public void setUniqueTogether(List<List<String>> uniqueTogether) {
            this.uniqueTogether = uniqueTogether;
        }

        public void setForeignKeys(Map<String, String> foreignKeys) {
            this.foreignKeys = foreignKeys;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public void setColumnsName(List<String> columnsName) {
            this.columnsName = columnsName;
        }

        public void setColumnsType(List<String> columnsType) {
            this.columnsType = columnsType;
        }

        public void setPrimaryKeys(List<String> primaryKeys) {
            this.primaryKeys = primaryKeys;
        }

        public void setUniqueKeys(List<String> uniqueKeys) {
            this.uniqueKeys = uniqueKeys;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return database.createTable(
                            tableName,
                            columnsName,
                            columnsType,
                            primaryKeys,
                            uniqueKeys,
                            foreignKeys,
                            notNull,
                            autoIncrement,
                            uniqueTogether
                    );
                }
            };
        }
    }

    public static class InsertData extends Service<Integer> {
        private String tableName;
        private List<Object> values;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public void setValues(List<Object> values) {
            this.values = values;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return database.insertIntoTable(
                            tableName, values);
                }
            };
        }
    }

    public static class DeleteData extends Service<Integer> {
        private String tableName;
        private List<String> values;
        private List<String> primaryKey;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }

        public void setPrimaryKey(List<String> primaryKey) {
            this.primaryKey = primaryKey;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return database.deleteData(tableName, values, primaryKey);
                }
            };
        }
    }

    public static class UpdateData extends Service<Integer> {
        private String tableName;
        private String columnModified;
        private String newValue;
        private List<String> values;
        private List<String> primaryKey;

        public void setPrimaryKey(List<String> primaryKey) {
            this.primaryKey = primaryKey;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public void setColumnModified(String columnModified) {
            this.columnModified = columnModified;
        }

        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return database.updateData(tableName,
                            columnModified, newValue, values, primaryKey);
                }
            };
        }
    }

    public static class DescAll extends Service<ObservableList<ObservableList<String>>> {
        private String tableName;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        @Override
        protected Task<ObservableList<ObservableList<String>>> createTask() {
            return new Task<>() {
                @Override
                protected ObservableList<ObservableList<String>> call() throws Exception {
                    return database.descAll(tableName);
                }
            };
        }
    }

    public static class PrimaryKeyValueProvider extends Service<List<String>> {
        private Map<String, TableColumn<ObservableList<Object>, String>> nameTableColumn;
        private ObservableList<TableColumn<ObservableList<Object>, ?>> allColumns;
        private ObservableList<Object> objectObservableList;
        private List<String> primaryKey;

        public void setPrimaryKey(List<String> primaryKey) {
            this.primaryKey = primaryKey;
        }

        public void setAllColumns(ObservableList<TableColumn<ObservableList<Object>, ?>> allColumns) {
            this.allColumns = allColumns;
        }

        public void setNameTableColumn(Map<String, TableColumn<ObservableList<Object>, String>> nameTableColumn) {
            this.nameTableColumn = nameTableColumn;
        }

        public void setObjectObservableList(ObservableList<Object> objectObservableList) {
            this.objectObservableList = objectObservableList;
        }

        @Override
        protected Task<List<String>> createTask() {
            return new Task<>() {
                @Override
                protected List<String> call() {
                    List<String> value = new ArrayList<>();
                    for (String name : primaryKey) {
                        int index = allColumns.indexOf(nameTableColumn.get(name));
                        value.add(objectObservableList.get(index).toString());
                    }
                    return value;
                }
            };
        }
    }

    public static class Suggestions extends Service<List<String>> {
        private SortedSet<String> strings;
        private String basedOn;
        private int spaceIndex;

        public void setSpaceIndex(int spaceIndex) {
            this.spaceIndex = spaceIndex;
        }

        public void setStrings(SortedSet<String> strings) {
            this.strings = strings;
        }

        public void setBasedOn(String basedOn) {
            this.basedOn = basedOn;
        }

        @Override
        protected Task<List<String>> createTask() {
            return new Task<>() {
                @Override
                protected List<String> call() {
                    StringBuilder leftSide = new StringBuilder();
                    StringBuilder rightSide = new StringBuilder();
                    for (int y = 0; y < basedOn.length(); y++) {
                        if (y < spaceIndex + 1) {
                            leftSide.append(basedOn.charAt(y));
                        }
                        if (y >= spaceIndex + 1) {
                            rightSide.append(basedOn.charAt(y));
                        }
                    }
                    basedOn = rightSide.toString();
                    updateMessage(leftSide.toString());
                    updateTitle(basedOn);

//                System.out.println(t1);


                    ArrayList<String> arrayList = new ArrayList<>(
                            strings.subSet(basedOn.toLowerCase(), basedOn.toLowerCase() + Character.MAX_VALUE));

                    arrayList.addAll(strings.subSet(basedOn.toUpperCase(), basedOn.toUpperCase() + Character.MAX_VALUE));
                    return arrayList;
                }
            };
        }
    }

    public static class FilterQueryProvider extends Service<Database.Column> {
        private String tableName;
        private String whereQuery;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public void setWhereQuery(String whereQuery) {
            this.whereQuery = whereQuery;
        }

        @Override
        protected Task<Database.Column> createTask() {
            return new Task<>() {
                @Override
                protected Database.Column call() throws Exception {
                    return database.filterQuery(tableName, whereQuery);
                }
            };
        }
    }

    public static class ChangeTableName extends Service<Boolean> {
        private String oldTableName;
        private String newTableName;

        public void setOldTableName(String oldTableName) {
            this.oldTableName = oldTableName;
        }

        public void setNewTableName(String newTableName) {
            this.newTableName = newTableName;
        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return database.changeTableName(oldTableName, newTableName);
                }
            };
        }
    }

    public static class BackupDB extends Service<Boolean> {
        private String databaseName;
        private File toBeSavedAt;

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public void setToBeSavedAt(File toBeSavedAt) {
            this.toBeSavedAt = toBeSavedAt;
        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return database.saveBackup(databaseName, toBeSavedAt);
                }
            };
        }
    }

    public static class LoadSavedDB extends Service<Boolean> {
        private String databaseName;
        private File toBeLoaded;

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public void setToBeLoaded(File toBeLoaded) {
            this.toBeLoaded = toBeLoaded;
        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return database.loadSavedDatabase(databaseName, toBeLoaded);
                }
            };
        }
    }

    public static class GetColumnNameAndType extends Service<Map<String, String>> {
        private String tableName;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        @Override
        protected Task<Map<String, String>> createTask() {
            return new Task<>() {
                @Override
                protected Map<String, String> call() throws Exception {
                    return database.nameAndTypeDESC(tableName);
                }
            };
        }
    }

    public static class ChangeTableColumnName extends Service<Integer> {
        private String tableName;
        private String oldColumnName;
        private String newColumnName;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public void setOldColumnName(String oldColumnName) {
            this.oldColumnName = oldColumnName;
        }

        public void setNewColumnName(String newColumnName) {
            this.newColumnName = newColumnName;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return database.changeColumnName(tableName, oldColumnName, newColumnName);
                }
            };
        }
    }

    public static class AddColumn extends Service<Integer> {
        private String tableName;
        private String dataType;
        private String newColumn;
        private boolean autoIncrement;
        private boolean notNull;
        private boolean primaryKey;
        private boolean unique;
        private Database.Column column;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public void setNewColumn(String newColumn) {
            this.newColumn = newColumn;
        }

        public void setAutoIncrement(boolean autoIncrement) {
            this.autoIncrement = autoIncrement;
        }

        public void setNotNull(boolean notNull) {
            this.notNull = notNull;
        }

        public void setPrimaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
        }

        public void setUnique(boolean unique) {
            this.unique = unique;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public void setColumn(Database.Column value) {
            column = value;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    int y = database.addColumn(tableName, dataType, newColumn, autoIncrement, notNull, primaryKey, unique);
                    for (List<Object> x : column.getColumn()) {
                        x.add(null);
                    }
                    return y;
                }
            };
        }
    }

    public static class GetColumnAutoIncrement extends Service<Integer> {
        private String tableName;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return database.getAutoIncrementColumn(tableName);
                }
            };
        }
    }

    public static class DeleteColumn extends Service<Integer> {
        private String tableName;
        private String columnName;
        private Database.Column column;
        private int z;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public void setColumn(Database.Column column) {
            this.column = column;
        }

        public void setZ(int z) {
            this.z = z;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    int y = database.deleteColumn(tableName, columnName);
                    for (List<Object> x : column.getColumn()) {
                        x.remove(z);
                    }
                    return y;
                }
            };
        }
    }

    public static class CreateUser extends Service<Integer> {
        private Pair<String, String> newUserDetails;

        public Pair<String, String> getNewUserDetails() {
            return newUserDetails;
        }

        public void setNewUserDetails(Pair<String, String> newUserDetails) {
            this.newUserDetails = newUserDetails;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return database.createUser(newUserDetails);
                }
            };
        }
    }

    public static class ShowUsers extends Service<ObservableList<String>> {
        @Override
        protected Task<ObservableList<String>> createTask() {
            return new Task<>() {
                @Override
                protected ObservableList<String> call() throws Exception {
                    return database.showUsers();
                }
            };
        }
    }

    public static class DropUser extends Service<Integer> {
        private String user_toBe_dropped;

        public String getUser_toBe_dropped() {
            return user_toBe_dropped;
        }

        public void setUser_toBe_dropped(String user_toBe_dropped) {
            this.user_toBe_dropped = user_toBe_dropped;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return database.dropUser(user_toBe_dropped);
                }
            };
        }
    }
}
