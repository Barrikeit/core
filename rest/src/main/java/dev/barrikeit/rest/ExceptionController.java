package dev.barrikeit.rest;

import dev.barrikeit.exception.BadRequestException;
import dev.barrikeit.exception.ConflictException;
import dev.barrikeit.exception.ForbiddenException;
import dev.barrikeit.exception.NotFoundException;
import dev.barrikeit.exception.UnauthorizedException;
import dev.barrikeit.exception.ValidationException;
import dev.barrikeit.web.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler — translates application exceptions to RFC 7807 {@code
 * application/problem+json} responses.
 *
 * <p>Handles both application-level exceptions (from the {@code exception} module) and Spring
 * Security exceptions (401 / 403) so all error responses share a consistent structure.
 */
@Slf4j
@RestControllerAdvice
public class ExceptionController {

  // -------------------------------------------------------------------------
  // 400 — validation and bad request
  // -------------------------------------------------------------------------

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    log.debug("ValidationException: {}", ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setType(URI.create("urn:error:validation"));
    problem.setTitle("Validation failed");
    problem.setInstance(URI.create(req.getRequestURI()));
    problem.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
    problem.setProperty(
        "errors",
        ex.getBindingResult().getAllErrors().stream()
            .map(
                e -> {
                  String field = e instanceof FieldError fe ? fe.getField() : e.getObjectName();
                  Object rejected = e instanceof FieldError fe ? fe.getRejectedValue() : null;
                  return new FieldProblem(field, rejected, e.getDefaultMessage());
                })
            .toList());
    return problem;
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({BadRequestException.class, ValidationException.class})
  public ProblemDetail handleBadRequest(BadRequestException ex, HttpServletRequest req) {
    log.debug("BadRequestException: {}", ex.getFormattedMessage());
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setType(URI.create("urn:error:bad-request"));
    problem.setTitle(ex.getFormattedMessage());
    problem.setInstance(URI.create(req.getRequestURI()));
    problem.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
    return problem;
  }

  // -------------------------------------------------------------------------
  // 401 — authentication
  // -------------------------------------------------------------------------

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(UnauthorizedException.class)
  public ProblemDetail handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
    log.debug("UnauthorizedException: {}", ex.getFormattedMessage());
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    problem.setType(URI.create("urn:error:unauthorized"));
    problem.setTitle("Unauthorized");
    problem.setInstance(URI.create(req.getRequestURI()));
    problem.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
    return problem;
  }

  // -------------------------------------------------------------------------
  // 403 — authorization
  // -------------------------------------------------------------------------

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(ForbiddenException.class)
  public ProblemDetail handleForbidden(ForbiddenException ex, HttpServletRequest req) {
    log.debug("ForbiddenException: {}", ex.getFormattedMessage());
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    problem.setType(URI.create("urn:error:forbidden"));
    problem.setTitle("Forbidden");
    problem.setInstance(URI.create(req.getRequestURI()));
    problem.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
    return problem;
  }

  // -------------------------------------------------------------------------
  // 404 — not found
  // -------------------------------------------------------------------------

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFound(NotFoundException ex, HttpServletRequest req) {
    log.debug("NotFoundException: {}", ex.getFormattedMessage());
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setType(URI.create("urn:error:not-found"));
    problem.setTitle(ex.getFormattedMessage());
    problem.setInstance(URI.create(req.getRequestURI()));
    problem.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
    return problem;
  }

  // -------------------------------------------------------------------------
  // 409 — conflict
  // -------------------------------------------------------------------------

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(ConflictException.class)
  public ProblemDetail handleConflict(ConflictException ex, HttpServletRequest req) {
    log.debug("ConflictException: {}", ex.getFormattedMessage());
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problem.setType(URI.create("urn:error:conflict"));
    problem.setTitle(ex.getFormattedMessage());
    problem.setInstance(URI.create(req.getRequestURI()));
    problem.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
    return problem;
  }

  // -------------------------------------------------------------------------
  // 500 — unexpected
  // -------------------------------------------------------------------------

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest req) {
    log.error("UnhandledException", ex);
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problem.setType(URI.create("urn:error:internal"));
    problem.setTitle("Internal Server Error");
    problem.setInstance(URI.create(req.getRequestURI()));
    problem.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
    return problem;
  }

  // -------------------------------------------------------------------------
  // Inner record for field-level validation errors
  // -------------------------------------------------------------------------

  public record FieldProblem(String field, Object rejected, String message) {}
}
