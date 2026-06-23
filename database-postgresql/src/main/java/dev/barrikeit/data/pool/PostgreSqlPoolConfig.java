package dev.barrikeit.data.pool;

public class PostgreSqlPoolConfig extends ConnectionPoolConfig<PostgreSqlPoolConfig> {

  public PostgreSqlPoolConfig(
      String host, int port, String database, String username, String password) {
    super(host, port, database, username, password);
  }

  @Override
  protected String buildJdbcUrl() {
    return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
  }

  @Override
  protected String driverClassName() {
    return "org.postgresql.Driver";
  }
}
