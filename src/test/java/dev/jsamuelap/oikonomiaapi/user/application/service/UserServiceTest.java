package dev.jsamuelap.oikonomiaapi.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jsamuelap.oikonomiaapi.user.domain.exception.UserNotFoundException;
import dev.jsamuelap.oikonomiaapi.user.domain.model.User;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.UserProfile;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("User service")
public class UserServiceTest {
  @Mock
  private UserRepositoryPort userRepository;

  private UserService userService;

  @BeforeEach
  void setup() {
    userService = new UserService(userRepository);
  }

  @Nested
  @DisplayName("Get User")
  class Get {
    @Test
    @DisplayName("Should reject when user does not exits")
    void shouldRejectWhenUserDoesNotExits() {
      UUID id = UUID.randomUUID();
      when(userRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.get(id)).isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Should return user profile")
    void shouldReturnUserProfile() {
      UUID id = UUID.randomUUID();
      String firstName = "Mike";
      String lastName = "Evans";
      String email = "mike@example.com";
      User user = User.reconstitute(id, firstName, lastName, email, "", null);
      when(userRepository.findById(id)).thenReturn(Optional.of(user));

      UserProfile response = userService.get(id);
      UserProfile userProfile = new UserProfile(id, email, firstName, lastName);
      assertThat(response).isEqualTo(userProfile);
    }
  }
}
