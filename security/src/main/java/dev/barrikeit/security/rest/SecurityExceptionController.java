package dev.barrikeit.security.rest;

import dev.barrikeit.web.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles Spring Security exceptions so they produce the same RFC 7807
 * {@code application/problem+json} shape as the rest of the application.
 *
 * <p>Lives in the {@code security} module (which has Spring Security on the classpath) rather than
 * in the {@code rest} module so that {@code rest} stays security-agnostic.
 *
 * <p>{@link SecurityExceptionHandler} delegates filter-level 401/403 responses here via
 * {@link org.springframework.web.servlet.HandlerExceptionResolver}.
 */
@Slf4j
@RestControllerAdvice
public class SecurityExceptionController {

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
    log.debug("AuthenticationException: {}", ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    problem.setType(URI.create("urn:error:unauthorized"));
    problem.setTitle("Unauthorized");
    problem.setInstance(URI.create(req.getRequestURI()));
    problem.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
    return problem;
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
    log.debug("AccessDeniedException: {}", ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    problem.setType(URI.create("urn:error:forbidden"));
    problem.setTitle("Forbidden");
    problem.setInstance(URI.create(req.getRequestURI()));
    problem.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
    return problem;
  }
}
