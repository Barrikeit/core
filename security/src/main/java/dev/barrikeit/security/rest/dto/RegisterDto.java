package dev.barrikeit.security.rest.dto;

import dev.barrikeit.validation.Alphanumeric;
import dev.barrikeit.validation.Password;
import dev.barrikeit.validation.Sanitize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {
  @Alphanumeric @NotBlank @Sanitize private String username;
  @Email @NotBlank private String email;
  @Password @NotBlank private String password;
}
