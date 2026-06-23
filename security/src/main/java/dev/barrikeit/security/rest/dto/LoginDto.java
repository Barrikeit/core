package dev.barrikeit.security.rest.dto;

import dev.barrikeit.validation.Alphanumeric;
import dev.barrikeit.validation.SafeInput;
import dev.barrikeit.validation.Sanitize;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {
  @Alphanumeric @NotBlank @Sanitize private String username;
  @SafeInput @NotBlank @Sanitize private String password;
}
