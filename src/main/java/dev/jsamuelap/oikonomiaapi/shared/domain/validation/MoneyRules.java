package dev.jsamuelap.oikonomiaapi.shared.domain.validation;

import java.math.BigDecimal;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

public final class MoneyRules {
  public static final int MAX_INTEGER_DIGITS = 10;
  public static final int MAX_SCALE = 2;

  private MoneyRules() {}

  public static void requireValidAmount(BigDecimal value, String label) {
    if (value == null) {
      throw new DomainException("%s es requerido".formatted(label));
    }
    if (value.signum() <= 0) {
      throw new DomainException("%s debe ser mayor a cero".formatted(label));
    }

    BigDecimal stripped = value.stripTrailingZeros();
    if (stripped.scale() > MAX_SCALE) {
      throw new DomainException("%s no puede tener más de %d decimales".formatted(label, MAX_SCALE));
    }
    if (stripped.precision() - stripped.scale() > MAX_INTEGER_DIGITS) {
      throw new DomainException("%s no puede tener más de %d dígitos enteros".formatted(label, MAX_INTEGER_DIGITS));
    }
  }
}
