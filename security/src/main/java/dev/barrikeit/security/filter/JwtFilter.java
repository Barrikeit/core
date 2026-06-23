package dev.barrikeit.security.filter;

import dev.barrikeit.security.service.UserSessionService;
import dev.barrikeit.security.util.JwtUtil;
import dev.barrikeit.security.util.TokenType;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates the JWT Bearer token on every request and populates the {@link
 * org.springframework.security.core.context.SecurityContext}.
 *
 * <p>Skips requests that carry no Authorization header. On a valid, active token it builds a {@link
 * UsernamePasswordAuthenticationToken} from the token's claims and sets it in the security context.
 * On an invalid or revoked token the filter clears the context and lets the request continue — the
 * downstream {@link org.springframework.security.web.access.intercept.AuthorizationFilter} will
 * reject it with 401.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtUtil jwtUtil;
  private final UserSessionService userSessionService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      chain.doFilter(request, response);
      return;
    }

    String token = header.substring(BEARER_PREFIX.length()).trim();
    try {
      UUID userId = jwtUtil.extractUserId(token);
      String jti = jwtUtil.extractJti(token);

      if (!userSessionService.validateToken(userId, jti)) {
        log.debug("JWT token revoked for userId={} jti={}", userId, jti);
        SecurityContextHolder.clearContext();
        chain.doFilter(request, response);
        return;
      }

      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        var authorities =
            jwtUtil.extractAuthorities(token).stream()
                .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                .toList();
        var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    } catch (JwtException ex) {
      log.debug("Invalid JWT token: {}", ex.getMessage());
      SecurityContextHolder.clearContext();
    }

    chain.doFilter(request, response);
  }
}
