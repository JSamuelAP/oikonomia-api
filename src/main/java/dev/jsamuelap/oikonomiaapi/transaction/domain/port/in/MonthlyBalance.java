package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyBalance(YearMonth month, BigDecimal totalIncome, BigDecimal totalExpense) {
  public BigDecimal balance() {
    return totalIncome.subtract(totalExpense);
  }

  public static MonthlyBalance empty(YearMonth month) {
    return new MonthlyBalance(month, BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2));
  }
}
