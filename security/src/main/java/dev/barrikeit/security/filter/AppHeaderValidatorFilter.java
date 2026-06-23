package dev.barrikeit.security.filter;

import dev.barrikeit.security.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that validates the custom application-identity header on incoming requests.
 *
 * <p>When {@link SecurityProperties.AppValidatorFilterProperties#getAppHeaderNameValidationFilter()}
 * is true, the configured header must be present and equal to {@code appSelfName}; otherwise the
 * request is rejected with {@code 400 Bad Request}. Header-free endpoints (public, error, version by
 * default) bypass the check.
 *
 * <p>Not a Spring bean — instantiate it from a security configuration with the project's API path
 * prefix:
 *
 * <pre>
 *   http.addFilterBefore(
 *       new AppHeaderValidatorFilter(apiPath, securityProperties.getAppValidatorFilter()),
 *       JwtFilter.class);
 * </pre>
 */
@Slf4j
public class AppHeaderValidatorFilter extends OncePerRequestFilter {

  private static final List<String> DEFAULT_HEADER_FREE_PREFIXES =
      List.of("/public", "/error", "/version");

  private final String apiPath;
  private final SecurityProperties.AppValidatorFilterProperties appValidatorFilterProperties;
  private final List<String> headerFreePrefixes;

  public AppHeaderValidatorFilter(
      String apiPath,
      SecurityProperties.AppValidatorFilterProperties appValidatorFilterProperties) {
    this(apiPath, appValidatorFilterProperties, DEFAULT_HEADER_FREE_PREFIXES);
  }

  public AppHeaderValidatorFilter(
      String apiPath,
      SecurityProperties.AppValidatorFilterProperties appValidatorFilterProperties,
      List<String> headerFreePrefixes) {
    this.apiPath = apiPath == null ? "" : apiPath;
    this.appValidatorFilterProperties = appValidatorFilterProperties;
    this.headerFreePrefixes = headerFreePrefixes;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    String requestURI = request.getRequestURI();
    String contextPath = request.getContextPath();
    String endpoint = requestURI.substring(contextPath.length() + apiPath.length());

    log.debug("requestURI={} | contextPath={} | apiPath={}", requestURI, contextPath, apiPath);

    if (isHeaderFreeEndpoint(endpoint)) {
      log.debug("Request to endpoint {} ALLOWED (public endpoint)", requestURI);
      filterChain.doFilter(request, response);
      return;
    }

    if (appValidatorFilterProperties != null
        && Boolean.TRUE.equals(appValidatorFilterProperties.getAppHeaderNameValidationFilter())) {
      String calledAppId = request.getHeader(appValidatorFilterProperties.getAppHeaderName());
      if (calledAppId == null
          || calledAppId.isBlank()
          || !calledAppId.equals(appValidatorFilterProperties.getAppSelfName())) {
        log.debug("Request from app {} to endpoint {} REJECTED", calledAppId, requestURI);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      log.debug("Request from app {} to endpoint {} ACCEPTED", calledAppId, requestURI);
    }

    filterChain.doFilter(request, response);
  }

  private boolean isHeaderFreeEndpoint(String endpoint) {
    return headerFreePrefixes.stream().anyMatch(endpoint::startsWith);
  }
}
