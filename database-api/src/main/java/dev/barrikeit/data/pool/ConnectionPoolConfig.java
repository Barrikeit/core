package dev.barrikeit.data.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.barrikeit.exception.DatabaseException;
import java.util.logging.Logger;
import javax.sql.DataSource;

public abstract class ConnectionPoolConfig<T extends ConnectionPoolConfig<T>> {

  protected final Logger log = Logger.getLogger(getClass().getName());

  protected final String host;
  protected final int port;
  protected final String database;
  protected final String username;
  protected final String password;

  private int maximumPoolSize = 10;
  private int minimumIdle = 2;
  private long connectionTimeout = 30_000L;
  private long idleTimeout = 600_000L;
  private long maxLifetime = 1_800_000L;

  protected ConnectionPoolConfig(
      String host, int port, String database, String username, String password) {
    this.host = host;
    this.port = port;
    this.database = database;
    this.username = username;
    this.password = password;
  }

  protected abstract String buildJdbcUrl();

  protected abstract String driverClassName();

  protected void applyDialectConfig(HikariConfig config) {}

  public DataSource build() {
    HikariConfig config = new HikariConfig();

    config.setJdbcUrl(buildJdbcUrl());
    config.setDriverClassName(driverClassName());
    config.setUsername(username);
    config.setPassword(password);
    config.setMaximumPoolSize(maximumPoolSize);
    config.setMinimumIdle(minimumIdle);
    config.setConnectionTimeout(connectionTimeout);
    config.setIdleTimeout(idleTimeout);
    config.setMaxLifetime(maxLifetime);
    config.setPoolName(getClass().getSimpleName() + "-pool");

    applyDialectConfig(config);

    try {
      log.info("Initialising connection pool [" + config.getPoolName() + "] → " + buildJdbcUrl());
      return new HikariDataSource(config);
    } catch (Exception e) {
      throw new DatabaseException(
          "Failed to initialise connection pool for '%s': %s", buildJdbcUrl(), e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  public T maximumPoolSize(int n) {
    this.maximumPoolSize = n;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T minimumIdle(int n) {
    this.minimumIdle = n;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T connectionTimeout(long ms) {
    this.connectionTimeout = ms;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T idleTimeout(long ms) {
    this.idleTimeout = ms;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T maxLifetime(long ms) {
    this.maxLifetime = ms;
    return (T) this;
  }
}
