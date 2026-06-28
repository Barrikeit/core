package dev.barrikeit.security.interceptor;

import dev.barrikeit.security.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * STOMP interceptor interceptor that validates the custom application-identity header on the {@code
 * CONNECT} frame, mirroring {@link dev.barrikeit.security.filter.AppHeaderValidatorFilter} for the
 * servlet world.
 *
 * <p>Behaviour is driven by {@link SecurityProperties.AppValidatorFilterProperties}: when {@code
 * appHeaderNameValidationFilter} is true the configured header must be present and equal to {@code
 * appSelfName}, otherwise the CONNECT is rejected.
 *
 * <p>Active only when {@code security.enabled=true} (or the property is missing).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.messaging.support.ChannelInterceptor")
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
public class AppHeaderValidatorInterceptor implements ChannelInterceptor {

  private final SecurityProperties securityProperties;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
      return message;
    }

    SecurityProperties.AppValidatorFilterProperties cfg =
        securityProperties.getAppValidatorFilter();

    if (cfg == null || !Boolean.TRUE.equals(cfg.getAppHeaderNameValidationFilter())) {
      return message;
    }

    String calledAppId = accessor.getFirstNativeHeader(cfg.getAppHeaderName());

    if (calledAppId == null || calledAppId.isBlank() || !calledAppId.equals(cfg.getAppSelfName())) {
      log.debug("STOMP CONNECT rejected — invalid app header: {}", calledAppId);
      throw new MessagingException("App header inválido — esperado: " + cfg.getAppSelfName());
    }

    log.debug("STOMP CONNECT app header OK — app: {}", calledAppId);
    return message;
  }
}
