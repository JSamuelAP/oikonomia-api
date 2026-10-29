package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

import java.math.BigDecimal;
import java.time.YearMonth;

import dev.jsamuelap.oikonomiaapi.shared.domain.validation.MoneyRules;

public record MonthlyBalance(YearMonth month, BigDecimal totalIncome, BigDecimal totalExpense) {
  public BigDecimal balance() {
    return totalIncome.subtract(totalExpense);
  }

  public static MonthlyBalance empty(YearMonth month) {
    return new MonthlyBalance(month, MoneyRules.ZERO_AMOUNT, MoneyRules.ZERO_AMOUNT);
  }
}
