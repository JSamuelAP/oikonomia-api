package dev.jsamuelap.oikonomiaapi.budget.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

import lombok.Getter;

@Getter
public final class MonthlyBudget {
  private final UUID id;
  private final UUID userId;
  private final UUID categoryId;
  private final Short month;
  private final Short year;
  private final BigDecimal expectedAmount;

  private MonthlyBudget(UUID id, UUID userId, UUID categoryId, Short month, Short year, BigDecimal expectedAmount) {
    this.id = id;
    this.userId = userId;
    this.categoryId = categoryId;
    this.month = month;
    this.year = year;
    this.expectedAmount = expectedAmount;
  }

  public static MonthlyBudget create(UUID userId, UUID categoryId, Short month, Short year, BigDecimal expectedAmount) {
    validateUserId(userId);
    validateCategoryId(categoryId);
    validateMonth(month);
    validateYear(year);
    validateExpectedAmount(expectedAmount);
    return new MonthlyBudget(UUID.randomUUID(), userId, categoryId, month, year, expectedAmount);
  }

  public static MonthlyBudget reconstitute(UUID id, UUID userId, UUID categoryId, Short month, Short year,
    BigDecimal expectedAmount) {
    return new MonthlyBudget(id, userId, categoryId, month, year, expectedAmount);
  }

  private static void validateUserId(UUID userId) {
    if (userId == null) {
      throw new DomainException("El id del usuario no puede ser nulo");
    }
  }

  private static void validateCategoryId(UUID categoryId) {
    if (categoryId == null) {
      throw new DomainException("El id de la categoría no puede ser nulo");
    }
  }

  private static void validateMonth(Short month) {
    if (month == null) {
      throw new DomainException("El mes no puede ser nulo");
    }

    if (month < 1 || month > 12) {
      throw new DomainException("El mes debe ser entre 1 y 12");
    }
  }

  private static void validateYear(Short year) {
    if (year == null) {
      throw new DomainException("El año no puede ser nulo");
    }

    if (year < 2025 || year > 2100) {
      throw new DomainException("El año debe ser entre 2025 y 2100");
    }
  }

  private static void validateExpectedAmount(BigDecimal amount) {
    if (amount == null) {
      throw new DomainException("La cantidad presupuestada no puede ser nula");
    }

    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new DomainException("La cantidad presupuestada debe ser mayor a 0");
    }
  }
}
