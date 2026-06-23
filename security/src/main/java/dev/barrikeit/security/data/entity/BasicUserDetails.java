package dev.barrikeit.security.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Security-only representation of an authenticated user.
 *
 * <p>Carries everything Spring Security needs to make authorization decisions without exposing the
 * domain {@code User} entity beyond the security layer.
 *
 * <p>Child projects construct this from their {@code User} entity inside their {@code
 * UserDetailsService} implementation:
 *
 * <pre>
 *   return new BasicUserDetails(
 *       user.getId(), user.getUsername(), user.getPassword(),
 *       user.isEnabled(), user.isBanned(),
 *       getRoles(user), getAuthorities(user));
 * </pre>
 *
 * Roles vs authorities: roles — coarse-grained: "ADM", "USR", "AUD" authorities — fine-grained:
 * "ADM_SEC", "ADM_EV", "USR_RPT" formatted as ROLE_CODE + "_" + MODULE_CODE
 */
@Getter
public class BasicUserDetails implements UserDetails {

  @Serial private static final long serialVersionUID = 1L;

  private final UUID id;
  private final String username;

  @JsonIgnore private final String password;

  private final boolean enabled;
  private final boolean banned;
  private final Collection<? extends GrantedAuthority> roles;
  private final Collection<? extends GrantedAuthority> authorities;

  public BasicUserDetails(
      UUID id,
      String username,
      String password,
      boolean enabled,
      boolean banned,
      List<? extends GrantedAuthority> roles,
      List<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.enabled = enabled;
    this.banned = banned;
    this.roles = roles;
    this.authorities = authorities;
  }

  // -------------------------------------------------------------------------
  // Convenience extractors
  // -------------------------------------------------------------------------

  public List<String> getRolesNames() {
    return roles.stream().map(GrantedAuthority::getAuthority).filter(Objects::nonNull).toList();
  }

  public List<String> getAuthorityNames() {
    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .filter(Objects::nonNull)
        .toList();
  }

  // -------------------------------------------------------------------------
  // UserDetails contract
  // -------------------------------------------------------------------------

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return !banned;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  // -------------------------------------------------------------------------
  // equals / hashCode — identity based on UUID id
  // -------------------------------------------------------------------------

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BasicUserDetails that)) return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "BasicUserDetails{id=" + id + ", username='" + username + "', enabled=" + enabled + "}";
  }
}
