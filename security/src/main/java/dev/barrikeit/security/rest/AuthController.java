package dev.barrikeit.security.rest;

import dev.barrikeit.rest.Response;
import dev.barrikeit.security.rest.dto.JwtDto;
import dev.barrikeit.security.rest.dto.LoginDto;
import dev.barrikeit.security.rest.dto.RegisterDto;
import dev.barrikeit.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Authentication endpoints — login, register, logout, and token refresh.
 *
 * <p>Not a component-scanned {@code @RestController}: it is registered as a bean by {@code
 * SecurityAutoConfiguration} via {@code @ConditionalOnBean(AuthService)} and {@code
 * @ConditionalOnMissingBean(name="authController")}, so applications that expose their own {@code
 * authController} bean automatically suppress this default. {@code @ResponseBody} +
 * {@code @RequestMapping} keep it a valid MVC handler when registered as a plain bean.
 *
 * <p>The default base path is {@code /api/v1/auth}.
 */
@ResponseBody
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public Response<JwtDto> login(@Valid @RequestBody LoginDto dto) {
    return Response.ok(authService.login(dto));
  }

  @PostMapping("/register")
  public Response<String> register(@Valid @RequestBody RegisterDto dto) {
    return Response.ok(authService.register(dto));
  }

  @PostMapping("/logout")
  public Response<Void> logout(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    String token = header != null && header.startsWith("Bearer ")
        ? header.substring(7).trim()
        : "";
    authService.logout(token);
    return Response.noContent();
  }

  @PostMapping("/refresh")
  public Response<JwtDto> refresh(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String accessHeader,
      @RequestHeader("X-Refresh-Token") String refreshToken) {
    String accessToken =
        accessHeader != null && accessHeader.startsWith("Bearer ")
            ? accessHeader.substring(7).trim()
            : accessHeader;
    return Response.ok(authService.refresh(accessToken, refreshToken));
  }
}
