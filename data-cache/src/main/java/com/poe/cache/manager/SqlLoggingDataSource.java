package com.poe.cache.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;

/**
 * DataSource 代理，拦截所有 {@link PreparedStatement#execute()} / {@link Statement#execute(String)}
 * 调用并输出 DEBUG 级别 SQL 日志。
 *
 * <p>所有 DAO 通过 {@code DataSource.getConnection()} 获取连接，因此
 * 在连接池外包装一层即可覆盖全部 SQL 执行日志。
 */
public class SqlLoggingDataSource implements DataSource {

    final DataSource delegate;
    private final boolean enabled;

    SqlLoggingDataSource(DataSource delegate, boolean enabled) {
        this.delegate = delegate;
        this.enabled = enabled;
    }

    // ──── DataSource delegation ────

    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = delegate.getConnection();
        return enabled ? wrap(conn) : conn;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection conn = delegate.getConnection(username, password);
        return enabled ? wrap(conn) : conn;
    }

    @Override public PrintWriter getLogWriter() throws SQLException { return delegate.getLogWriter(); }
    @Override public void setLogWriter(PrintWriter out) throws SQLException { delegate.setLogWriter(out); }
    @Override public void setLoginTimeout(int seconds) throws SQLException { delegate.setLoginTimeout(seconds); }
    @Override public int getLoginTimeout() throws SQLException { return delegate.getLoginTimeout(); }
    @Override public java.util.logging.Logger getParentLogger() {
        return java.util.logging.Logger.getLogger(SqlLoggingDataSource.class.getName());
    }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException { return delegate.unwrap(iface); }
    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return delegate.isWrapperFor(iface); }

    // ──── factory ────

    /** 包装 DataSource，仅在非测试环境启用日志。 */
    static DataSource wrap(DataSource ds) {
        return new SqlLoggingDataSource(ds, !DatabaseManager.isTestEnvironment());
    }

    // ──── Connection proxy ────

    private Connection wrap(Connection conn) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new ConnectionHandler(conn));
    }

    private static class ConnectionHandler implements InvocationHandler {
        private final Connection delegate;

        ConnectionHandler(Connection delegate) { this.delegate = delegate; }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();

            if ("prepareStatement".equals(methodName) && args != null && args.length > 0 && args[0] instanceof String) {
                String sql = (String) args[0];
                PreparedStatement ps = (PreparedStatement) method.invoke(delegate, args);
                return wrapStatement(ps, sql);
            }

            if (("createStatement".equals(methodName) || "prepareCall".equals(methodName))
                    && (args == null || args.length == 0)) {
                Statement stmt = (Statement) method.invoke(delegate, args);
                return wrapStatement(stmt, null);
            }

            return method.invoke(delegate, args);
        }
    }

    // ──── Statement proxy ────

    private static Statement wrapStatement(Statement stmt, String preparedSql) {
        return (Statement) Proxy.newProxyInstance(
                Statement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class.isInstance(stmt)
                        ? PreparedStatement.class : Statement.class},
                new StatementHandler(stmt, preparedSql));
    }

    private static class StatementHandler implements InvocationHandler {
        private static final Logger log = LoggerFactory.getLogger("sql");

        private final Statement delegate;
        private final String preparedSql;

        StatementHandler(Statement delegate, String preparedSql) {
            this.delegate = delegate;
            this.preparedSql = preparedSql;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();

            boolean isExec = methodName.equals("execute")
                    || methodName.equals("executeQuery")
                    || methodName.equals("executeUpdate")
                    || methodName.equals("executeBatch")
                    || methodName.equals("executeLargeUpdate")
                    || methodName.equals("executeLargeBatch");

            if (isExec && log.isDebugEnabled()) {
                // PreparedStatement: use captured SQL; Statement: use arg
                String sql = preparedSql;
                if (sql == null && args != null && args.length > 0 && args[0] instanceof String) {
                    sql = (String) args[0];
                }
                sql = sql != null ? normalize(sql) : "<unknown>";

                long start = System.nanoTime();
                try {
                    Object result = method.invoke(delegate, args);
                    long ms = (System.nanoTime() - start) / 1_000_000;
                    log.debug("{} ms | {}", ms, sql);
                    return result;
                } catch (Exception e) {
                    long ms = (System.nanoTime() - start) / 1_000_000;
                    log.debug("{} ms | {}  [FAILED: {}]", ms, sql, e.getMessage());
                    throw e;
                }
            }

            return method.invoke(delegate, args);
        }

        private static String normalize(String sql) {
            return sql.replace('\n', ' ').replace('\r', ' ').replaceAll("\\s+", " ").trim();
        }
    }
}
