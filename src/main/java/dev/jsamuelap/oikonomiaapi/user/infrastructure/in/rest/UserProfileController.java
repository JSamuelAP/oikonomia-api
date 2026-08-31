package dev.jsamuelap.oikonomiaapi.user.infrastructure.in.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jsamuelap.oikonomiaapi.shared.security.jwt.AuthenticatedPrincipal;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {
  @GetMapping("/me")
  public ResponseEntity<AuthenticatedPrincipal> me(@AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    return ResponseEntity.ok(principal);
  }
}
