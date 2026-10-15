package dev.jsamuelap.oikonomiaapi.budget.infrastructure.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import dev.jsamuelap.oikonomiaapi.budget.domain.model.MonthlyBudget;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.MonthlyBudgetRepositoryPort;
import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;
import dev.jsamuelap.oikonomiaapi.support.IntegrationTestSupport;

@DisplayName("Monthly budget repository adapter")
public class MonthlyBudgetRepositoryAdapterTest extends IntegrationTestSupport {
  @Autowired
  private MonthlyBudgetRepositoryPort budgetRepository;

  @Nested
  @DisplayName("Save monthly budget")
  class Save {
    @Test
    @DisplayName("Should persist all fields")
    void shouldPersistAllFields() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      MonthlyBudget budget = MonthlyBudget.create(userId, categoryId, (short) 10, (short) 2026,
        new BigDecimal("200.00"));
      budgetRepository.save(budget);

      MonthlyBudget found = budgetRepository.findByIdAndUser(budget.getId(), userId).orElseThrow();
      assertThat(found).usingRecursiveComparison().isEqualTo(budget);
    }

    @ParameterizedTest
    @DisplayName("Should reject invalid month")
    @ValueSource(shorts = {-1, 0, 13})
    void shouldRejectInvalidMonth(short month) {
      MonthlyBudget budget = budgetFor(month, (short) 2026, "200.00");

      assertThatThrownBy(() -> budgetRepository.save(budget)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @ParameterizedTest
    @DisplayName("Should reject invalid year")
    @ValueSource(shorts = {-431, 2024, 2101})
    void shouldRejectInvalidYear(short year) {
      MonthlyBudget budget = budgetFor((short) 10, year, "200.00");

      assertThatThrownBy(() -> budgetRepository.save(budget)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @ParameterizedTest
    @DisplayName("Should reject invalid amount")
    @ValueSource(strings = {"-200.00", "-0.01", "0.00"})
    void shouldRejectInvalidAmount(String amount) {
      MonthlyBudget budget = budgetFor((short) 10, (short) 2026, amount);

      assertThatThrownBy(() -> budgetRepository.save(budget)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @ParameterizedTest
    @DisplayName("Should accept valid month")
    @ValueSource(shorts = {1, 12})
    void shouldAcceptValidMonth(short month) {
      MonthlyBudget budget = budgetFor(month, (short) 2026, "200.00");

      assertThatCode(() -> budgetRepository.save(budget)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @DisplayName("Should accept valid year")
    @ValueSource(shorts = {2025, 2100})
    void shouldAcceptValidYear(short year) {
      MonthlyBudget budget = budgetFor((short) 10, year, "200.00");

      assertThatCode(() -> budgetRepository.save(budget)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should persist smallest positive amount")
    void shouldPersistSmallestPositiveAmount() {
      MonthlyBudget budget = budgetFor((short) 10, (short) 2026, "0.01");

      assertThatCode(() -> budgetRepository.save(budget)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should persist maximum amount")
    void shouldPersistMaximumAmount() {
      MonthlyBudget budget = budgetFor((short) 10, (short) 2026, "9999999999.99");

      assertThatCode(() -> budgetRepository.save(budget)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject amount that overflows the column")
    void shouldRejectAmountThatOverflowsTheColumn() {
      MonthlyBudget budget = budgetFor((short) 10, (short) 2026, "10000000000.00");

      assertThatThrownBy(() -> budgetRepository.save(budget)).isInstanceOf(DataIntegrityViolationException.class);
    }
  }

  @Nested
  @DisplayName("Unique active budget constraint")
  class UniqueActiveBudget {
    @Test
    @DisplayName("Should allow recreating budget after soft delete")
    void shouldAllowRecreatingBudgetAfterSoftDelete() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      budgetRepository.save(MonthlyBudget.reconstitute(UUID.randomUUID(), userId, categoryId, (short) 10, (short) 2026,
        new BigDecimal("200.00"), Instant.now()));

      MonthlyBudget newBudget = MonthlyBudget.create(userId, categoryId, (short) 10, (short) 2026,
        new BigDecimal("200.00"));
      assertThatCode(() -> budgetRepository.save(newBudget)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject two active budgets with same category and month")
    void shouldRejectTwoActiveBudgetsWithSameCategoryAndMonth() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      budgetRepository
        .save(MonthlyBudget.create(userId, categoryId, (short) 10, (short) 2026, new BigDecimal("200.00")));

      MonthlyBudget budget = MonthlyBudget.create(userId, categoryId, (short) 10, (short) 2026,
        new BigDecimal("200.00"));
      assertThatThrownBy(() -> budgetRepository.save(budget)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should allow same month for different category")
    void shouldAllowSameMonthForDifferentCategory() {
      UUID userId = insertUser();
      newMonthlyBudgetOn(userId, insertCategory(userId, "A", FlowType.EXPENSE), 10, 2026);

      assertThatCode(() -> newMonthlyBudgetOn(userId, insertCategory(userId, "B", FlowType.EXPENSE), 10, 2026))
        .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @DisplayName("Should allow same category in another month or year")
    @CsvSource({"11, 2026", "10, 2027"})
    void shouldAllowSameCategoryInAnotherMonthOrYear(short month, short year) {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "A", FlowType.EXPENSE);
      newMonthlyBudgetOn(userId, categoryId, 10, 2026);

      assertThatCode(() -> newMonthlyBudgetOn(userId, categoryId, month, year)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject moving budget into an occupied slot")
    void shouldRejectMovingBudgetIntoAnOccupiedSlot() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "A", FlowType.EXPENSE);
      newMonthlyBudgetOn(userId, categoryId, 10, 2026);

      MonthlyBudget other = newMonthlyBudgetOn(userId, categoryId, 11, 2026);
      other.changeMonth((short) 10);

      assertThatThrownBy(() -> budgetRepository.save(other)).isInstanceOf(DataIntegrityViolationException.class);
    }
  }

  @Nested
  @DisplayName("Update monthly budget")
  class Update {
    @Test
    @DisplayName("Should persist every mutable field")
    void shouldPersistEveryMutableField() {
      UUID userId = insertUser();
      UUID originalCategory = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      UUID newCategory = insertCategory(userId, "Salary", FlowType.INCOME);
      MonthlyBudget budget = newMonthlyBudgetOn(userId, originalCategory, 10, 2026);

      budget.changeCategoryId(newCategory);
      budget.changeMonth((short) 9);
      budget.changeYear((short) 2025);
      budget.changeExpectedAmount(new BigDecimal("1500.50"));
      budgetRepository.save(budget);

      MonthlyBudget found = budgetRepository.findByIdAndUser(budget.getId(), userId).orElseThrow();
      assertThat(found).usingRecursiveComparison().isEqualTo(budget);
    }
  }

  @Nested
  @DisplayName("Soft delete monthly budget")
  class SoftDelete {
    @Test
    @DisplayName("Should persist deleted at when inserted already deleted")
    void shouldPersistDeletedAtWhenInsertedAlreadyDeleted() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      Instant deletedAt = Instant.now();
      MonthlyBudget deleted = MonthlyBudget.reconstitute(UUID.randomUUID(), userId, categoryId, (short) 10,
        (short) 2026, new BigDecimal("200.00"), deletedAt);
      budgetRepository.save(deleted);

      OffsetDateTime stored = jdbc.queryForObject("SELECT deleted_at FROM monthly_budgets WHERE id = ?",
        OffsetDateTime.class, deleted.getId());
      assertThat(stored.toInstant()).isCloseTo(deletedAt, within(1, ChronoUnit.MILLIS));
    }

    @Test
    @DisplayName("Should persist deletion of an existing budget")
    void shouldPersistDeletionOfAnExistingBudget() {
      MonthlyBudget deleted = deleteMonthlyBudget(insertUser(), FlowType.EXPENSE);

      Boolean isDeleted = jdbc.queryForObject("SELECT deleted_at IS NOT NULL FROM monthly_budgets WHERE id = ?",
        Boolean.class, deleted.getId());
      assertThat(isDeleted).isTrue();
    }
  }

  @Nested
  @DisplayName("Find by user and month")
  class FindByUserAndMonth {
    private static final YearMonth MARCH = YearMonth.of(2025, 3);

    @Test
    @DisplayName("Should return only active of the user")
    void shouldReturnOnlyActiveOfTheUser() {
      UUID userId = insertUser();
      MonthlyBudget budget = newMonthlyBudget(userId, FlowType.EXPENSE);
      deleteMonthlyBudget(userId, FlowType.INCOME);
      newMonthlyBudget(insertUser(), FlowType.EXPENSE);

      assertThat(budgetRepository.findAllByUser(userId, YearMonth.of(2026, 10))).extracting(MonthlyBudget::getId)
        .containsExactly(budget.getId());
    }

    @Test
    @DisplayName("Should include boundary months and exclude adjacent ones")
    void shouldIncludeBoundaryMonthsAndExcludeAdjacentOnes() {
      UUID userId = insertUser();
      UUID categoryId1 = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      UUID categoryId2 = insertCategory(userId, "Salary", FlowType.INCOME);
      newMonthlyBudgetOn(userId, categoryId1, 2, 2025);
      MonthlyBudget budget1 = newMonthlyBudgetOn(userId, categoryId1, 3, 2025);
      MonthlyBudget budget2 = newMonthlyBudgetOn(userId, categoryId2, 3, 2025);
      newMonthlyBudgetOn(userId, categoryId1, 4, 2025);

      assertThat(budgetRepository.findAllByUser(userId, MARCH)).extracting(MonthlyBudget::getId)
        .containsExactlyInAnyOrder(budget1.getId(), budget2.getId());
    }

    @Test
    @DisplayName("Should return empty when month has not budgets")
    void shouldReturnEmptyWhenMonthHasNotBudgets() {
      UUID userId = insertUser();
      newMonthlyBudget(userId, FlowType.EXPENSE);

      assertThat(budgetRepository.findAllByUser(userId, MARCH)).isEmpty();
    }
  }

  @Nested
  @DisplayName("Find by ID and user")
  class FindByIdAndUser {
    @Test
    @DisplayName("Should not find deleted budget")
    void shouldNotFindDeletedBudget() {
      UUID userId = insertUser();
      MonthlyBudget budget = deleteMonthlyBudget(userId, FlowType.EXPENSE);

      assertThat(budgetRepository.findByIdAndUser(budget.getId(), userId)).isEmpty();
    }

    @Test
    @DisplayName("Should not find budget of another user")
    void shouldNotFindBudgetOfAnotherUser() {
      MonthlyBudget budget = newMonthlyBudget(insertUser(), FlowType.EXPENSE);

      assertThat(budgetRepository.findByIdAndUser(budget.getId(), insertUser())).isEmpty();
    }
  }

  @Nested
  @DisplayName("Exists by category, user and date")
  class ExistsByCategoryAndUserAndDate {
    @Test
    @DisplayName("Should exist when active budget matched")
    void shouldExistWhenActiveBudgetMatched() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      newMonthlyBudgetOn(userId, categoryId, 10, 2026);

      assertThat(exists(userId, categoryId, (short) 10, (short) 2026)).isTrue();
    }

    @Test
    @DisplayName("Should not exist when deleted budget matched")
    void shouldNotExistWhenDeletedBudgetMatched() {
      UUID userId = insertUser();
      MonthlyBudget deleted = deleteMonthlyBudget(userId, FlowType.EXPENSE);

      assertThat(exists(deleted.getCategoryId(), userId, (short) 10, (short) 2026)).isFalse();
    }

    @Test
    @DisplayName("Should not exist for different month, year, category or user")
    void shouldNotExistsForDifferentMontOrYearOrCategoryOrUser() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      newMonthlyBudgetOn(userId, categoryId, 10, 2026);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(exists(categoryId, userId, (short) 11, (short) 2026)).as("other month").isFalse();
        softly.assertThat(exists(categoryId, userId, (short) 10, (short) 2025)).as("other year").isFalse();
        softly.assertThat(exists(insertCategory(userId, "Any", FlowType.EXPENSE), userId, (short) 10, (short) 2026))
          .as("other category").isFalse();
        softly.assertThat(exists(categoryId, insertUser(), (short) 10, (short) 2026)).as("other user").isFalse();
      });
    }
  }

  private MonthlyBudget budgetFor(short month, short year, String amount) {
    UUID userId = insertUser();
    UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
    return MonthlyBudget.reconstitute(UUID.randomUUID(), userId, categoryId, month, year, new BigDecimal(amount), null);
  }

  private MonthlyBudget newMonthlyBudget(UUID userId, FlowType flowType) {
    UUID categoryId = insertCategory(userId, "Cat-" + UUID.randomUUID(), flowType);
    return budgetRepository
      .save(MonthlyBudget.create(userId, categoryId, (short) 10, (short) 2026, new BigDecimal("200.00")));
  }

  private MonthlyBudget newMonthlyBudgetOn(UUID userId, UUID categoryId, int month, int year) {
    return budgetRepository
      .save(MonthlyBudget.create(userId, categoryId, (short) month, (short) year, new BigDecimal("200.00")));
  }

  private MonthlyBudget deleteMonthlyBudget(UUID userId, FlowType flowType) {
    MonthlyBudget budget = newMonthlyBudget(userId, flowType);
    budget.delete();
    return budgetRepository.save(budget);
  }

  private boolean exists(UUID userId, UUID categoryId, int month, int year) {
    return budgetRepository.existsByCategoryAndUserAndDate(categoryId, userId, (short) month, (short) year);
  }
}
