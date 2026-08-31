package dev.jsamuelap.oikonomiaapi.category.domain.model;

import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

import lombok.Getter;

@Getter
public final class Category {
  private final UUID id;
  private final UUID userId;
  private final String name;
  private final FlowType flowType;

  // TODO: validar MIN LENGTH
  private static final short MAX_NAME_LENGTH = 50;

  private Category(UUID id, UUID userId, String name, FlowType flowType) {
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.flowType = flowType;
  }

  public static Category create(UUID userId, String name, FlowType flowType) {
    validateName(name);
    return new Category(UUID.randomUUID(), userId, name, flowType);
  }

  public static Category reconstitute(UUID id, UUID userId, String name, FlowType flowType) {
    return new Category(id, userId, name, flowType);
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new DomainException("El nombre no puede estar vacío");
    }

    if (name.length() > MAX_NAME_LENGTH) {
      throw new DomainException("El nombre no puede exceder más de %s caracteres".formatted(MAX_NAME_LENGTH));
    }
  }
}
