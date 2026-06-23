package dev.barrikeit.data.pool;

import com.zaxxer.hikari.HikariConfig;

public class H2PoolConfig extends ConnectionPoolConfig<H2PoolConfig> {

  public enum Mode {
    IN_MEMORY,
    FILE
  }

  private final Mode mode;

  public H2PoolConfig(String database) {
    super("localhost", 0, database, "sa", "");
    this.mode = Mode.IN_MEMORY;
  }

  public H2PoolConfig(Mode mode, String database, String username, String password) {
    super("localhost", 0, database, username, password);
    this.mode = mode;
  }

  @Override
  protected String buildJdbcUrl() {
    return switch (mode) {
      case IN_MEMORY ->
          "jdbc:h2:mem:" + database + ";DB_CLOSE_DELAY=-1" + ";DB_CLOSE_ON_EXIT=FALSE";
      case FILE -> "jdbc:h2:file:" + database;
    };
  }

  @Override
  protected String driverClassName() {
    return "org.h2.Driver";
  }

  @Override
  protected void applyDialectConfig(HikariConfig config) {
    config.setMaximumPoolSize(5);
    config.setMinimumIdle(1);
  }
}
