package dev.jsamuelap.oikonomiaapi.user.infrastructure.in.rest;

import static dev.jsamuelap.oikonomiaapi.shared.util.StringSanitizer.normalizeEmail;
import static dev.jsamuelap.oikonomiaapi.shared.util.StringSanitizer.trimOrNull;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "User account registration request")
public record RegisterUserRequest(
  // spotless:off
  @NotBlank(message = "El nombre es requerido")
  @Size(min = 2, max = 100, message = "El nombre debe tener entre {min} y {max} caracteres de longitud")
  @Schema(description = "User first name", example = "John")
  String firstName,

  @NotBlank(message = "Los apellidos son requeridos")
  @Size(min = 2, max = 150, message = "Los apellidos deben tener entre {min} y {max} caracteres de longitud")
  @Schema(description = "User last name", example = "Doe")
  String lastName,

  @NotBlank(message = "El email es requerido")
  @Email(message = "El email es inválido")
  @Schema(description = "User email", example = "john.doe@example.com")
  String email,

  @NotBlank(message = "La contraseña es requerida")
  @Size(min = 8, max = 100, message = "La contraseña debe tener entre {min} y {max} caracteres de longitud")
  @Pattern(
    regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
    message = "La contraseña debe tener al menos un digito, una letra minúscula, una letra mayúscula y un caracter especial"
  )
  @Schema(description = "User password (min 8 chars, must contain digit, lowercase, uppercase, special char)", example = "Password123!")
  String password
  // spotless:on
) {
  public RegisterUserRequest {
    email = normalizeEmail(email);
    firstName = trimOrNull(firstName);
    lastName = trimOrNull(lastName);
  }

  @Override
  public String toString() {
    return "CreateUserRequest[firstName=%s, lastName=%s, email=%s, password=****]".formatted(firstName, lastName,
      email);
  }
}
