package dev.jsamuelap.oikonomiaapi.budget.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jsamuelap.oikonomiaapi.budget.domain.exception.MonthlyBudgetAlreadyExistsException;
import dev.jsamuelap.oikonomiaapi.budget.domain.exception.MonthlyBudgetNotFoundException;
import dev.jsamuelap.oikonomiaapi.budget.domain.model.MonthlyBudget;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.CreateMonthlyBudgetCommand;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.MonthlyBudgetDetailView;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.MonthlyBudgetView;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.UpdateMonthlyBudgetCommand;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategoryLookupPort;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategorySummary;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.MonthlyBudgetDetail;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.MonthlyBudgetRepositoryPort;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.TransactionTotalsLookupPort;
import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;
import dev.jsamuelap.oikonomiaapi.shared.domain.validation.PeriodRules;

@ExtendWith(MockitoExtension.class)
@DisplayName("Monthly budget service")
class MonthlyBudgetServiceTest {
  @Mock
  private MonthlyBudgetRepositoryPort monthlyBudgetRepository;
  @Mock
  private CategoryLookupPort categoryLookupPort;
  @Mock
  private TransactionTotalsLookupPort transactionTotalsLookupPort;

  @InjectMocks
  private MonthlyBudgetService monthlyBudgetService;

  private static final YearMonth OCTOBER = YearMonth.of(2026, 10);
  private final UUID userId = UUID.randomUUID();

  @Nested
  @DisplayName("Get all monthly budgets")
  class GetAll {
    @Test
    @DisplayName("Should reject when year is invalid")
    void shouldRejectWhenYearIsInvalid() {
      var invalidMonth = YearMonth.of(PeriodRules.MIN_YEAR - 1, 9);

      assertThatThrownBy(() -> monthlyBudgetService.getAll(userId, invalidMonth)).isInstanceOf(DomainException.class)
        .hasMessageContaining("El año debe estar entre");

      verify(monthlyBudgetRepository, never()).findAllByUser(any(), any());
    }

    @Test
    @DisplayName("Should calculate current year when null")
    void shouldCalculateCurrentYearWhenNull() {
      UUID userId = UUID.randomUUID();
      monthlyBudgetService.getAll(userId, null);

      verify(monthlyBudgetRepository).findAllByUser(userId, YearMonth.now());
    }

    @Test
    @DisplayName("Should return monthly budget view")
    void shouldReturnMonthlyBudgetView() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      Short month = 9;
      Short year = 2026;
      YearMonth yearMonth = YearMonth.of(year, month);
      BigDecimal amount = BigDecimal.TEN;
      MonthlyBudget budget = MonthlyBudget.reconstitute(id, userId, categoryId, month, year, amount, null);
      when(monthlyBudgetRepository.findAllByUser(userId, yearMonth)).thenReturn(List.of(budget));

      CategorySummary category = new CategorySummary(categoryId, "Groceries", "EXPENSE", false);
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of(categoryId, category));

      when(transactionTotalsLookupPort.findTotalsByCategory(userId, yearMonth))
        .thenReturn(Map.of(categoryId, BigDecimal.TWO));

      MonthlyBudgetView expected = new MonthlyBudgetView(id, userId, month, year, amount, BigDecimal.TWO, category);

      List<MonthlyBudgetView> budgets = monthlyBudgetService.getAll(userId, yearMonth);
      assertThat(budgets).isEqualTo(List.of(expected));

