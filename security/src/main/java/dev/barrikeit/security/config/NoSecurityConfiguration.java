package dev.barrikeit.security.config;

import java.util.logging.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration that disables all authentication and authorization.
 *
 * <p>Active only when {@code security.enabled=false} in application config.
 *
 * <p>Intended for: - Local development - Integration tests - Deployments where security is handled
 * externally (API Gateway, VPN, etc.)
 *
 * <p>Child projects activate this automatically by depending on the {@code security} module and
 * setting {@code security.enabled=false} — no extra configuration needed.
 */
@Configuration
@ConditionalOnProperty(name = "security.enabled", havingValue = "false")
public class NoSecurityConfiguration {

  private static final Logger log = Logger.getLogger(NoSecurityConfiguration.class.getName());

  @Bean
  SecurityFilterChain noSecurity(HttpSecurity http) throws Exception {
    log.warning("Security is DISABLED — all requests are permitted without authentication");
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }
}
