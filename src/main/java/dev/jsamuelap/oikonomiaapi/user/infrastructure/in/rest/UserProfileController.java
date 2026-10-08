package dev.jsamuelap.oikonomiaapi.user.infrastructure.in.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jsamuelap.oikonomiaapi.shared.security.jwt.AuthenticatedPrincipal;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.GetUserUseCase;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.UserProfile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserProfileController {
  private final GetUserUseCase getUserUseCase;

  @GetMapping("/me")
  public ResponseEntity<UserProfile> me(@AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    UserProfile user = getUserUseCase.get(principal.userId());
    return ResponseEntity.ok(user);
  }
}
