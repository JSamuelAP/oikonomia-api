package dev.jsamuelap.oikonomiaapi.category.domain.exception;

import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.NotFoundException;

public class CategoryNotFoundException extends NotFoundException {
  private static final long serialVersionUID = 1L;

  public CategoryNotFoundException(UUID id) {
    super("No se encontró la categoría con id " + id);
  }
}
