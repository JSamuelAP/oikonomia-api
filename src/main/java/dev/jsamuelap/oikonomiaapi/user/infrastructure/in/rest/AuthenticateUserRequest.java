package dev.jsamuelap.oikonomiaapi.user.infrastructure.in.rest;

import static dev.jsamuelap.oikonomiaapi.shared.util.StringSanitizer.normalizeEmail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticateUserRequest(
  @NotBlank(message = "El email es requerido") @Email(message = "El email es inválido") String email,

  @NotBlank(message = "La contraseña es requerida") @Size(max = 255, message = "La contraseña debe tener máximo {max} caracteres de longitud") String password) {
  public AuthenticateUserRequest {
    email = normalizeEmail(email);
  }

  @Override
  public String toString() {
    return "AuthenticateUserRequest[email=%s]".formatted(email);
  }
}
