package com.poe.provider.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 从 SQLite 数据库读取表结构，自动生成对应的 Model 和 DAO 类。
 */
public class ModelDaoGenerator {

    private static final Logger log = LoggerFactory.getLogger(ModelDaoGenerator.class);

    private static final Map<String, String> SQL_TO_JAVA = Map.of(
            "INTEGER", "Integer",
            "INT", "Integer",
            "TEXT", "String",
            "REAL", "Double",
            "BLOB", "byte[]"
    );

    /** 列名 → 中文注释 的启发式映射 */
    private static final Map<String, String> COMMENT_MAP = new LinkedHashMap<>();

    static {
        COMMENT_MAP.put("id", "主键");
        COMMENT_MAP.put("name", "名称");
        COMMENT_MAP.put("desc", "描述");
        COMMENT_MAP.put("key", "键");
        COMMENT_MAP.put("value", "值");
        COMMENT_MAP.put("type", "类型");
        COMMENT_MAP.put("status", "状态");
        COMMENT_MAP.put("offset", "偏移量");
        COMMENT_MAP.put("total", "总数");
        COMMENT_MAP.put("format", "格式");
        COMMENT_MAP.put("scale", "小数位");
        COMMENT_MAP.put("delimiter", "分隔符");
        COMMENT_MAP.put("file", "文件");
        COMMENT_MAP.put("url", "URL");
        COMMENT_MAP.put("version", "版本");
        COMMENT_MAP.put("count", "数量");
        COMMENT_MAP.put("level", "等级");
        COMMENT_MAP.put("index", "序号");
        COMMENT_MAP.put("order", "排序");
        COMMENT_MAP.put("source", "来源");
        COMMENT_MAP.put("target", "目标");
        COMMENT_MAP.put("english", "英文");
        COMMENT_MAP.put("chinese", "中文");
        COMMENT_MAP.put("create_time", "创建时间");
        COMMENT_MAP.put("update_time", "更新时间");
        COMMENT_MAP.put("time", "时间");
        COMMENT_MAP.put("sync", "同步");
        COMMENT_MAP.put("synced", "已同步");
    }

    private final String dbPath;
    private final Path modelDir;
    private final Path daoDir;
    private final Set<String> tableFilter;

    public ModelDaoGenerator(String dbPath, Path outputDir, Set<String> tableFilter) {
        this.dbPath = dbPath;
        this.modelDir = outputDir.resolve("com/poe/cache/model");
        this.daoDir = outputDir.resolve("com/poe/cache/dao");
        this.tableFilter = tableFilter;
    }

    public void generate() throws Exception {
        log.info("Reading schema from: {}", dbPath);

        Files.createDirectories(modelDir);
        Files.createDirectories(daoDir);

        List<TableMeta> tables = readSchema();

        for (TableMeta table : tables) {
            log.info("Generating model: {}", table.className);
            writeFile(modelDir.resolve(table.className + ".java"), generateModel(table));

            log.info("Generating dao: {}Dao", table.className);
            writeFile(daoDir.resolve(table.className + "Dao.java"), generateDao(table));
        }

        log.info("Done. Generated {} models and {} DAOs.", tables.size(), tables.size());
    }

    // ─── read schema ───

