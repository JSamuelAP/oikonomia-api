package dev.jsamuelap.oikonomiaapi.user.infrastructure.in.rest;

import static dev.jsamuelap.oikonomiaapi.shared.util.StringSanitizer.normalizeEmail;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User login request")
public record AuthenticateUserRequest(
  // spotless:off
  @NotBlank(message = "El email es requerido")
  @Email(message = "El email es inválido")
  @Schema(description = "User email", example = "john.doe@example.com")
  String email,

  @NotBlank(message = "La contraseña es requerida")
  @Size(max = 255, message = "La contraseña debe tener máximo {max} caracteres de longitud")
  @Schema(description = "User password", example = "Password123!")
  String password
  // spotless:on
) {
  public AuthenticateUserRequest {
    email = normalizeEmail(email);
  }

  @Override
  public String toString() {
    return "AuthenticateUserRequest[email=%s]".formatted(email);
  }
}