      verify(monthlyBudgetRepository).findAllByUser(userId, yearMonth);
      verify(categoryLookupPort).findByIds(Set.of(categoryId), userId);
    }

    @Test
    @DisplayName("Should return empty list without querying lookups when month has no budgets")
    void shouldReturnEmptyListWithoutQueryingLookupsWhenMonthHasNoBudgets() {
      when(monthlyBudgetRepository.findAllByUser(userId, OCTOBER)).thenReturn(List.of());

      assertThat(monthlyBudgetService.getAll(userId, OCTOBER)).isEmpty();

      verifyNoInteractions(categoryLookupPort, transactionTotalsLookupPort);
    }

    @Test
    @DisplayName("Should take actual amount from the totals of each category")
    void shouldTakeActualAmountFromTheTotalsOfEachCategory() {
      UUID expenseCat = UUID.randomUUID();
      UUID incomeCat = UUID.randomUUID();
      when(monthlyBudgetRepository.findAllByUser(userId, OCTOBER))
        .thenReturn(List.of(budget(expenseCat, "3000.00"), budget(incomeCat, "5000.00")));
      when(categoryLookupPort.findByIds(Set.of(expenseCat, incomeCat), userId))
        .thenReturn(Map.of(expenseCat, category(expenseCat, "EXPENSE"), incomeCat, category(incomeCat, "INCOME")));
      when(transactionTotalsLookupPort.findTotalsByCategory(userId, OCTOBER))
        .thenReturn(Map.of(expenseCat, new BigDecimal("2150.40"), incomeCat, new BigDecimal("5200.00")));

      List<MonthlyBudgetView> result = monthlyBudgetService.getAll(userId, OCTOBER);

      assertThat(result).extracting(MonthlyBudgetView::actualAmount).containsExactly(new BigDecimal("2150.40"),
        new BigDecimal("5200.00"));
    }

    @Test
    @DisplayName("Should use zero with two decimals when category has no transactions")
    void shouldUseZeroWithTwoDecimalsWhenCategoryHasNoTransactions() {
      UUID categoryId = UUID.randomUUID();
      when(monthlyBudgetRepository.findAllByUser(userId, OCTOBER)).thenReturn(List.of(budget(categoryId, "3000.00")));
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId))
        .thenReturn(Map.of(categoryId, category(categoryId, "EXPENSE")));
      when(transactionTotalsLookupPort.findTotalsByCategory(userId, OCTOBER)).thenReturn(Map.of());

      assertThat(monthlyBudgetService.getAll(userId, OCTOBER).getFirst().actualAmount())
        .isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("Should query each lookup only once for several budgets")
    void shouldQueryEachLookupOnlyOnceForSeveralBudgets() {
      UUID c1 = UUID.randomUUID();
      UUID c2 = UUID.randomUUID();
      UUID c3 = UUID.randomUUID();
      when(monthlyBudgetRepository.findAllByUser(userId, OCTOBER))
        .thenReturn(List.of(budget(c1, "1.00"), budget(c2, "2.00"), budget(c3, "3.00")));
      when(categoryLookupPort.findByIds(Set.of(c1, c2, c3), userId))
        .thenReturn(Map.of(c1, category(c1, "EXPENSE"), c2, category(c2, "EXPENSE"), c3, category(c3, "INCOME")));

      monthlyBudgetService.getAll(userId, OCTOBER);

      verify(categoryLookupPort).findByIds(Set.of(c1, c2, c3), userId);
      verify(transactionTotalsLookupPort).findTotalsByCategory(userId, OCTOBER);
      verifyNoMoreInteractions(categoryLookupPort, transactionTotalsLookupPort);
    }

    @Test
    @DisplayName("Should fail when category is missing from lookup")
    void shouldFailWhenCategoryIsMissingFromLookup() {
      UUID categoryId = UUID.randomUUID();
      when(monthlyBudgetRepository.findAllByUser(userId, OCTOBER)).thenReturn(List.of(budget(categoryId, "3000.00")));
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of());

      assertThatThrownBy(() -> monthlyBudgetService.getAll(userId, OCTOBER)).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(categoryId.toString());
    }
  }

  @Nested
  @DisplayName("Get monthly budget by ID")
  class GetById {
    private final UUID budgetId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();

    @Test
    @DisplayName("Should reject when budget does not exist")
    void shouldRejectWhenBudgetDoesNotExist() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      when(monthlyBudgetRepository.findDetailByIdAndUser(id, userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> monthlyBudgetService.getById(id, userId))
        .isInstanceOf(MonthlyBudgetNotFoundException.class).hasMessageContaining(id.toString());
      verifyNoInteractions(categoryLookupPort, transactionTotalsLookupPort);
    }

    @Test
    @DisplayName("Should return monthly budget detail view")
    void shouldReturnMonthlyBudgetDetailView() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      Short month = 9;
      Short year = 2026;
      BigDecimal amount = BigDecimal.TEN;
      Instant instant = Instant.now();
      MonthlyBudgetDetail budget = new MonthlyBudgetDetail(id, categoryId, month, year, amount, instant, instant);
      when(monthlyBudgetRepository.findDetailByIdAndUser(id, userId)).thenReturn(Optional.of(budget));

      CategorySummary category = new CategorySummary(categoryId, "Groceries", "EXPENSE", false);
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of(categoryId, category));

      when(transactionTotalsLookupPort.findTotalsByCategory(userId, YearMonth.of(year, month)))
        .thenReturn(Map.of(categoryId, BigDecimal.TWO));

      MonthlyBudgetDetailView expected = new MonthlyBudgetDetailView(id, month, year, amount, BigDecimal.TWO, category,
        instant, instant);

      MonthlyBudgetDetailView response = monthlyBudgetService.getById(id, userId);
      assertThat(response).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return actual amount of the budget month not the current one")
    void shouldReturnActualAmountOfTheBudgetMonthNotTheCurrentOne() {
      when(monthlyBudgetRepository.findDetailByIdAndUser(budgetId, userId)).thenReturn(Optional.of(detail()));
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId))
        .thenReturn(Map.of(categoryId, category(categoryId, "EXPENSE")));
      when(transactionTotalsLookupPort.findTotalsByCategory(userId, YearMonth.of(2025, 3)))
        .thenReturn(Map.of(categoryId, new BigDecimal("150.25")));

      MonthlyBudgetDetailView result = monthlyBudgetService.getById(budgetId, userId);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(result.actualAmount()).as("actualAmount").isEqualTo(new BigDecimal("150.25"));
        softly.assertThat(result.expectedAmount()).as("expectedAmount").isEqualTo(new BigDecimal("3000.00"));
        softly.assertThat(result.category().id()).as("category").isEqualTo(categoryId);
      });
    }

    @Test
    @DisplayName("Should use zero with two decimals when category has no transactions")
    void shouldUseZeroWithTwoDecimalsWhenCategoryHasNoTransactions() {
      when(monthlyBudgetRepository.findDetailByIdAndUser(budgetId, userId)).thenReturn(Optional.of(detail()));
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId))
        .thenReturn(Map.of(categoryId, category(categoryId, "EXPENSE")));
      when(transactionTotalsLookupPort.findTotalsByCategory(userId, YearMonth.of(2025, 3))).thenReturn(Map.of());

      assertThat(monthlyBudgetService.getById(budgetId, userId).actualAmount()).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("Should fail when category is missing from lookup")
    void shouldFailWhenCategoryIsMissingFromLookup() {
      when(monthlyBudgetRepository.findDetailByIdAndUser(budgetId, userId)).thenReturn(Optional.of(detail()));
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of());

      assertThatThrownBy(() -> monthlyBudgetService.getById(budgetId, userId)).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(categoryId.toString());
    }

    private MonthlyBudgetDetail detail() {
      return new MonthlyBudgetDetail(budgetId, categoryId, (short) 3, (short) 2025, new BigDecimal("3000.00"),
        Instant.now(), Instant.now());
    }
  }

  @Nested
  @DisplayName("Create monthly budget")
  class Create {
    @Test
    @DisplayName("Should reject when category does not exist")
    void shouldRejectWhenCategoryDoesNotExist() {
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of());

      var command = new CreateMonthlyBudgetCommand(userId, categoryId, (short) 9, (short) 2026, BigDecimal.TEN);
      assertThatThrownBy(() -> monthlyBudgetService.create(command)).isInstanceOf(DomainException.class)
        .hasMessageContaining("Categoría no encontrada");

      verify(monthlyBudgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject when budget already exists")
    void shouldRejectWhenBudgetAlreadyExists() {
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      Short month = 9;
      Short year = 2026;
      when(monthlyBudgetRepository.existsByCategoryAndUserAndDate(categoryId, userId, month, year)).thenReturn(true);

      CategorySummary category = new CategorySummary(categoryId, "Groceries", "EXPENSE", false);
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of(categoryId, category));

      var command = new CreateMonthlyBudgetCommand(userId, categoryId, month, year, BigDecimal.TEN);
      assertThatThrownBy(() -> monthlyBudgetService.create(command))
        .isInstanceOf(MonthlyBudgetAlreadyExistsException.class).hasMessageContaining("Ya existe un presupuesto");

      verify(monthlyBudgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should persist monthly budget")
    void shouldPersist() {
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      CategorySummary category = new CategorySummary(categoryId, "Groceries", "EXPENSE", false);
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of(categoryId, category));

      Short month = 9;
      Short year = 2026;
      BigDecimal amount = BigDecimal.TEN;
      MonthlyBudget saved = MonthlyBudget.create(userId, categoryId, month, year, amount);
      when(monthlyBudgetRepository.save(any())).thenReturn(saved);

      var command = new CreateMonthlyBudgetCommand(userId, categoryId, month, year, amount);
      UUID responseId = monthlyBudgetService.create(command);

      assertThat(responseId).isNotNull();

      verify(monthlyBudgetRepository).save(argThat(budget -> budget.getUserId().equals(userId)));
    }
  }

  @Nested
  @DisplayName("Update monthly budget")
  class Update {
    @Test
    @DisplayName("Should reject when budget does not exist")
    void shouldRejectWhenBudgetDoesNotExist() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      when(monthlyBudgetRepository.findByIdAndUser(id, userId)).thenReturn(Optional.empty());

      var command = new UpdateMonthlyBudgetCommand(id, userId, UUID.randomUUID(), (short) 9, (short) 2026,
        BigDecimal.TEN);
      assertThatThrownBy(() -> monthlyBudgetService.update(command)).isInstanceOf(MonthlyBudgetNotFoundException.class)
        .hasMessageContaining(id.toString());

      verify(monthlyBudgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject when category does not exist")
    void shouldRejectWhenCategoryDoesNotExist() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      Short month = 9;
      Short year = 2026;
      BigDecimal amount = BigDecimal.TEN;
      MonthlyBudget budget = MonthlyBudget.reconstitute(id, userId, categoryId, month, year, amount, null);
      when(monthlyBudgetRepository.findByIdAndUser(id, userId)).thenReturn(Optional.of(budget));

      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of());

      var command = new UpdateMonthlyBudgetCommand(id, userId, categoryId, month, year, amount);
      assertThatThrownBy(() -> monthlyBudgetService.update(command)).isInstanceOf(DomainException.class)
        .hasMessageContaining("Categoría no encontrada");

      verify(monthlyBudgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update monthly budget")
    void shouldUpdate() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      Short month = 9;
      Short year = 2026;
      BigDecimal amount = BigDecimal.TEN;
      MonthlyBudget budget = MonthlyBudget.reconstitute(id, userId, categoryId, month, year, amount, null);
      when(monthlyBudgetRepository.findByIdAndUser(id, userId)).thenReturn(Optional.of(budget));

      UUID updatedCategoryId = UUID.randomUUID();
      CategorySummary category = new CategorySummary(updatedCategoryId, "Salary", "INCOME", false);
      when(categoryLookupPort.findByIds(Set.of(updatedCategoryId), userId))
        .thenReturn(Map.of(updatedCategoryId, category));

      Short updatedMonth = 10;
      Short updatedYear = 2027;
      BigDecimal updatedAmount = BigDecimal.TWO;
      var command = new UpdateMonthlyBudgetCommand(id, userId, updatedCategoryId, updatedMonth, updatedYear,
        updatedAmount);
      monthlyBudgetService.update(command);

      ArgumentCaptor<MonthlyBudget> captor = ArgumentCaptor.forClass(MonthlyBudget.class);
      verify(monthlyBudgetRepository).save(captor.capture());

      MonthlyBudget expected = MonthlyBudget.reconstitute(id, userId, updatedCategoryId, updatedMonth, updatedYear,
        updatedAmount, null);
      assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(expected);
    }
  }

  @Nested
  @DisplayName("Delete monthly budget")
  class Delete {
    @Test
    @DisplayName("Should reject when budget does not exist")
    void shouldRejectWhenBudgetDoesNotExist() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      when(monthlyBudgetRepository.findByIdAndUser(id, userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> monthlyBudgetService.deleteById(id, userId))
        .isInstanceOf(MonthlyBudgetNotFoundException.class).hasMessageContaining(id.toString());

      verify(monthlyBudgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not delete when budget is already deleted")
    void shouldNotDeleteWhenBudgetIsAlreadyDeleted() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      MonthlyBudget deletedBudget = MonthlyBudget.reconstitute(id, userId, UUID.randomUUID(), (short) 9, (short) 2026,
        BigDecimal.TEN, Instant.now());
      when(monthlyBudgetRepository.findByIdAndUser(id, userId)).thenReturn(Optional.of(deletedBudget));

      monthlyBudgetService.deleteById(id, userId);

      verify(monthlyBudgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete monthly budget")
    void shouldDelete() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      MonthlyBudget existingBudget = MonthlyBudget.reconstitute(id, userId, UUID.randomUUID(), (short) 9, (short) 2026,
        BigDecimal.TEN, null);
      when(monthlyBudgetRepository.findByIdAndUser(id, userId)).thenReturn(Optional.of(existingBudget));

      monthlyBudgetService.deleteById(id, userId);

      verify(monthlyBudgetRepository).save(argThat(budget -> budget.getId().equals(id) && budget.isDeleted()));
    }
  }

  private MonthlyBudget budget(UUID categoryId, String expected) {
    return MonthlyBudget.reconstitute(UUID.randomUUID(), userId, categoryId, (short) 10, (short) 2026,
      new BigDecimal(expected), null);
  }

  private static CategorySummary category(UUID id, String flowType) {
    return new CategorySummary(id, "Any", flowType, false);
  }
}
