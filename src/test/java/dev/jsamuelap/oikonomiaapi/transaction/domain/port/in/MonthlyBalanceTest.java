package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.YearMonth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.jsamuelap.oikonomiaapi.shared.domain.validation.MoneyRules;

@DisplayName("Monthly balance DTO")
public class MonthlyBalanceTest {
  @Nested
  @DisplayName("Balance computation")
  class Balance {
    @Test
    @DisplayName("Should compute positive balance when income exceeds expense")
    void shouldComputePositiveBalance() {
      MonthlyBalance balance = new MonthlyBalance(YearMonth.of(2025, 1), new BigDecimal("1000.00"),
        new BigDecimal("300.00"));

      assertThat(balance.balance()).isEqualTo(new BigDecimal("700.00"));
    }

    @Test
    @DisplayName("Should compute negative balance when expense exceeds income")
    void shouldComputeNegativeBalance() {
      MonthlyBalance balance = new MonthlyBalance(YearMonth.of(2025, 1), new BigDecimal("300.00"),
        new BigDecimal("1000.00"));

      assertThat(balance.balance()).isEqualTo(new BigDecimal("-700.00"));
    }

    @Test
    @DisplayName("Should compute zero balance when income equals expense")
    void shouldComputeZeroBalance() {
      MonthlyBalance balance = new MonthlyBalance(YearMonth.of(2025, 1), new BigDecimal("500.00"),
        new BigDecimal("500.00"));

      assertThat(balance.balance()).isEqualTo(MoneyRules.ZERO_AMOUNT);
    }

    @Test
    @DisplayName("Should handle decimal precision correctly")
    void shouldHandleDecimalPrecision() {
      MonthlyBalance balance = new MonthlyBalance(YearMonth.of(2025, 1), new BigDecimal("1000.50"),
        new BigDecimal("300.25"));

      assertThat(balance.balance()).isEqualTo(new BigDecimal("700.25"));
    }
  }

  @Nested
  @DisplayName("Empty factory")
  class Empty {
    @Test
    @DisplayName("Should create empty balance with zero income and expense")
    void shouldCreateEmptyBalance() {
      YearMonth month = YearMonth.of(2025, 1);
      MonthlyBalance balance = MonthlyBalance.empty(month);

      assertThat(balance.month()).isEqualTo(month);
      assertThat(balance.totalIncome()).isEqualTo(MoneyRules.ZERO_AMOUNT);
      assertThat(balance.totalExpense()).isEqualTo(MoneyRules.ZERO_AMOUNT);
    }

    @Test
    @DisplayName("Should have zero balance when created empty")
    void shouldHaveZeroBalance() {
      YearMonth month = YearMonth.of(2025, 12);
      MonthlyBalance balance = MonthlyBalance.empty(month);

      assertThat(balance.balance()).isEqualTo(MoneyRules.ZERO_AMOUNT);
    }
  }
}
