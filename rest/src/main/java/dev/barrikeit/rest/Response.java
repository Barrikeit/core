package dev.barrikeit.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.barrikeit.util.TimeUtil;
import dev.barrikeit.web.filter.CorrelationIdFilter;
import java.time.Instant;
import lombok.Getter;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

/**
 * Generic HTTP response envelope used by all REST controllers.
 *
 * <p>Every response carries a status string, an ISO-8601 timestamp, the request's correlation ID
 * (from the SLF4J MDC), an optional human-readable message, optional metadata, and the actual
 * response content.
 *
 * <p>Static factory methods cover the common cases:
 *
 * <pre>
 *   return Response.ok(dto);                       // 200 with body
 *   return Response.ok(dto, meta);                 // 200 with body + metadata (e.g. paging)
 *   return Response.created(dto);                  // 201 with body
 *   return Response.noContent();                   // no body
 *   return Response.noContent("Deleted");          // no body + message
 *   return Response.error("Boom");                 // error with message
 *   return Response.error(HttpStatus.FORBIDDEN, "Disabled");
 * </pre>
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

  private final String status;
  private final Instant timestamp;
  private final String requestId;
  private final String message;
  private final Object meta;
  private final T content;

  private Response(String status, String message, T content, Object meta) {
    this.status = status;
    this.timestamp = TimeUtil.instantNow();
    this.requestId = MDC.get(CorrelationIdFilter.MDC_KEY);
    this.message = message;
    this.meta = meta;
    this.content = content;
  }

  public static <T> Response<T> ok(T content) {
    return new Response<>("OK", null, content, null);
  }

  public static <T> Response<T> ok(T content, Object meta) {
    return new Response<>("OK", null, content, meta);
  }

  public static <T> Response<T> created(T content) {
    return new Response<>("CREATED", null, content, null);
  }

  public static <T> Response<T> noContent() {
    return new Response<>("OK", null, null, null);
  }

  public static <T> Response<T> noContent(String message) {
    return new Response<>("OK", message, null, null);
  }

  public static <T> Response<T> error(String message) {
    return new Response<>("ERROR", message, null, null);
  }

  public static <T> Response<T> error(HttpStatus status, String message) {
    return new Response<>(status != null ? status.name() : "ERROR", message, null, null);
  }
}
