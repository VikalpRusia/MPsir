package controller.serviceProvider;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TableColumn;
import model.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Services {
    private Services() {
    }

    static Database database;

    public static void setDatabase(Database database) {
        Services.database = database;
    }

    public static class PrimarykeyService extends Service<List<String>> {
        String tableName;

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
        String databaseName;

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
        String tableName;

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
        String databaseName;

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
        String tableName;

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
        String databaseName;

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
        String tableName;
        List<String> columnsName;
        List<String> columnsType;
        List<String> primaryKeys;

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

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return database.createTable(
                            tableName,
                            columnsName,
                            columnsType,
                            primaryKeys
                    );
                }
            };
        }
    }

    public static class InsertData extends Service<Integer> {
        String tableName;
        List<Object> values;

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
        String tableName;
        List<String> values;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return database.deleteData(tableName, values);
                }
            };
        }
    }

    public static class UpdateData extends Service<Integer> {
        String tableName;
        String columnModified;
        String newValue;
        List<String> values;

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
                            columnModified, newValue, values);
                }
            };
        }
    }

    public static class DescAll extends Service<ObservableList<ObservableList<String>>>{
        String tableName;

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

    public static class PrimaryKeyValueProvider extends Service<List<String>>{
        Map<String, TableColumn<ObservableList<Object>,String>> nameTableColumn;
        ObservableList<TableColumn<ObservableList<Object>,?>> allColumns;
        ObservableList<Object> objectObservableList;
        List<String> primaryKey;

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
                protected List<String> call() throws Exception {
                    List<String> value = new ArrayList<>();
                    for (String name:primaryKey) {
                        int index = allColumns.indexOf(nameTableColumn.get(name));
                        value.add(objectObservableList.get(index).toString());
                    }
                    return value;
                }
            };
        }
    }
}