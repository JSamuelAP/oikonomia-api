package dev.jsamuelap.oikonomiaapi.budget.domain.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Monthly budget view DTO")
public class MonthlyBudgetViewTest {
  @ParameterizedTest
  @DisplayName("Should compute remaining amount")
  @CsvSource({"3000.00, 2150.40, 849.60", "3000.00, 3000.00, 0.00", "3000.00, 3500.00, -500.00"})
  void shouldComputeRemainingAmount(String expected, String actual, String remaining) {
    var view = new MonthlyBudgetView(UUID.randomUUID(), UUID.randomUUID(), (short) 10, (short) 2026,
      new BigDecimal(expected), new BigDecimal(actual), null);

    assertThat(view.remainingAmount()).isEqualTo(new BigDecimal(remaining));
  }
}
