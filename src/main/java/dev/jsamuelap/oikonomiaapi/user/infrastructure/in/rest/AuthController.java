package dev.jsamuelap.oikonomiaapi.user.infrastructure.in.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.AuthenticationException;
import dev.jsamuelap.oikonomiaapi.shared.security.config.SecurityProperties;
import dev.jsamuelap.oikonomiaapi.shared.security.jwt.AuthenticatedPrincipal;
import dev.jsamuelap.oikonomiaapi.shared.security.jwt.JwtCookies;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.AuthenticateUserUseCase;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.AuthenticationResult;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.LogoutUseCase;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.RefreshTokenUseCase;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.RegisterUserUseCase;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.TokenGeneratorPort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
  private final RegisterUserUseCase registerUserUseCase;
  private final AuthenticateUserUseCase authenticateUserUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final LogoutUseCase logoutUseCase;
  private final UserRestMapper mapper;
  private final TokenGeneratorPort tokenGenerator;
  private final SecurityProperties securityProperties;

  @PostMapping("/signup")
  public ResponseEntity<RegisterUserResponse> signup(@Valid @RequestBody final RegisterUserRequest request) {
    UUID userId = registerUserUseCase.registerUser(mapper.toCommand(request));
    return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterUserResponse(userId));
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody final AuthenticateUserRequest request) {
    AuthenticationResult result = authenticateUserUseCase.authenticate(mapper.toCommand(request));

    ResponseCookie cookie = buildRefreshCookie(result.refreshToken(), tokenGenerator.refreshTokenTtl().toSeconds());

    return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(new LoginResponse(result.accessToken()));
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refresh(
    @CookieValue(name = JwtCookies.REFRESH_TOKEN_COOKIE, required = false) String refreshTokenCookie) {
    if (refreshTokenCookie == null) {
      throw new AuthenticationException("No se encontró el refresh token");
    }

    AuthenticationResult result = refreshTokenUseCase.refresh(refreshTokenCookie);
    ResponseCookie cookie = buildRefreshCookie(result.refreshToken(), tokenGenerator.refreshTokenTtl().toSeconds());

    return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(new LoginResponse(result.accessToken()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    logoutUseCase.logout(principal.userId());

    ResponseCookie expiredCookie = buildRefreshCookie("", 0);

    return ResponseEntity.ok().header("Set-Cookie", expiredCookie.toString()).build();
  }

  private ResponseCookie buildRefreshCookie(String value, long maxAgeSeconds) {
    return ResponseCookie.from(JwtCookies.REFRESH_TOKEN_COOKIE, value).httpOnly(true)
      .secure(securityProperties.cookieSecure()).sameSite("Strict").path("/api/v1/auth").maxAge(maxAgeSeconds).build();
  }
}
