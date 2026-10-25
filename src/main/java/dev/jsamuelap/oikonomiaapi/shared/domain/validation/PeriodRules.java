package dev.jsamuelap.oikonomiaapi.shared.domain.validation;

import java.time.YearMonth;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

public final class PeriodRules {
  public static final int MIN_YEAR = 2025;
  public static final int MAX_YEAR = 2100;

  private PeriodRules() {}

  public static void requireSupportedYear(int year) {
    if (year < MIN_YEAR || year > MAX_YEAR) {
      throw new DomainException("El año debe estar entre %d y %d".formatted(MIN_YEAR, MAX_YEAR));
    }
  }

  public static void requireSupportedYear(YearMonth month) {
    if (month == null) {
      throw new DomainException("El año es requerido");
    }
    requireSupportedYear(month.getYear());
  }
}
