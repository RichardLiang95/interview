import java.sql.*;
import java.util.*;

public class DbComparator {

    public CompareResult compare(Db db1, Db db2) {
        // 获取两个数据库的表和列信息
        Map<String, List<ColumnInfo>> db1Schema = getDbSchema(db1);
        Map<String, List<ColumnInfo>> db2Schema = getDbSchema(db2);

        CompareResult compareResult = new CompareResult();
        List<TableDiff> tableDiffs = new ArrayList<>();
        List<ColumnDiff> columnDiffs = new ArrayList<>();

        // 比较表的差异
        Set<String> allTables = new HashSet<>(db1Schema.keySet());
        allTables.addAll(db2Schema.keySet());

        for (String tableName : allTables) {
            boolean inDb1 = db1Schema.containsKey(tableName);
            boolean inDb2 = db2Schema.containsKey(tableName);

            if (inDb1 && inDb2) {
                // 两个数据库都有此表，进一步比较列
                TableDiff tableDiff = new TableDiff();
                tableDiff.setName(tableName);

                List<ColumnInfo> db1Columns = db1Schema.get(tableName);
                List<ColumnInfo> db2Columns = db2Schema.get(tableName);

                if (db1Columns.equals(db2Columns)) {
                    tableDiff.setDiff(1); // 表完全相同
                } else {
                    tableDiff.setDiff(2); // 表存在不同
                    columnDiffs.addAll(compareColumns(tableName, db1Columns, db2Columns));
                }

                tableDiffs.add(tableDiff);
            } else if (inDb1) {
                // 只有db1有此表
                TableDiff tableDiff = new TableDiff();
                tableDiff.setName(tableName);
                tableDiff.setDiff(3);
                tableDiffs.add(tableDiff);
            } else {
                // 只有db2有此表
                TableDiff tableDiff = new TableDiff();
                tableDiff.setName(tableName);
                tableDiff.setDiff(4);
                tableDiffs.add(tableDiff);
            }
        }

        compareResult.setTableDiffs(tableDiffs);
        compareResult.setColumnDiffs(columnDiffs);

        return compareResult;
    }

    private List<ColumnDiff> compareColumns(String tableName, List<ColumnInfo> db1Columns, List<ColumnInfo> db2Columns) {
        List<ColumnDiff> columnDiffs = new ArrayList<>();
        Map<String, ColumnInfo> db1ColumnMap = new HashMap<>();
        Map<String, ColumnInfo> db2ColumnMap = new HashMap<>();

        for (ColumnInfo column : db1Columns) {
            db1ColumnMap.put(column.getName(), column);
        }

        for (ColumnInfo column : db2Columns) {
            db2ColumnMap.put(column.getName(), column);
        }

        Set<String> allColumns = new HashSet<>(db1ColumnMap.keySet());
        allColumns.addAll(db2ColumnMap.keySet());

        for (String columnName : allColumns) {
            boolean inDb1 = db1ColumnMap.containsKey(columnName);
            boolean inDb2 = db2ColumnMap.containsKey(columnName);

            ColumnDiff columnDiff = new ColumnDiff();
            columnDiff.setTable(tableName);
            columnDiff.setName(columnName);

            if (inDb1 && inDb2) {
                if (db1ColumnMap.get(columnName).equals(db2ColumnMap.get(columnName))) {
                    columnDiff.setDiff(1); // 字段完全相同
                } else {
                    columnDiff.setDiff(2); // 字段存在不同
                }
            } else if (inDb1) {
                columnDiff.setDiff(3); // 只有db1有此字段
            } else {
                columnDiff.setDiff(4); // 只有db2有此字段
            }

            columnDiffs.add(columnDiff);
        }

        return columnDiffs;
    }

    private Map<String, List<ColumnInfo>> getDbSchema(Db db) {
        Map<String, List<ColumnInfo>> schema = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(db.getUrl(), db.getUsername(), db.getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                List<ColumnInfo> columns = new ArrayList<>();

                ResultSet columnsResultSet = metaData.getColumns(null, null, tableName, "%");
                while (columnsResultSet.next()) {
                    ColumnInfo columnInfo = new ColumnInfo();
                    columnInfo.setName(columnsResultSet.getString("COLUMN_NAME"));
                    columnInfo.setType(columnsResultSet.getString("TYPE_NAME"));
                    columnInfo.setSize(columnsResultSet.getInt("COLUMN_SIZE"));
                    columnInfo.setNullable(columnsResultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    columnInfo.setDefaultValue(columnsResultSet.getString("COLUMN_DEF"));
                    columnInfo.setPrimaryKey(isPrimaryKey(metaData, tableName, columnInfo.getName()));
                    columnInfo.setAutoIncrement("YES".equals(columnsResultSet.getString("IS_AUTOINCREMENT")));
                    columns.add(columnInfo);
                }

                schema.put(tableName, columns);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schema;
    }

    private boolean isPrimaryKey(DatabaseMetaData metaData, String tableName, String columnName) throws SQLException {
        ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName);
        while (primaryKeys.next()) {
            if (primaryKeys.getString("COLUMN_NAME").equals(columnName)) {
                return true;
            }
        }
        return false;
    }
}



@Data
public class ColumnInfo {
    private String name;
    private String type;
    private int size;
    private boolean nullable;
    private String defaultValue;
    private boolean primaryKey;
    private boolean autoIncrement;

    // 重写equals和hashCode方法，用于比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnInfo that = (ColumnInfo) o;
        return size == that.size &&
                nullable == that.nullable &&
                primaryKey == that.primaryKey &&
                autoIncrement == that.autoIncrement &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, size, nullable, defaultValue, primaryKey, autoIncrement);
    }
}
