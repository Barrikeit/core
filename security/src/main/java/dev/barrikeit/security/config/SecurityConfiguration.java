package dev.barrikeit.security.config;

import dev.barrikeit.security.filter.JwtFilter;
import dev.barrikeit.security.filter.RateLimitingFilter;
import dev.barrikeit.security.service.UserSessionService;
import dev.barrikeit.security.util.JwtUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Abstract base security configuration for stateless JWT REST APIs.
 *
 * <p>Child projects extend this class and implement {@link #publicEndpoints()} to declare which
 * paths are accessible without authentication. All other paths require a valid JWT.
 *
 * <p>Example:
 *
 * <pre>
 * {@literal @}Configuration
 * {@literal @}EnableWebSecurity
 * public class AppSecurityConfiguration extends SecurityConfiguration {
 *
 *     public AppSecurityConfiguration(SecurityProperties props,
 *                                     SecurityExceptionHandler handler,
 *                                     JwtUtil jwtUtil,
 *                                     UserSessionService sessionService) {
 *         super(props, handler, jwtUtil, sessionService);
 *     }
 *
 *     {@literal @}Override
 *     protected String[] publicEndpoints() {
 *         return new String[]{"/api/v1/auth/**", "/actuator/health", "/actuator/info"};
 *     }
 * }
 * </pre>
 */
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public abstract class SecurityConfiguration {

  protected final SecurityProperties securityProperties;
  protected final SecurityExceptionHandler exceptionHandler;
  protected final JwtUtil jwtUtil;
  protected final UserSessionService userSessionService;

  /**
   * Declares the URL patterns that are accessible without authentication. Override to add
   * application-specific public paths (Swagger, health checks, auth endpoints, etc.).
   */
  protected abstract String[] publicEndpoints();

  /**
   * Declares the URL patterns that require ADMIN authority. Defaults to empty — override to
   * restrict specific paths (e.g. {@code /actuator/**}).
   */
  protected String[] adminEndpoints() {
    return new String[0];
  }

  /**
   * Declares the URI suffixes that are subject to rate limiting. Defaults to the three auth
   * endpoints — override to customize.
   */
  protected List<String> rateLimitedEndpoints() {
    return List.of("/auth/login", "/auth/register", "/auth/refresh");
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .cors(c -> c.configurationSource(corsConfigurationSource()))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(exceptionHandler)
                    .accessDeniedHandler(exceptionHandler))
        .authorizeHttpRequests(
            auth -> {
              auth.requestMatchers(publicEndpoints()).permitAll();
              if (adminEndpoints().length > 0) {
                auth.requestMatchers(adminEndpoints()).hasAuthority("ADMIN");
              }
              auth.anyRequest().authenticated();
            })
        .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(rateLimitingFilter(), JwtFilter.class);

    return http.build();
  }

  @Bean
  public JwtFilter jwtFilter() {
    return new JwtFilter(jwtUtil, userSessionService);
  }

  @Bean
  public RateLimitingFilter rateLimitingFilter() {
    SecurityProperties.RateLimitProperties rl = securityProperties.getRateLimit();
    if (rl == null) {
      return new RateLimitingFilter(false, 5, 5, 1, rateLimitedEndpoints());
    }
    return new RateLimitingFilter(
        rl.isEnabled(),
        rl.getCapacity(),
        rl.getRefillTokens(),
        rl.getRefillPeriodMinutes(),
        rateLimitedEndpoints());
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    SecurityProperties.CorsProperties cors = securityProperties.getCors();
    CorsConfiguration config = new CorsConfiguration();

    if (cors != null && Boolean.TRUE.equals(cors.getEnabled())) {
      if (cors.getAllowed() != null) {
        if (cors.getAllowed().getOrigins() != null) {
          config.addAllowedOrigin(cors.getAllowed().getOrigins());
        }
        if (cors.getAllowed().getMethods() != null) {
          for (String m : cors.getAllowed().getMethods().split(",")) {
            config.addAllowedMethod(m.trim());
          }
        }
        if (cors.getAllowed().getHeaders() != null) {
          config.addAllowedHeader(cors.getAllowed().getHeaders());
        }
      }
    } else {
      config.addAllowedOrigin("*");
      config.addAllowedMethod("*");
      config.addAllowedHeader("*");
    }

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    String pattern =
        (cors != null && cors.getPath() != null && cors.getPath().getPattern() != null)
            ? cors.getPath().getPattern()
            : "/**";
    source.registerCorsConfiguration(pattern, config);
    return source;
  }
}
