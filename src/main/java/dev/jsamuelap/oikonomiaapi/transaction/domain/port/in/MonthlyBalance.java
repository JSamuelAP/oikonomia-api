package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyBalance(YearMonth month, BigDecimal totalIncome, BigDecimal totalExpense) {
  public static final BigDecimal ZERO_AMOUNT = new BigDecimal("0.00");

  public BigDecimal balance() {
    return totalIncome.subtract(totalExpense);
  }

  public static MonthlyBalance empty(YearMonth month) {
    return new MonthlyBalance(month, ZERO_AMOUNT, ZERO_AMOUNT);
  }
}
