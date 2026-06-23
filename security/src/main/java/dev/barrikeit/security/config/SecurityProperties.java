package dev.barrikeit.security.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code security.*} block from application.yml / application.properties.
 *
 * <p>Enable in child projects by adding:
 *
 * <pre>
 *   {@literal @}EnableConfigurationProperties(SecurityProperties.class)
 * </pre>
 *
 * or via {@code @Import} in a security configuration class.
 *
 * <p>Example YAML:
 *
 * <pre>
 * security:
 *   enabled: true
 *   max-concurrent-sessions: 3
 *   jwt:
 *     issuer: my-app
 *     secret: my-secret
 *     expiration: 3600
 *     expiration-refresh: 86400
 *   cors:
 *     enabled: true
 *     path:
 *       pattern: /**
 *     allowed:
 *       origins: http://localhost:4200
 *       methods: GET,POST,PUT,DELETE,OPTIONS
 *       headers: "*"
 *   app-validator-filter:
 *     app-header-name-validation-filter: true
 *     app-header-name: X-App-Name
 *     app-self-name: my-app
 * </pre>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "security", ignoreUnknownFields = false)
public class SecurityProperties {

  private Boolean enabled;
  private Integer maxConcurrentSessions;
  private CorsProperties cors;
  private JwtProperties jwt;
  private AppValidatorFilterProperties appValidatorFilter;
  private RateLimitProperties rateLimit;

  @Getter
  @Setter
  public static class JwtProperties {
    @NotBlank private String issuer;

    @NotBlank private String secret;

    @Min(0)
    private long expiration;

    @Min(0)
    private long expirationRefresh;
  }

  @Getter
  @Setter
  public static class CorsProperties {
    private Boolean enabled;
    private Allowed allowed;
    private Path path;

    @Getter
    @Setter
    public static class Allowed {
      private String origins;
      private String methods;
      private String headers;
    }

    @Getter
    @Setter
    public static class Path {
      private String pattern;
    }
  }

  @Getter
  @Setter
  public static class AppValidatorFilterProperties {
    private Boolean appHeaderNameValidationFilter;
    private String appHeaderName;
    private String appSelfName;
    private String appSecurityName;
  }

  @Getter
  @Setter
  public static class RateLimitProperties {
    private boolean enabled = true;
    private int capacity = 5;
    private int refillTokens = 5;
    private int refillPeriodMinutes = 1;
  }
}
