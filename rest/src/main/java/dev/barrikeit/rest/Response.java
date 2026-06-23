package dev.barrikeit.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.barrikeit.util.TimeUtil;
import dev.barrikeit.web.filter.CorrelationIdFilter;
import java.time.Instant;
import lombok.Getter;
import org.slf4j.MDC;

/**
 * Generic HTTP response envelope used by all REST controllers.
 *
 * <p>Every response carries a status string, an ISO-8601 timestamp, the request's correlation ID
 * (from the SLF4J MDC), optional metadata, and the actual response content.
 *
 * <p>Static factory methods cover the common cases:
 *
 * <pre>
 *   return Response.ok(dto);          // 200 with body
 *   return Response.created(dto);     // 201 with body
 *   return Response.noContent();      // 200 with no body (e.g. logout)
 * </pre>
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

  private final String status;
  private final Instant timestamp;
  private final String requestId;
  private final Object meta;
  private final T content;

  private Response(String status, T content, Object meta) {
    this.status = status;
    this.timestamp = TimeUtil.instantNow();
    this.requestId = MDC.get(CorrelationIdFilter.MDC_KEY);
    this.meta = meta;
    this.content = content;
  }

  public static <T> Response<T> ok(T content) {
    return new Response<>("OK", content, null);
  }

  public static <T> Response<T> ok(T content, Object meta) {
    return new Response<>("OK", content, meta);
  }

  public static <T> Response<T> created(T content) {
    return new Response<>("CREATED", content, null);
  }

  public static <T> Response<T> noContent() {
    return new Response<>("OK", null, null);
  }

  public static <T> Response<T> error(String message) {
    return new Response<>("ERROR", null, null);
  }
}
