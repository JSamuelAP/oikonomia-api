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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Profile", description = "Authenticated user profile endpoints")
public class UserProfileController {
  private final GetUserUseCase getUserUseCase;

  @GetMapping("/me")
  @Operation(summary = "Get authenticated user profile", description = "Retrieves the profile of the currently authenticated user")
  @ApiResponse(responseCode = "200", description = "User profile retrieved successfully")
  public ResponseEntity<UserProfile> me(
    @Parameter(hidden = true) @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    UserProfile user = getUserUseCase.get(principal.userId());
    return ResponseEntity.ok(user);
  }
}
