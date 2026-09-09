package dev.jsamuelap.oikonomiaapi.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

@DisplayName("User domain model")
public class UserTest {
  private static final String VALID_EMAIL = "test@example.com";
  private static final String VALID_PASSWORD_HASH = "hash123";

  @Nested
  @DisplayName("User registration")
  class Registration {
    @Test
    @DisplayName("Should register a valid user with all fields")
    void shouldRegisterValidUser() {
      User user = User.register("Samuel", "Aldana", VALID_EMAIL, VALID_PASSWORD_HASH);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(user).as("user").isNotNull();
        softly.assertThat(user.getFirstName()).as("firstName").isEqualTo("Samuel");
        softly.assertThat(user.getLastName()).as("lastName").isEqualTo("Aldana");
        softly.assertThat(user.getEmail()).as("email").isEqualTo(VALID_EMAIL);
        softly.assertThat(user.getPasswordHash()).as("passwordHash").isEqualTo(VALID_PASSWORD_HASH);
        softly.assertThat(user.isDeleted()).as("isDeleted").isFalse();
      });
    }

    @Nested
    @DisplayName("First name validation")
    class FirstName {
      @ParameterizedTest(name = "should reject: {0}")
      @DisplayName("Should reject blank first name")
      @NullAndEmptySource
      @ValueSource(strings = {" ", "   "})
      void shouldRejectBlankFirstName(String invalidFirstName) {
        assertThatThrownBy(() -> User.register(invalidFirstName, "Aldana", VALID_EMAIL, VALID_PASSWORD_HASH))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede estar vacío");
      }

      @Test
      @DisplayName("Should reject first name exceeding maximum length")
      void shouldRejectFirstNameExceedingMaxLength() {
        String tooLongFirstName = "a".repeat(User.MAX_FIRSTNAME_LENGTH + 1);
        assertThatThrownBy(() -> User.register(tooLongFirstName, "Aldana", VALID_EMAIL, VALID_PASSWORD_HASH))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede exceder");
      }

      @Test
      @DisplayName("Should accept first name at maximum length")
      void shouldAcceptFirstNameAtMaxLength() {
        String exactlyMaxFirstName = "a".repeat(User.MAX_FIRSTNAME_LENGTH);
        User user = User.register(exactlyMaxFirstName, "Aldana", VALID_EMAIL, VALID_PASSWORD_HASH);
        assertThat(user.getFirstName()).hasSize(User.MAX_FIRSTNAME_LENGTH);
      }
    }

    @Nested
    @DisplayName("Last name validation")
    class LastName {
      @ParameterizedTest(name = "should reject: {0}")
      @DisplayName("Should reject blank last name")
      @NullAndEmptySource
      @ValueSource(strings = {" ", "   "})
      void shouldRejectBlankLastName(String invalidLastName) {
        assertThatThrownBy(() -> User.register("Samuel", invalidLastName, VALID_EMAIL, VALID_PASSWORD_HASH))
          .isInstanceOf(DomainException.class).hasMessageContaining("no pueden estar vacíos");
      }

      @Test
      @DisplayName("Should reject last name exceeding maximum length")
      void shouldRejectLastNameExceedingMaxLength() {
        String tooLongLastName = "a".repeat(User.MAX_LASTNAME_LENGTH + 1);
        assertThatThrownBy(() -> User.register("Samuel", tooLongLastName, VALID_EMAIL, VALID_PASSWORD_HASH))
          .isInstanceOf(DomainException.class).hasMessageContaining("no pueden exceder");
      }

      @Test
      @DisplayName("Should accept last name at maximum length")
      void shouldAcceptLastNameAtMaxLength() {
        String exactlyMaxLastName = "a".repeat(User.MAX_LASTNAME_LENGTH);
        User user = User.register("Samuel", exactlyMaxLastName, VALID_EMAIL, VALID_PASSWORD_HASH);
        assertThat(user.getLastName()).hasSize(User.MAX_LASTNAME_LENGTH);
      }
    }

    @Nested
    @DisplayName("Email validation")
    class Email {
      @ParameterizedTest(name = "should reject: {0}")
      @DisplayName("Should reject blank email")
      @NullAndEmptySource
      @ValueSource(strings = {" ", "   "})
      void shouldRejectBlankEmail(String invalidEmail) {
        assertThatThrownBy(() -> User.register("Samuel", "Aldana", invalidEmail, VALID_PASSWORD_HASH))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede estar vacío");
      }

      @Test
      @DisplayName("Should reject email exceeding maximum length")
      void shouldRejectEmailExceedingMaxLength() {
        String tooLongEmail = "a".repeat(User.MAX_EMAIL_LENGTH + 1);
        assertThatThrownBy(() -> User.register("Samuel", "Aldana", tooLongEmail, VALID_PASSWORD_HASH))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede exceder");
      }

      @Test
      @DisplayName("Should accept email at maximum length")
      void shouldAcceptEmailAtMaxLength() {
        String exactlyMaxEmail = "a".repeat(User.MAX_EMAIL_LENGTH - 10) + "@email.com";
        User user = User.register("Samuel", "Aldana", exactlyMaxEmail, VALID_PASSWORD_HASH);
        assertThat(user.getEmail()).hasSize(User.MAX_EMAIL_LENGTH);
      }

      @ParameterizedTest(name = "should reject: {0}")
      @DisplayName("Should reject malformed email formats")
      @ValueSource(strings = {"test.com", "@email.com", "test@", "test@.com", "test@example.com "})
      void shouldRejectMalformedEmail(String invalidEmail) {
        assertThatThrownBy(() -> User.register("Samuel", "Aldana", invalidEmail, VALID_PASSWORD_HASH))
          .isInstanceOf(DomainException.class).hasMessageContaining("no es válido");
      }

      @ParameterizedTest(name = "should reject: {0}")
      @DisplayName("Should accept valid email formats")
      @ValueSource(strings = {VALID_EMAIL, "123@email.com", "test.name@example.com", "test+tag@example.com",
        "test_name@example.com", "test-name@example.com", "test@subdomain.example.com", "test@example.co",
        "test@example.travel", "TEST@EXAMPLE.COM"})
      void shouldAcceptValidEmail(String validEmail) {
        User user = User.register("Samuel", "Aldana", validEmail, VALID_PASSWORD_HASH);
        assertThat(user.getEmail()).isEqualTo(validEmail);
      }
    }

    @Nested
    @DisplayName("Password hash validation")
    class PasswordHash {
      @ParameterizedTest(name = "should reject: {0}")
      @DisplayName("Should reject blank password hash")
      @NullAndEmptySource
      @ValueSource(strings = {" ", "   "})
      void shouldRejectBlankPasswordHash(String invalidPasswordHash) {
        assertThatThrownBy(() -> User.register("Samuel", "Aldana", VALID_EMAIL, invalidPasswordHash))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede estar vacío");
      }

      @Test
      @DisplayName("Should reject password hash exceeding maximum length")
      void shouldRejectPasswordHashExceedingMaxLength() {
        String tooLongPasswordHash = "a".repeat(User.MAX_PASSWORD_HASH_LENGTH + 1);
        assertThatThrownBy(() -> User.register("Samuel", "Aldana", VALID_EMAIL, tooLongPasswordHash))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede exceder");
      }

      @Test
      @DisplayName("Should accept password hash at maximum length")
      void shouldAcceptPasswordHashAtMaxLength() {
        String exactlyMaxPasswordHash = "a".repeat(User.MAX_PASSWORD_HASH_LENGTH);
        User user = User.register("Samuel", "Aldana", VALID_EMAIL, exactlyMaxPasswordHash);
        assertThat(user.getPasswordHash()).hasSize(User.MAX_PASSWORD_HASH_LENGTH);
      }
    }
  }

  @Nested
  @DisplayName("User reconstitution from persistence")
  class Reconstitution {
    @Test
    @DisplayName("Should reconstitute user without re-validating fields")
    void shouldReconstituteWithoutRevalidating() {
      UUID id = UUID.randomUUID();
      User user = User.reconstitute(id, "", " ", "test @", "\t", null);
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(user).as("user").isNotNull();
        softly.assertThat(user.getId()).as("id").isEqualTo(id);
        softly.assertThat(user.getEmail()).as("email").isEqualTo("test @");
      });
    }

    @Test
    @DisplayName("Should reconstitute user with all fields")
    void shouldReconstituteWithAllFields() {
      UUID id = UUID.randomUUID();
      Instant deletedAt = Instant.now();
      User user = User.reconstitute(id, "Samuel", "Aldana", VALID_EMAIL, VALID_PASSWORD_HASH, deletedAt);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(user).as("user").isNotNull();
        softly.assertThat(user.getId()).as("id").isEqualTo(id);
        softly.assertThat(user.getFirstName()).as("firstName").isEqualTo("Samuel");
        softly.assertThat(user.getLastName()).as("lastName").isEqualTo("Aldana");
        softly.assertThat(user.getEmail()).as("email").isEqualTo(VALID_EMAIL);
        softly.assertThat(user.getPasswordHash()).as("passwordHash").isEqualTo(VALID_PASSWORD_HASH);
        softly.assertThat(user.getDeletedAt()).as("deletedAt").isEqualTo(deletedAt);
      });
    }
  }

  @Nested
  @DisplayName("User deletion")
  class Elimination {
    @Test
    @DisplayName("Should mark user as deleted")
    void shouldDeleteUser() {
      User user = User.register("Samuel", "Aldana", VALID_EMAIL, VALID_PASSWORD_HASH);
      user.delete();

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(user.isDeleted()).as("isDeleted").isTrue();
        softly.assertThat(user.getDeletedAt()).as("deletedAt").isNotNull();
      });
    }

    @Test
    @DisplayName("Should reject deleting an already deleted user")
    void shouldRejectDeletingAlreadyDeletedUser() {
      User user = User.register("Samuel", "Aldana", VALID_EMAIL, VALID_PASSWORD_HASH);
      user.delete();
      assertThatThrownBy(user::delete).isInstanceOf(DomainException.class).hasMessageContaining("ya está eliminado");
    }
  }
}
