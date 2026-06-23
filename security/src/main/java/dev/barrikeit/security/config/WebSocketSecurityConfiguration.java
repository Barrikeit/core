package dev.barrikeit.security.config;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Abstract base security configuration for STOMP-over-WebSocket applications.
 *
 * <p>Unlike {@link SecurityConfiguration} (the REST base), authentication here is performed at the
 * STOMP interceptor level by {@link dev.barrikeit.security.interceptor.JwtChannelInterceptor} on the {@code
 * CONNECT} frame — so no JWT servlet filter is added to the HTTP chain. This base only secures the
 * HTTP surface (handshake endpoint, health/version endpoints, security headers, CORS) and enables
 * {@code @PreAuthorize} on {@code @MessageMapping} methods.
 *
 * <p>Child projects extend this class, add {@code @Configuration}, {@code @EnableWebSecurity} and
 * {@code @EnableMethodSecurity}, and implement {@link #apiPath()}:
 *
 * <pre>
 * {@literal @}Configuration
 * {@literal @}EnableWebSecurity
 * {@literal @}EnableMethodSecurity
 * {@literal @}Import(SecurityExceptionHandler.class)
 * public class AppWebSocketSecurityConfiguration extends WebSocketSecurityConfiguration {
 *
 *     public AppWebSocketSecurityConfiguration(SecurityProperties props,
 *                                              SecurityExceptionHandler handler) {
 *         super(props, handler);
 *     }
 *
 *     {@literal @}Override
 *     protected String apiPath() {
 *         return serverProperties.getServlet().getApiPath();
 *     }
 * }
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
public abstract class WebSocketSecurityConfiguration {

  protected final SecurityProperties securityProperties;
  protected final SecurityExceptionHandler exceptionHandler;

  /** The servlet API path prefix that the WS handshake and HTTP endpoints are mounted under. */
  protected abstract String apiPath();

  /**
   * URL patterns accessible without authentication. Defaults to the WS handshake plus the public,
   * error and version endpoints under {@link #apiPath()}. Override to customize.
   */
  protected String[] publicEndpoints() {
    String apiPath = apiPath();
    return new String[] {
      apiPath + "/ws/**",
      apiPath + "/public/**",
      apiPath + "/error/**",
      apiPath + "/error",
      apiPath + "/version/**",
      apiPath + "/version"
    };
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    log.warn("WebSocket security configuration active");
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .headers(
            headers ->
                headers
                    .frameOptions(HeadersFrame -> HeadersFrame.sameOrigin())
                    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                    .addHeaderWriter(
                        new StaticHeadersWriter(
                            "Referrer-Policy", "strict-origin-when-cross-origin"))
                    .addHeaderWriter(
                        new StaticHeadersWriter(
                            "Permissions-Policy",
                            "camera=(), microphone=(), geolocation=()"))
                    .addHeaderWriter(
                        new StaticHeadersWriter(
                            "Cross-Origin-Opener-Policy", "same-origin")))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(exceptionHandler)
                    .accessDeniedHandler(exceptionHandler))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(publicEndpoints())
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    SecurityProperties.CorsProperties cors = securityProperties.getCors();

    if (cors != null && Boolean.TRUE.equals(cors.getEnabled()) && cors.getAllowed() != null) {
      configuration.setAllowedOriginPatterns(splitTrimmed(cors.getAllowed().getOrigins()));
      configuration.setAllowedMethods(splitTrimmed(cors.getAllowed().getMethods()));
      configuration.setAllowedHeaders(splitTrimmed(cors.getAllowed().getHeaders()));
      configuration.setAllowCredentials(true);
    }

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    String pattern =
        (cors != null && cors.getPath() != null && cors.getPath().getPattern() != null)
            ? cors.getPath().getPattern()
            : "/**";
    source.registerCorsConfiguration(pattern, configuration);
    return source;
  }

  private static List<String> splitTrimmed(String csv) {
    if (csv == null) {
      return List.of();
    }
    return Arrays.stream(csv.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
  }
}
