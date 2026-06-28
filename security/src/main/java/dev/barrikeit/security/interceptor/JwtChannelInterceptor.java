package dev.barrikeit.security.interceptor;

import dev.barrikeit.security.service.UserSessionService;
import dev.barrikeit.security.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * STOMP interceptor interceptor that authenticates the {@code CONNECT} frame with a Bearer JWT.
 *
 * <p>Only the {@code CONNECT} frame is validated — subsequent frames reuse the principal that this
 * interceptor sets on the STOMP session. On success the authenticated {@link
 * UsernamePasswordAuthenticationToken} is exposed to {@code @MessageMapping} methods via {@code
 * Principal}.
 *
 * <p>Registered as a {@code @Component} so WebSocket child projects can wire it into the client
 * inbound interceptor. Active only when {@code security.enabled=true} (or the property is missing).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.messaging.support.ChannelInterceptor")
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
public class JwtChannelInterceptor implements ChannelInterceptor {

  private final JwtUtil jwtUtil;
  private final UserSessionService userSessionService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    // Only intercept CONNECT — all other frames reuse the session principal
    if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
      return message;
    }

    log.debug("STOMP CONNECT — validating JWT");

    String jwt = extractToken(accessor);

    if (!StringUtils.hasText(jwt)) {
      log.warn("STOMP CONNECT rejected — no Authorization header");
      throw new MessagingException("Missing Authorization header");
    }

    try {
      String username = jwtUtil.extractUsername(jwt);
      validateActiveSession(jwt);

      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(username, null, jwtUtil.extractAuthorities(jwt));
      auth.setDetails(jwt);

      // principal now available in @MessageMapping via Principal
      accessor.setUser(auth);
      log.debug("STOMP CONNECT accepted — user: {}", username);
    } catch (ExpiredJwtException e) {
      log.warn("STOMP CONNECT rejected — JWT expired");
      throw new MessagingException("JWT Token expirado");
    } catch (SignatureException e) {
      log.warn("STOMP CONNECT rejected — invalid signature");
      throw new MessagingException("Token inválido");
    } catch (SessionAuthenticationException e) {
      log.warn("STOMP CONNECT rejected — session revoked");
      throw new MessagingException("Sesión no válida");
    }

    return message;
  }

  private void validateActiveSession(String jwt) {
    UUID userId = jwtUtil.extractUserId(jwt);
    String jti = jwtUtil.extractJti(jwt);
    if (!userSessionService.validateToken(userId, jti)) {
      throw new SessionAuthenticationException("Sesión no válida");
    }
  }

  private String extractToken(StompHeaderAccessor accessor) {
    String header = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
    return (StringUtils.hasText(header) && header.startsWith("Bearer "))
        ? header.substring(7)
        : null;
  }
}
