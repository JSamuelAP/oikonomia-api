package dev.jsamuelap.oikonomiaapi.transaction.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

import lombok.Getter;

@Getter
public final class Transaction {
  private final UUID id;
  private final UUID userId;
  private final UUID categoryId;
  private final BigDecimal amount;
  private final LocalDate date;
  private final String notes;

  private static final short MAX_NOTES_LENGTH = 255;

  private Transaction(UUID id, UUID userId, UUID categoryId, BigDecimal amount, LocalDate date, String notes) {
    this.id = id;
    this.userId = userId;
    this.categoryId = categoryId;
    this.amount = amount;
    this.date = date;
    this.notes = notes;
  }

  public static Transaction create(UUID userId, UUID categoryId, BigDecimal amount, LocalDate date, String notes) {
    validateUserId(userId);
    validateCategoryId(categoryId);
    validateAmount(amount);
    validateDate(date);
    validateNotes(notes);
    return new Transaction(UUID.randomUUID(), userId, categoryId, amount, date, notes);
  }

  public static Transaction reconstitute(UUID id, UUID userId, UUID categoryId, BigDecimal amount, LocalDate date,
    String notes) {
    return new Transaction(id, userId, categoryId, amount, date, notes);
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

  private static void validateAmount(BigDecimal amount) {
    if (amount == null) {
      throw new DomainException("La cantidad no puede ser nula");
    }

    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new DomainException("La cantidad debe ser mayor a 0");
    }
  }

  private static void validateDate(LocalDate date) {
    if (date == null) {
      throw new DomainException("La fecha no puede ser nula");
    }

    if (date.isAfter(LocalDate.now())) {
      throw new DomainException("La fecha no puede ser futura");
    }
  }

  private static void validateNotes(String notes) {
    if (notes == null) {
      return;
    }

    if (notes.length() > MAX_NOTES_LENGTH) {
      throw new DomainException("Las notas no pueden exceder más de %s caracteres".formatted(MAX_NOTES_LENGTH));
    }
  }
}
