package dev.jsamuelap.oikonomiaapi.transaction.domain.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Category summary DTO")
public class CategorySummaryTest {
  @Test
  @DisplayName("Should isIncome() be true and isExpense() false")
  void shouldIsIncomeBeTrueAndIsExpenseFalse() {
    var category = new CategorySummary(UUID.randomUUID(), "Salary", "INCOME", false);
    assertThat(category.isIncome()).isTrue();
    assertThat(category.isExpense()).isFalse();
  }

  @Test
  @DisplayName("Should isExpense() be true and isIncome() false")
  void shouldIsExpenseBeTrueAndIsIncomeFalse() {
    var category = new CategorySummary(UUID.randomUUID(), "Groceries", "EXPENSE", false);
    assertThat(category.isExpense()).isTrue();
    assertThat(category.isIncome()).isFalse();
  }
}