    private List<TableMeta> readSchema() throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        List<TableMeta> tables = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url)) {
            List<String> tableNames = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type='table'"
                                 + " AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'items_fts%'"
                                 + " ORDER BY name")) {
                while (rs.next()) {
                    tableNames.add(rs.getString("name"));
                }
            }

            for (String tableName : tableNames) {
                if (tableFilter != null && !tableFilter.contains(tableName)) {
                    log.info("Skipping table: {}", tableName);
                    continue;
                }
                List<ColumnMeta> columns = new ArrayList<>();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("PRAGMA table_info(\"" + tableName + "\")")) {
                    while (rs.next()) {
                        columns.add(new ColumnMeta(
                                rs.getInt("cid"),
                                rs.getString("name"),
                                rs.getString("type"),
                                rs.getInt("notnull") != 0,
                                rs.getObject("dflt_value") == null ? null : rs.getString("dflt_value"),
                                rs.getInt("pk") != 0
                        ));
                    }
                }
                tables.add(new TableMeta(tableName, columns));
            }
        }
        return tables;
    }

    // ─── model generation ───

    private String generateModel(TableMeta table) {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.poe.cache.model;\n\n");
        sb.append("import java.time.LocalDateTime;\n");
        sb.append("import java.time.format.DateTimeFormatter;\n\n");
        sb.append("/**\n * ").append(table.tableName)
                .append(" — 自动生成的模型类。\n */\n");
        sb.append("public class ").append(table.className).append(" {\n\n");
        sb.append("    private static final DateTimeFormatter FMT = "
                + "DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss\");\n\n");

        // fields
        for (ColumnMeta col : table.columns) {
            sb.append("    private ").append(sqlToJava(col.type))
                    .append(" ").append(col.fieldName).append(";\n");
        }

        // default constructor
        sb.append("\n    public ").append(table.className).append("() {}\n\n");

        // convenience constructor (non-PK, non-timestamp NOT NULL columns)
        List<ColumnMeta> ctorCols = table.columns.stream()
                .filter(c -> !c.isPk && c.notNull
                        && !"create_time".equals(c.name) && !"update_time".equals(c.name))
                .collect(Collectors.toList());
        if (!ctorCols.isEmpty()) {
            sb.append("    public ").append(table.className).append("(");
            sb.append(ctorCols.stream()
                    .map(c -> sqlToJava(c.type) + " " + c.fieldName)
                    .collect(Collectors.joining(", ")));
            sb.append(") {\n");
            for (ColumnMeta col : ctorCols) {
                sb.append("        this.").append(col.fieldName)
                        .append(" = ").append(col.fieldName).append(";\n");
            }
            if (table.hasColumn("create_time") || table.hasColumn("update_time")) {
                sb.append("        String now = FMT.format(LocalDateTime.now());\n");
                if (table.hasColumn("create_time")) sb.append("        this.createTime = now;\n");
                if (table.hasColumn("update_time")) sb.append("        this.updateTime = now;\n");
            }
            sb.append("    }\n\n");
        }

        // getters & setters
        for (ColumnMeta col : table.columns) {
            sb.append("    /** ").append(colComment(col.name)).append(" */\n");
            sb.append("    public ").append(sqlToJava(col.type))
                    .append(" get").append(col.getterName).append("() { return ")
                    .append(col.fieldName).append("; }\n");
            sb.append("    public void set").append(col.getterName).append("(")
                    .append(sqlToJava(col.type)).append(" ").append(col.fieldName)
                    .append(") { this.").append(col.fieldName).append(" = ")
                    .append(col.fieldName).append("; }\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    // ─── DAO generation ───

    private String generateDao(TableMeta table) {
        String cn = table.className;
        StringBuilder sb = new StringBuilder();
        sb.append("package com.poe.cache.dao;\n\n");
        sb.append("import com.poe.cache.model.").append(cn).append(";\n\n");
        sb.append("import javax.sql.DataSource;\n");
        sb.append("import java.sql.*;\n");
        sb.append("import java.util.ArrayList;\n");
        sb.append("import java.util.List;\n");
        sb.append("import java.util.Optional;\n\n");
        sb.append("/**\n * ").append(table.tableName)
                .append(" 数据访问对象（自动生成）。\n */\n");
        sb.append("public class ").append(cn).append("Dao extends BaseDao<").append(cn).append("> {\n\n");

        List<ColumnMeta> nonPk = table.nonPkColumns();

        // constructor
        sb.append("    public ").append(cn).append("Dao(DataSource dataSource) {\n");
        sb.append("        super(dataSource);\n    }\n\n");

        // tableName
        appendTableName(sb, table);
        // insertColumns
        appendInsertColumns(sb, nonPk);
        // bindInsertParams
        appendBindInsertParams(sb, cn, nonPk);
        // setEntityId
        appendSetEntityId(sb, cn);
        // mapRow
        appendMapRow(sb, table, cn);
        // update
        appendUpdate(sb, table, cn, nonPk);

        sb.append("}\n");
        return sb.toString();
    }

    private void appendTableName(StringBuilder sb, TableMeta table) {
        sb.append("\n    @Override\n");
        sb.append("    protected String tableName() {\n");
        sb.append("        return \"").append(table.tableName).append("\";\n    }\n");
    }

    private void appendInsertColumns(StringBuilder sb, List<ColumnMeta> nonPk) {
        sb.append("\n    @Override\n");
        sb.append("    protected String insertColumns() {\n");
        sb.append("        return \"");
        sb.append(nonPk.stream().map(c -> c.name).collect(Collectors.joining(", ")));
        sb.append("\";\n    }\n");
    }

    private void appendBindInsertParams(StringBuilder sb, String cn, List<ColumnMeta> nonPk) {
        sb.append("\n    @Override\n");
        sb.append("    protected void bindInsertParams(PreparedStatement ps, ").append(cn)
                .append(" entity) throws SQLException {\n");
        for (int i = 0; i < nonPk.size(); i++) {
            sb.append("        ").append(setterCall(nonPk.get(i), "entity", i + 1)).append(";\n");
        }
        sb.append("    }\n");
    }

    private void appendSetEntityId(StringBuilder sb, String cn) {
        sb.append("\n    @Override\n");
        sb.append("    protected void setEntityId(").append(cn).append(" entity, int id) {\n");
        sb.append("        entity.setId(id);\n    }\n");
    }

    private void appendUpdate(StringBuilder sb, TableMeta table, String cn, List<ColumnMeta> nonPk) {
        List<ColumnMeta> updatable = nonPk.stream()
                .filter(c -> !c.isPk).collect(Collectors.toList());
        if (updatable.isEmpty()) return;
        sb.append("\n    public void update(").append(cn).append(" entity) throws SQLException {\n");
        sb.append("        String sql = \"UPDATE ").append(table.tableName).append(" SET ");
        sb.append(updatable.stream().map(c -> c.name + " = ?")
                .collect(Collectors.joining(", ")));
        sb.append(" WHERE id = ?\";\n");
        sb.append("        try (Connection conn = dataSource.getConnection();\n");
        sb.append("             PreparedStatement ps = conn.prepareStatement(sql)) {\n");
        int idx = 1;
        for (ColumnMeta c : updatable) {
            sb.append("            ").append(setterCall(c, "entity", idx)).append(";\n");
            idx++;
        }
        sb.append("            ps.setInt(").append(idx).append(", entity.getId());\n");
        sb.append("            ps.executeUpdate();\n");
        sb.append("        }\n    }\n\n");
    }

    private void appendMapRow(StringBuilder sb, TableMeta table, String cn) {
        sb.append("\n    @Override\n");
        sb.append("    protected ").append(cn).append(" mapRow(ResultSet rs) throws SQLException {\n");
        sb.append("        ").append(cn).append(" entity = new ").append(cn).append("();\n");
        for (ColumnMeta col : table.columns) {
            sb.append("        entity.set").append(col.getterName).append("(");
            String javaType = sqlToJava(col.type);
            if ("Integer".equals(javaType)) {
                sb.append("rs.getInt(\"").append(col.name).append("\")");
            } else if ("Double".equals(javaType)) {
                sb.append("rs.getDouble(\"").append(col.name).append("\")");
            } else {
                sb.append("rs.getString(\"").append(col.name).append("\")");
            }
            sb.append(");\n");
        }
        sb.append("        return entity;\n    }\n\n");
    }

    // ─── helpers ───

    private String setterCall(ColumnMeta col, String var, int idx) {
        String type = sqlToJava(col.type);
        String getter = "get" + col.getterName;
        if ("Integer".equals(type)) {
            return "ps.setInt(" + idx + ", " + var + "." + getter + "())";
        } else if ("Double".equals(type)) {
            return "ps.setDouble(" + idx + ", " + var + "." + getter + "())";
        }
        return "ps.setString(" + idx + ", " + var + "." + getter + "())";
    }

    static String sqlToJava(String sqlType) {
        if (sqlType == null) return "String";
        String upper = sqlType.toUpperCase().trim().replaceAll("\\(.*\\)", "");
        return SQL_TO_JAVA.getOrDefault(upper, "String");
    }

    static String colComment(String colName) {
        if (COMMENT_MAP.containsKey(colName)) {
            return COMMENT_MAP.get(colName);
        }
        for (Map.Entry<String, String> entry : COMMENT_MAP.entrySet()) {
            String suffix = "_" + entry.getKey();
            if (colName.endsWith(suffix)) {
                String prefix = colName.substring(0, colName.length() - suffix.length());
                return prefix + entry.getValue();
            }
        }
        return colName;
    }

    private void writeFile(Path path, String content) throws IOException {
        Files.writeString(path, content);
        log.debug("  wrote: {}", path);
    }
}
