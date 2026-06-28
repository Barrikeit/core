package dev.barrikeit.security.config;

import dev.barrikeit.security.rest.AuthController;
import dev.barrikeit.security.service.AuthService;
import dev.barrikeit.security.service.BasicUserDetailsService;
import dev.barrikeit.security.service.UserSessionService;
import dev.barrikeit.security.util.JwtUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Registers the generic authentication beans ({@link AuthService}, {@link AuthController}) as
 * opt-in, overridable defaults.
 *
 * <p>Because these are declared here (and {@code AuthService}/{@code AuthController} are no longer
 * component-scanned stereotypes), application code controls them declaratively rather than via
 * {@code @ComponentScan} exclude filters:
 *
 * <ul>
 *   <li>{@link AuthService} is created only when the application provides a {@link
 *       BasicUserDetailsService} bean and has not already defined a bean named {@code authService}.
 *       Applications with a richer auth flow simply expose their own {@code @Service} named {@code
 *       authService} and core's backs off. WebSocket apps with no {@code BasicUserDetailsService}
 *       never get it (so it can't fail on a missing dependency).
 *   <li>{@link AuthController} is created only when an {@link AuthService} bean exists and the
 *       application has not defined a bean named {@code authController}.
 * </ul>
 */
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

  @Bean
  @ConditionalOnBean(BasicUserDetailsService.class)
  @ConditionalOnMissingBean(name = "authService")
  public AuthService authService(
      SecurityProperties securityProperties,
      BasicUserDetailsService userDetailsService,
      UserSessionService userSessionService,
      JwtUtil jwtUtil) {
    return new AuthService(securityProperties, userDetailsService, userSessionService, jwtUtil);
  }

  @Bean
  @ConditionalOnBean(AuthService.class)
  @ConditionalOnMissingBean(name = "authController")
  public AuthController authController(AuthService authService) {
    return new AuthController(authService);
  }
}
