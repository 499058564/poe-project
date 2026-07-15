package com.poe.provider.tool;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SQLite 表元信息。
 */
class TableMeta {

    final String tableName;
    final String className;
    final List<ColumnMeta> columns;

    TableMeta(String tableName, List<ColumnMeta> columns) {
        this.tableName = tableName;
        this.className = snakeToPascal(tableName);
        this.columns = columns;
    }

    boolean hasColumn(String name) {
        return columns.stream().anyMatch(c -> c.name.equals(name));
    }

    List<ColumnMeta> nonPkColumns() {
        return columns.stream().filter(c -> !c.isPk).collect(Collectors.toList());
    }

    static String snakeToPascal(String s) {
        StringBuilder sb = new StringBuilder();
        boolean upper = true;
        for (char ch : s.toCharArray()) {
            if (ch == '_') {
                upper = true;
            } else if (upper) {
                sb.append(Character.toUpperCase(ch));
                upper = false;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
