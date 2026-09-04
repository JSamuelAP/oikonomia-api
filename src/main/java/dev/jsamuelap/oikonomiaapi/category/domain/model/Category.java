package dev.jsamuelap.oikonomiaapi.category.domain.model;

import java.time.Instant;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

import lombok.Getter;

@Getter
public final class Category {
  private final UUID id;
  private final UUID userId;
  private final String name;
  private final FlowType flowType;
  private Instant deletedAt;

  private static final short MIN_NAME_LENGTH = 2;
  private static final short MAX_NAME_LENGTH = 50;

  private Category(UUID id, UUID userId, String name, FlowType flowType, Instant deletedAt) {
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.flowType = flowType;
    this.deletedAt = deletedAt;
  }

  public static Category create(UUID userId, String name, FlowType flowType) {
    validateName(name);
    return new Category(UUID.randomUUID(), userId, name, flowType, null);
  }

  public static Category reconstitute(UUID id, UUID userId, String name, FlowType flowType, Instant deletedAt) {
    return new Category(id, userId, name, flowType, deletedAt);
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void delete() {
    if (isDeleted()) {
      throw new DomainException("La categoría ya está eliminada");
    }
    this.deletedAt = Instant.now();
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new DomainException("El nombre no puede estar vacío");
    }

    if (name.length() < MIN_NAME_LENGTH) {
      throw new DomainException("El nombre debe tener mínimo %s caracteres".formatted(MIN_NAME_LENGTH));
    }
    if (name.length() > MAX_NAME_LENGTH) {
      throw new DomainException("El nombre no puede exceder más de %s caracteres".formatted(MAX_NAME_LENGTH));
    }
  }
}
