package dev.barrikeit.security.service;

import dev.barrikeit.exception.UnauthorizedException;
import dev.barrikeit.security.data.entity.BasicUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Abstract base {@link UserDetailsService} for JWT-based authentication.
 *
 * <p>Child projects extend this class and implement {@link #loadDetails(String)} (load by
 * username/email from the app DB) and {@link #loadDetailsByCode(UUID)} (load by primary key UUID).
 * The authentication and registration logic is provided here.
 *
 * <p>Example:
 *
 * <pre>
 * {@literal @}Service
 * public class AppUserDetailsService extends BasicUserDetailsService {
 *
 *     private final UserRepository userRepository;
 *
 *     public AppUserDetailsService(PasswordEncoder encoder, UserRepository repo) {
 *         super(encoder);
 *         this.userRepository = repo;
 *     }
 *
 *     {@literal @}Override
 *     protected BasicUserDetails loadDetails(String username) {
 *         User user = userRepository.findByUsername(username)
 *             .orElseThrow(() -> new UsernameNotFoundException(username));
 *         return toUserDetails(user);
 *     }
 *
 *     {@literal @}Override
 *     protected BasicUserDetails loadDetailsByCode(UUID id) {
 *         User user = userRepository.findById(id)
 *             .orElseThrow(() -> new UsernameNotFoundException(id.toString()));
 *         return toUserDetails(user);
 *     }
 * }
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BasicUserDetailsService implements UserDetailsService {

  protected final PasswordEncoder passwordEncoder;

  // -------------------------------------------------------------------------
  // Abstract contract — implement in child project
  // -------------------------------------------------------------------------

  /**
   * Loads the user's security details by username (or email, depending on the app).
   *
   * @throws UsernameNotFoundException if not found
   */
  protected abstract BasicUserDetails loadDetails(String username);

  /**
   * Loads the user's security details by their UUID primary key.
   *
   * @throws UsernameNotFoundException if not found
   */
  protected abstract BasicUserDetails loadDetailsByCode(UUID userId);

  // -------------------------------------------------------------------------
  // UserDetailsService contract
  // -------------------------------------------------------------------------

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return loadDetails(username);
  }

  /**
   * Convenience method used by {@link AuthService} during token refresh to reload an up-to-date
   * {@link BasicUserDetails} from the database without requiring a password.
   */
  public BasicUserDetails loadUserByCode(UUID userId) {
    return loadDetailsByCode(userId);
  }

  // -------------------------------------------------------------------------
  // Authentication
  // -------------------------------------------------------------------------

  /**
   * Verifies the supplied raw password against the stored encoded password.
   *
   * @throws UnauthorizedException if the credentials are invalid
   */
  public BasicUserDetails authenticate(String username, String rawPassword) {
    BasicUserDetails details;
    try {
      details = loadDetails(username);
    } catch (UsernameNotFoundException ex) {
      throw new UnauthorizedException("exception.auth.bad-credentials");
    }

    if (!passwordEncoder.matches(rawPassword, details.getPassword())) {
      throw new UnauthorizedException("exception.auth.bad-credentials");
    }
    if (!details.isEnabled()) {
      throw new UnauthorizedException("exception.auth.account-disabled");
    }
    if (!details.isAccountNonLocked()) {
      throw new UnauthorizedException("exception.auth.account-locked");
    }
    return details;
  }
}
