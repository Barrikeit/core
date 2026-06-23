package dev.barrikeit.data.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.barrikeit.exception.DatabaseException;
import dev.barrikeit.runtime.ConfigSource;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class HikariPoolConfig {

  private static final Logger log = Logger.getLogger(HikariPoolConfig.class.getName());
  private static final String DEFAULT_PREFIX = "datasource";

  private final String url;
  private final String username;
  private final String password;

  private final String driverClassName;
  private final String poolName;
  private final boolean autoCommit;
  private final int minimumIdle;
  private final int maximumPoolSize;
  private final long connectionTimeout;
  private final long idleTimeout;
  private final long maxLifetime;

  private HikariPoolConfig(Builder builder) {
    this.url = builder.url;
    this.username = builder.username;
    this.password = builder.password;
    this.driverClassName = builder.driverClassName;
    this.poolName = builder.poolName;
    this.autoCommit = builder.autoCommit;
    this.minimumIdle = builder.minimumIdle;
    this.maximumPoolSize = builder.maximumPoolSize;
    this.connectionTimeout = builder.connectionTimeout;
    this.idleTimeout = builder.idleTimeout;
    this.maxLifetime = builder.maxLifetime;
  }

  public static Builder from(ConfigSource config) {
    return from(config, DEFAULT_PREFIX);
  }

  public static Builder from(ConfigSource config, String prefix) {
    String p = prefix.endsWith(".") ? prefix : prefix + ".";
    String h = p + "hikari.";

    return new Builder()
        .url(config.require(p + "url"))
        .username(config.require(p + "username"))
        .password(config.require(p + "password"))
        .driverClassName(config.get(p + "driver-class-name", null))
        .poolName(config.get(h + "pool-name", "HikariPool"))
        .autoCommit(config.getBoolean(h + "auto-commit", false))
        .minimumIdle(config.getInt(h + "minimum-idle", 2))
        .maximumPoolSize(config.getInt(h + "maximum-pool-size", 10))
        .connectionTimeout(config.getLong(h + "connection-timeout", 30_000L))
        .idleTimeout(config.getLong(h + "idle-timeout", 600_000L))
        .maxLifetime(config.getLong(h + "max-lifetime", 1_800_000L));
  }

  public DataSource build() {
    HikariConfig config = new HikariConfig();

    config.setJdbcUrl(url);
    config.setUsername(username);
    config.setPassword(password);

    if (driverClassName != null && !driverClassName.isBlank()) {
      config.setDriverClassName(driverClassName);
    }

    config.setPoolName(poolName);
    config.setAutoCommit(autoCommit);
    config.setMinimumIdle(minimumIdle);
    config.setMaximumPoolSize(maximumPoolSize);
    config.setConnectionTimeout(connectionTimeout);
    config.setIdleTimeout(idleTimeout);
    config.setMaxLifetime(maxLifetime);

    try {
      log.info("Initialising connection pool [" + poolName + "] → " + url);
      return new HikariDataSource(config);
    } catch (Exception e) {
      throw new DatabaseException(
          "Failed to initialise connection pool [%s] → %s: %s", poolName, url, e.getMessage());
    }
  }

  public static final class Builder {
    private String url;
    private String username;
    private String password;

    private String driverClassName = null;
    private String poolName = "HikariPool";
    private boolean autoCommit = false;
    private int minimumIdle = 2;
    private int maximumPoolSize = 10;
    private long connectionTimeout = 30_000L;
    private long idleTimeout = 600_000L;
    private long maxLifetime = 1_800_000L;

    private Builder() {}

    public Builder url(String url) {
      this.url = url;
      return this;
    }

    public Builder username(String username) {
      this.username = username;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public Builder driverClassName(String driverClassName) {
      this.driverClassName = driverClassName;
      return this;
    }

    public Builder poolName(String poolName) {
      this.poolName = poolName;
      return this;
    }

    public Builder autoCommit(boolean autoCommit) {
      this.autoCommit = autoCommit;
      return this;
    }

    public Builder minimumIdle(int minimumIdle) {
      this.minimumIdle = minimumIdle;
      return this;
    }

    public Builder maximumPoolSize(int maximumPoolSize) {
      this.maximumPoolSize = maximumPoolSize;
      return this;
    }

    public Builder connectionTimeout(long ms) {
      this.connectionTimeout = ms;
      return this;
    }

    public Builder idleTimeout(long ms) {
      this.idleTimeout = ms;
      return this;
    }

    public Builder maxLifetime(long ms) {
      this.maxLifetime = ms;
      return this;
    }

    public HikariPoolConfig build() {
      if (url == null || url.isBlank()) throw new DatabaseException("datasource url is required");
      if (username == null) throw new DatabaseException("datasource username is required");
      if (password == null) throw new DatabaseException("datasource password is required");
      return new HikariPoolConfig(this);
    }

    public DataSource buildDataSource() {
      return build().build();
    }
  }
}
