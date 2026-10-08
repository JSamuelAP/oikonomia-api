package dev.jsamuelap.oikonomiaapi.user.domain.exception;

import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
  private static final long serialVersionUID = 1L;

  public UserNotFoundException(UUID id) {
    super("No se encontró el usuario con id " + id);
  }
}
