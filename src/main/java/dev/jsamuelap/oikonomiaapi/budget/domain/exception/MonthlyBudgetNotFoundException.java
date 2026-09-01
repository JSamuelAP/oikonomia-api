package dev.jsamuelap.oikonomiaapi.budget.domain.exception;

import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.NotFoundException;

public class MonthlyBudgetNotFoundException extends NotFoundException {
  private static final long serialVersionUID = 1L;

  public MonthlyBudgetNotFoundException(UUID id) {
    super("No se encontró el presupuesto mensual con id " + id);
  }
}
