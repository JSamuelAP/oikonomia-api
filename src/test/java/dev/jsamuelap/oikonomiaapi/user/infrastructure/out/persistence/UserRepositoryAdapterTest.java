package dev.jsamuelap.oikonomiaapi.user.infrastructure.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import dev.jsamuelap.oikonomiaapi.support.IntegrationTestSupport;
import dev.jsamuelap.oikonomiaapi.user.domain.model.User;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.UserRepositoryPort;

@DisplayName("User repository adapter")
public class UserRepositoryAdapterTest extends IntegrationTestSupport {
  @Autowired
  private UserRepositoryPort userRepository;

  @Nested
  @DisplayName("Save user")
  class Save {
    @Test
    @DisplayName("Should persist all fields")
    void shouldPersistsAllFields() {
      User user = newUser("Sam", "sam@example.com", "hash123");

      User found = userRepository.findById(user.getId()).orElseThrow();
      assertThat(found).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    @DisplayName("Should reject duplicate email")
    void shouldRejectDuplicateEmail() {
      newUser("Sam", "sam@example.com", "hash1");

      assertThatThrownBy(() -> newUser("Manuel", "sam@example.com", "hash2"))
        .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should reject email with uppercase")
    void shouldRejectEmailWithUppercase() {
      assertThatThrownBy(() -> newUser("Sam", "Sam@Example.com", "hash123"))
        .isInstanceOf(DataIntegrityViolationException.class);
    }
  }

  @Nested
  @DisplayName("Soft delete user")
  class SoftDelete {
    @Test
    @DisplayName("Should persist deleted at when inserted already deleted")
    void shouldPersistDeletedAtWhenInsertedAlreadyDeleted() {
      Instant deletedAt = Instant.now();
      User deleted = User.reconstitute(UUID.randomUUID(), "Sam", "Aldana", "sam@example.com", "hash123", deletedAt);
      userRepository.save(deleted);

      User found = userRepository.findById(deleted.getId()).orElseThrow();
      assertThat(found.isDeleted()).isTrue();
      assertThat(found.getDeletedAt()).isCloseTo(deletedAt, within(1, ChronoUnit.MILLIS));
    }

    @Test
    @DisplayName("Should persist deletion of an existing user")
    void shouldPersistDeletionOfAnExistingUser() {
      User user = newUser("Sam", "sam@example.com", "hash123");

      user.delete();
      userRepository.save(user);

      User found = userRepository.findById(user.getId()).orElseThrow();
      assertThat(found.isDeleted()).isTrue();
    }
  }

  @Nested
  @DisplayName("User queries")
  class Queries {
    @Test
    @DisplayName("Should find by email")
    void shouldFindByEmail() {
      User user = newUser("Sam", "sam@example.com", "hash123");

      assertThat(userRepository.findByEmail("sam@example.com")).isPresent().get().extracting(User::getId)
        .isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void shouldReturnEmptyWhenEmailDoesNotExist() {
      assertThat(userRepository.findByEmail("nobody@example.com")).isEmpty();
    }

    @Test
    @DisplayName("Should report existence by email")
    void shouldReportExistenceByEmail() {
      newUser("Sam", "sam@example.com", "hash123");

      assertThat(userRepository.existsByEmail("sam@example.com")).isTrue();
      assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }
  }

  private User newUser(String firstName, String email, String password) {
    return userRepository.save(User.register(firstName, "Aldana", email, password));
  }
}
