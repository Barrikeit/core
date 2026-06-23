package dev.barrikeit.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Delegates Spring Security authentication and authorization exceptions to Spring MVC's {@link
 * HandlerExceptionResolver}.
 *
 * <p>This allows security exceptions (401 Unauthorized, 403 Forbidden) to be handled by the same
 * {@code @ControllerAdvice} / {@code @ExceptionHandler} mechanism used for all other application
 * exceptions — producing consistent JSON error responses instead of Spring Security's default HTML
 * error pages.
 *
 * <p>Usage in a security configuration:
 *
 * <pre>
 *   http.exceptionHandling(ex -> ex
 *       .authenticationEntryPoint(exceptionHandler)
 *       .accessDeniedHandler(exceptionHandler));
 * </pre>
 */
@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

  private final HandlerExceptionResolver resolver;

  @Autowired
  public SecurityExceptionHandler(
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
    this.resolver = resolver;
  }

  /** Called when an unauthenticated request hits a protected endpoint (401). */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) {
    resolver.resolveException(request, response, null, authException);
  }

  /** Called when an authenticated user lacks the required authority (403). */
  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException) {
    resolver.resolveException(request, response, null, accessDeniedException);
  }
}
