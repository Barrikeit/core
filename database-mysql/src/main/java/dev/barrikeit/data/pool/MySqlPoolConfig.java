package dev.barrikeit.data.pool;

import com.zaxxer.hikari.HikariConfig;

public class MySqlPoolConfig extends ConnectionPoolConfig<MySqlPoolConfig> {

  public MySqlPoolConfig(String host, int port, String database, String username, String password) {
    super(host, port, database, username, password);
  }

  @Override
  protected String buildJdbcUrl() {
    return String.format(
        "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
        host, port, database);
  }

  @Override
  protected String driverClassName() {
    return "com.mysql.cj.jdbc.Driver";
  }

  @Override
  protected void applyDialectConfig(HikariConfig config) {
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.addDataSourceProperty("useServerPrepStmts", "true");
  }
}
