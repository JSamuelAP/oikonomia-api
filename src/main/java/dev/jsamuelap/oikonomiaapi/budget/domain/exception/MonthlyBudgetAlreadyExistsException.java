package dev.jsamuelap.oikonomiaapi.budget.domain.exception;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.ConflictException;

public class MonthlyBudgetAlreadyExistsException extends ConflictException {
  private static final long serialVersionUID = 1L;

  public MonthlyBudgetAlreadyExistsException(String categoryName, Short month, Short year) {
    super("Ya existe un presupuesto de %s para el %d-%d".formatted(categoryName, month, year));
  }
}
