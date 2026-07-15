package com.poe.provider.tool;

/**
 * SQLite 列元信息。
 */
class ColumnMeta {

    final int cid;
    final String name;
    final String type;
    final boolean notNull;
    final String defaultValue;
    final boolean isPk;
    final String fieldName;
    final String getterName;

    ColumnMeta(int cid, String name, String type, boolean notNull,
               String defaultValue, boolean isPk) {
        this.cid = cid;
        this.name = name;
        this.type = type;
        this.notNull = notNull;
        this.defaultValue = defaultValue;
        this.isPk = isPk;
        this.fieldName = snakeToCamel(name);
        this.getterName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    static String snakeToCamel(String s) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
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
