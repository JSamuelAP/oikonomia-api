package dev.jsamuelap.oikonomiaapi.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.jsamuelap.oikonomiaapi.user.domain.exception.UserNotFoundException;
import dev.jsamuelap.oikonomiaapi.user.domain.model.User;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.GetUserUseCase;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.UserProfile;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements GetUserUseCase {
  private final UserRepositoryPort userRepository;

  @Override
  public UserProfile get(UUID id) {
    User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    return new UserProfile(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
  }
}
