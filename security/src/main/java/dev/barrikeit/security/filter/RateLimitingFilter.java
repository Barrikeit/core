package dev.barrikeit.security.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Per-IP rate limiter applied to authentication endpoints.
 *
 * <p>Uses Bucket4j token-bucket algorithm. Each client IP gets its own bucket. When the bucket
 * runs dry the filter short-circuits with HTTP 429 and does not continue the filter chain.
 *
 * <p>The list of rate-limited URI suffixes and the bucket parameters are injected at construction
 * time so child projects can configure them from {@code SecurityProperties.RateLimitProperties}.
 */
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

  private final boolean enabled;
  private final int capacity;
  private final int refillTokens;
  private final int refillPeriodMinutes;
  private final List<String> rateLimitedSuffixes;
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  public RateLimitingFilter(
      boolean enabled,
      int capacity,
      int refillTokens,
      int refillPeriodMinutes,
      List<String> rateLimitedSuffixes) {
    this.enabled = enabled;
    this.capacity = capacity;
    this.refillTokens = refillTokens;
    this.refillPeriodMinutes = refillPeriodMinutes;
    this.rateLimitedSuffixes = rateLimitedSuffixes;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    if (!enabled || !isRateLimited(request.getRequestURI())) {
      chain.doFilter(request, response);
      return;
    }

    String ip = resolveClientIp(request);
    Bucket bucket = buckets.computeIfAbsent(ip, this::newBucket);

    if (bucket.tryConsume(1)) {
      chain.doFilter(request, response);
    } else {
      log.warn("Rate limit exceeded for IP={} uri={}", ip, request.getRequestURI());
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response
          .getWriter()
          .write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded\"}");
    }
  }

  private boolean isRateLimited(String uri) {
    return rateLimitedSuffixes.stream().anyMatch(uri::endsWith);
  }

  private Bucket newBucket(String ip) {
    return Bucket.builder()
        .addLimit(
            Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillTokens, Duration.ofMinutes(refillPeriodMinutes))
                .build())
        .build();
  }

  private String resolveClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
