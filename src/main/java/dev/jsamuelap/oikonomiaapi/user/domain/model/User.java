package dev.jsamuelap.oikonomiaapi.user.domain.model;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

import lombok.Getter;

@Getter
public final class User {
  private final UUID id;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final String passwordHash;
  // En el dominio no agrego campos de autoria porque son detalles de la base de
  // datos
  // deletedAt si lo agrego porque tiene lógica de negocio
  private Instant deletedAt;

  private static final short MAX_FIRSTNAME_LENGTH = 100;
  private static final short MAX_LASTNAME_LENGTH = 150;
  private static final short MAX_EMAIL_LENGTH = 320;
  private static final short MAX_PASSWORD_HASH_LENGTH = 255;
  private static final Pattern EMAIL_PATTERN = Pattern
    .compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$");

  private User(UUID id, String firstName, String lastName, String email, String passwordHash, Instant deletedAt) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.passwordHash = passwordHash;
    this.deletedAt = deletedAt;
  }

  /**
   * Crear nuevo usuario. Valida reglas de dominio.
   */
  public static User register(String firstName, String lastName, String email, String password) {
    validateFirstName(firstName);
    validateLastName(lastName);
    validateEmail(email);
    validatePasswordHash(password);
    return new User(UUID.randomUUID(), firstName, lastName, email, password, null);
  }

  /**
   * Construir a partir de la persistencia. No valida reglas de dominio. Uso
   * exclusivo para el mapper de persistencia.
   */
  public static User reconstitute(UUID id, String firstName, String lastName, String email, String password,
    Instant deletedAt) {
    return new User(id, firstName, lastName, email, password, deletedAt);
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void delete() {
    if (isDeleted()) {
      throw new DomainException("El usuario ya está eliminado");
    }
    deletedAt = Instant.now();
  }

  private static void validateFirstName(String firstName) {
    if (firstName == null || firstName.isBlank()) {
      throw new DomainException("El nombre no puede estar vacío");
    }

    if (firstName.length() > MAX_FIRSTNAME_LENGTH) {
      throw new DomainException("El nombre no puede exceder más de %s caracteres".formatted(MAX_FIRSTNAME_LENGTH));
    }
  }

  private static void validateLastName(String lastName) {
    if (lastName == null || lastName.isBlank()) {
      throw new DomainException("Los apellidos no pueden estar vacíos");
    }

    if (lastName.length() > MAX_LASTNAME_LENGTH) {
      throw new DomainException("Los apellidos no pueden exceder más de %s caracteres".formatted(MAX_LASTNAME_LENGTH));
    }
  }

  private static void validateEmail(String email) {
    if (email == null || email.isBlank()) {
      throw new DomainException("El email no puede estar vacío");
    }

    if (email.length() > MAX_EMAIL_LENGTH) {
      throw new DomainException("El email no pueden exceder más de %s caracteres".formatted(MAX_EMAIL_LENGTH));
    }

    if (!EMAIL_PATTERN.matcher(email).matches()) {
      throw new DomainException("El formato del email no es válido");
    }
  }

  private static void validatePasswordHash(String passwordHash) {
    if (passwordHash == null || passwordHash.isBlank()) {
      throw new DomainException("El hash de la contraseña no puede estar vacío");
    }

    if (passwordHash.length() > MAX_PASSWORD_HASH_LENGTH) {
      throw new DomainException(
        "El hash de la contraseña no puede exceder más de %s caracteres".formatted(MAX_PASSWORD_HASH_LENGTH));
    }
  }
}
