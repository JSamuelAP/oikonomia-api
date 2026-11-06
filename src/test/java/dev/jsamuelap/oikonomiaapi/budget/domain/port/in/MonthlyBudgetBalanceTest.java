package dev.jsamuelap.oikonomiaapi.budget.domain.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategorySummary;

@DisplayName("Monthly budget balance")
class MonthlyBudgetBalanceTest {
  private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

  private static MonthlyBudgetView view(String flowType, String expected, String actual) {
    return new MonthlyBudgetView(UUID.randomUUID(), UUID.randomUUID(), (short) 10, (short) 2026,
      new BigDecimal(expected), new BigDecimal(actual), new CategorySummary(UUID.randomUUID(), "Any", flowType, false));
  }

  @Nested
  @DisplayName("Create balance from budgets")
  class Of {
    @Test
    @DisplayName("Should return zeros with two decimals when there are no budgets")
    void shouldReturnZerosWithTwoDecimalsWhenThereAreNoBudgets() {
      var balance = MonthlyBudgetBalance.of(OCTOBER, List.of());

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(balance.month()).as("month").isEqualTo(OCTOBER);
        softly.assertThat(balance.income().expected()).as("income expected").isEqualTo(new BigDecimal("0.00"));
        softly.assertThat(balance.income().actual()).as("income actual").isEqualTo(new BigDecimal("0.00"));
        softly.assertThat(balance.expense().expected()).as("expense expected").isEqualTo(new BigDecimal("0.00"));
        softly.assertThat(balance.expense().actual()).as("expense actual").isEqualTo(new BigDecimal("0.00"));
        softly.assertThat(balance.expectedBalance()).as("expectedBalance").isEqualTo(new BigDecimal("0.00"));
        softly.assertThat(balance.actualBalance()).as("actualBalance").isEqualTo(new BigDecimal("0.00"));
      });
    }

    @Test
    @DisplayName("Should add each flow type separately")
    void shouldAddEachFlowTypeSeparately() {
      var balance = MonthlyBudgetBalance.of(OCTOBER, List.of(view("INCOME", "5000.00", "5200.00"),
        view("INCOME", "1000.00", "0.00"), view("EXPENSE", "3000.00", "2150.40"), view("EXPENSE", "500.00", "700.00")));

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(balance.income().expected()).as("income expected").isEqualTo(new BigDecimal("6000.00"));
        softly.assertThat(balance.income().actual()).as("income actual").isEqualTo(new BigDecimal("5200.00"));
        softly.assertThat(balance.expense().expected()).as("expense expected").isEqualTo(new BigDecimal("3500.00"));
        softly.assertThat(balance.expense().actual()).as("expense actual").isEqualTo(new BigDecimal("2850.40"));
        softly.assertThat(balance.expectedBalance()).as("expectedBalance").isEqualTo(new BigDecimal("2500.00"));
        softly.assertThat(balance.actualBalance()).as("actualBalance").isEqualTo(new BigDecimal("2349.60"));
      });
    }

    @Test
    @DisplayName("Should allow negative balances")
    void shouldAllowNegativeBalances() {
      var balance = MonthlyBudgetBalance.of(OCTOBER,
        List.of(view("INCOME", "1000.00", "800.00"), view("EXPENSE", "3000.00", "3100.00")));

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(balance.expectedBalance()).as("expectedBalance").isEqualTo(new BigDecimal("-2000.00"));
        softly.assertThat(balance.actualBalance()).as("actualBalance").isEqualTo(new BigDecimal("-2300.00"));
      });
    }

    @ParameterizedTest
    @DisplayName("Should reject unknown flow type")
    @NullSource
    @ValueSource(strings = {"TRANSFER", "income", ""})
    void shouldRejectUnknownFlowType(String flowType) {
      var budgets = List.of(view(flowType, "100.00", "50.00"));

      assertThatThrownBy(() -> MonthlyBudgetBalance.of(OCTOBER, budgets)).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Tipo de flujo desconocido");
    }
  }

  @Nested
  @DisplayName("Flow totals remaining")
  class FlowTotalsRemaining {
    @ParameterizedTest
    @DisplayName("Should subtract actual from expected")
    @CsvSource({"3000.00, 2150.40, 849.60", "3000.00, 3000.00, 0.00", "3000.00, 3500.00, -500.00"})
    void shouldSubtractActualFromExpected(String expected, String actual, String remaining) {
      var totals = new MonthlyBudgetBalance.FlowTotals(new BigDecimal(expected), new BigDecimal(actual));

      assertThat(totals.remaining()).isEqualTo(new BigDecimal(remaining));
    }
  }
}
