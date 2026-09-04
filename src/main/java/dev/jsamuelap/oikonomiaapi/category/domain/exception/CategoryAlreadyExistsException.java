package dev.jsamuelap.oikonomiaapi.category.domain.exception;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.ConflictException;

public class CategoryAlreadyExistsException extends ConflictException {
  private static final long serialVersionUID = 1L;

  public CategoryAlreadyExistsException(String name, String flowType) {
    super("Ya existe una categoría de %s con el nombre '%s'".formatted(flowType, name));
  }
}
