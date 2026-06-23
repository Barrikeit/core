package dev.barrikeit.security.rest.dto;

import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JwtDto {
  private String token;
  private String refreshToken;
  private Date expireAt;
  private Date expireRefreshAt;
}
