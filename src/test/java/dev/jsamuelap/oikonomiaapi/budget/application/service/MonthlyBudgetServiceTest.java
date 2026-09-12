package dev.jsamuelap.oikonomiaapi.budget.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jsamuelap.oikonomiaapi.budget.domain.exception.MonthlyBudgetAlreadyExistsException;
import dev.jsamuelap.oikonomiaapi.budget.domain.exception.MonthlyBudgetNotFoundException;
import dev.jsamuelap.oikonomiaapi.budget.domain.model.MonthlyBudget;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.CreateMonthlyBudgetCommand;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.MonthlyBudgetView;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.UpdateMonthlyBudgetCommand;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategoryLookupPort;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategorySummary;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.MonthlyBudgetRepository;
import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Monthly budget service")
class MonthlyBudgetServiceTest {
  @Mock
  private MonthlyBudgetRepository monthlyBudgetRepository;
  @Mock
  private CategoryLookupPort categoryLookupPort;

  @InjectMocks
  private MonthlyBudgetService monthlyBudgetService;

  @Nested
  @DisplayName("Get all monthly budgets")
  class GetAll {
    @ParameterizedTest(name = "should reject: {0}")
    @DisplayName("Should reject when year is invalid")
    @ValueSource(shorts = {-431, 0, 2024, 2101})
    void shouldRejectWhenYearIsInvalid(short invalidYear) {
      assertThatThrownBy(() -> monthlyBudgetService.getAll(UUID.randomUUID(), invalidYear))
        .isInstanceOf(DomainException.class).hasMessageContaining("El año debe ser entre");

      verify(monthlyBudgetRepository, never()).findAllByUser(any(), any());
      verify(categoryLookupPort, never()).findByIds(any(), any());
    }

    @Test
    @DisplayName("Should calculate current year when null")
    void shouldCalculateCurrentYearWhenNull() {
      UUID userId = UUID.randomUUID();
      monthlyBudgetService.getAll(userId, null);

      verify(monthlyBudgetRepository).findAllByUser(userId, (short) Year.now().getValue());
    }

    @Test
    @DisplayName("Should return monthly budget view")
    void shouldReturnMonthlyBudgetView() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      Short month = 9;
      Short year = 2026;
      BigDecimal amount = BigDecimal.TEN;
      MonthlyBudget budget = MonthlyBudget.reconstitute(id, userId, categoryId, month, year, amount, null);
      when(monthlyBudgetRepository.findAllByUser(userId, year)).thenReturn(List.of(budget));

      CategorySummary category = new CategorySummary(categoryId, "Groceries", "EXPENSE", false);
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of(categoryId, category));

      MonthlyBudgetView expected = new MonthlyBudgetView(id, userId, month, year, amount, category);

      List<MonthlyBudgetView> budgets = monthlyBudgetService.getAll(userId, year);
      assertThat(budgets).isEqualTo(List.of(expected));

      verify(monthlyBudgetRepository).findAllByUser(userId, year);
      verify(categoryLookupPort).findByIds(Set.of(categoryId), userId);
    }
  }

  @Nested
  @DisplayName("Get monthly budget by ID")
  class GetById {
    @Test
    @DisplayName("Should reject when budget does not exist")
    void shouldRejectWhenBudgetDoesNotExist() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      when(monthlyBudgetRepository.findByIdAndUser(id, userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> monthlyBudgetService.getById(id, userId))
        .isInstanceOf(MonthlyBudgetNotFoundException.class).hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Should return monthly budget view")
    void shouldReturnMonthlyBudgetView() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      Short month = 9;
      Short year = 2026;
      BigDecimal amount = BigDecimal.TEN;
      MonthlyBudget budget = MonthlyBudget.reconstitute(id, userId, categoryId, month, year, amount, null);
      when(monthlyBudgetRepository.findByIdAndUser(id, userId)).thenReturn(Optional.of(budget));

      CategorySummary category = new CategorySummary(categoryId, "Groceries", "EXPENSE", false);
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of(categoryId, category));

      MonthlyBudgetView expected = new MonthlyBudgetView(id, userId, month, year, amount, category);

      MonthlyBudgetView response = monthlyBudgetService.getById(id, userId);
      assertThat(response).isEqualTo(expected);
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
}
