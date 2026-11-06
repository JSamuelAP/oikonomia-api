package dev.jsamuelap.oikonomiaapi.budget.domain.port.in;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collection;

import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategorySummary;
import dev.jsamuelap.oikonomiaapi.shared.domain.validation.MoneyRules;

public record MonthlyBudgetBalance(YearMonth month, FlowTotals income, FlowTotals expense) {
  public BigDecimal expectedBalance() {
    return income.expected().subtract(expense.expected());
  }

  public BigDecimal actualBalance() {
    return income.actual().subtract(expense.actual());
  }

  public static MonthlyBudgetBalance of(YearMonth month, Collection<MonthlyBudgetView> budgets) {
    FlowTotals income = FlowTotals.ZERO;
    FlowTotals expense = FlowTotals.ZERO;

    for (MonthlyBudgetView budget : budgets) {
      CategorySummary category = budget.category();
      if (category.isIncome()) {
        income = income.plus(budget.expectedAmount(), budget.actualAmount());
      } else if (category.isExpense()) {
        expense = expense.plus(budget.expectedAmount(), budget.actualAmount());
      } else {
        throw new IllegalStateException("Tipo de flujo desconocido: " + category.flowType());
      }
    }
    return new MonthlyBudgetBalance(month, income, expense);
  }

  public record FlowTotals(BigDecimal expected, BigDecimal actual) {
    static final FlowTotals ZERO = new FlowTotals(MoneyRules.ZERO_AMOUNT, MoneyRules.ZERO_AMOUNT);

    FlowTotals plus(BigDecimal expectedAmount, BigDecimal actualAmount) {
      return new FlowTotals(expected.add(expectedAmount), actual.add(actualAmount));
    }

    public BigDecimal remaining() {
      return expected.subtract(actual);
    }
  }
}
