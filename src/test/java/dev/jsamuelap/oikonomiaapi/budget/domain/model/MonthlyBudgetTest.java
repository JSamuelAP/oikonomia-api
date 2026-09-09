package dev.jsamuelap.oikonomiaapi.budget.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

@DisplayName("Monthly budget domain model")
public class MonthlyBudgetTest {
  private static final UUID VALID_USER_ID = UUID.randomUUID();
  private static final UUID VALID_CATEGORY_ID = UUID.randomUUID();
  private static final Short VALID_MONTH = 6;
  private static final Short VALID_YEAR = 2026;
  private static final BigDecimal VALID_AMOUNT = new BigDecimal("500.00");

  @Nested
  @DisplayName("Monthly budget creation")
  class Creation {
    @Test
    @DisplayName("Should create valid monthly budget with all fields")
    void shouldCreateValidMonthlyBudget() {
      MonthlyBudget budget = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR,
        VALID_AMOUNT);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(budget).as("budget").isNotNull();
        softly.assertThat(budget.getId()).as("id").isNotNull();
        softly.assertThat(budget.getUserId()).as("userId").isEqualTo(VALID_USER_ID);
        softly.assertThat(budget.getCategoryId()).as("categoryId").isEqualTo(VALID_CATEGORY_ID);
        softly.assertThat(budget.getMonth()).as("month").isEqualTo(VALID_MONTH);
        softly.assertThat(budget.getYear()).as("year").isEqualTo(VALID_YEAR);
        softly.assertThat(budget.getExpectedAmount()).as("expectedAmount").isEqualTo(VALID_AMOUNT);
        softly.assertThat(budget.isDeleted()).as("isDeleted").isFalse();
      });
    }

    @Nested
    @DisplayName("User id validation")
    class UserId {
      @Test
      @DisplayName("Should reject null user id")
      void shouldRejectNullUserId() {
        assertThatThrownBy(() -> MonthlyBudget.create(null, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR, VALID_AMOUNT))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede ser nulo");
      }
    }

    @Nested
    @DisplayName("Category id validation")
    class CategoryId {
      @Test
      @DisplayName("Should reject null category id")
      void shouldRejectNullCategoryId() {
        assertThatThrownBy(() -> MonthlyBudget.create(VALID_USER_ID, null, VALID_MONTH, VALID_YEAR, VALID_AMOUNT))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede ser nulo");
      }
    }

    @Nested
    @DisplayName("Month validation")
    class Month {
      @Test
      @DisplayName("Should reject null month")
      void shouldRejectNullMonth() {
        assertThatThrownBy(() -> MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, null, VALID_YEAR, VALID_AMOUNT))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede ser nulo");
      }

      @Test
      @DisplayName("Should reject month less than minimum")
      void shouldRejectMonthLessThanMin() {
        assertThatThrownBy(() -> MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID,
          (short) (MonthlyBudget.MIN_MONTH - 1), VALID_YEAR, VALID_AMOUNT)).isInstanceOf(DomainException.class)
          .hasMessageContaining("El mes debe ser entre");
      }

      @Test
      @DisplayName("Should reject month greater than maximum")
      void shouldRejectMonthGreaterThanMax() {
        assertThatThrownBy(() -> MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID,
          (short) (MonthlyBudget.MAX_MONTH + 1), VALID_YEAR, VALID_AMOUNT)).isInstanceOf(DomainException.class)
          .hasMessageContaining("El mes debe ser entre");
      }

      @Test
      @DisplayName("Should accept month boundaries")
      void shouldAcceptMonthBoundaries() {
        MonthlyBudget budgetMin = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, MonthlyBudget.MIN_MONTH,
          VALID_YEAR, VALID_AMOUNT);
        MonthlyBudget budgetMax = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, MonthlyBudget.MAX_MONTH,
          VALID_YEAR, VALID_AMOUNT);

        assertThat(budgetMin.getMonth()).isEqualTo(MonthlyBudget.MIN_MONTH);
        assertThat(budgetMax.getMonth()).isEqualTo(MonthlyBudget.MAX_MONTH);
      }
    }

    @Nested
    @DisplayName("Year validation")
    class Year {
      @Test
      @DisplayName("Should reject null year")
      void shouldRejectNullYear() {
        assertThatThrownBy(
          () -> MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, null, VALID_AMOUNT))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede ser nulo");
      }

      @Test
      @DisplayName("Should reject year less than minimum")
      void shouldRejectYearLessThanMin() {
        assertThatThrownBy(() -> MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH,
          (short) (MonthlyBudget.MIN_YEAR - 1), VALID_AMOUNT)).isInstanceOf(DomainException.class)
          .hasMessageContaining("El año debe ser entre");
      }

      @Test
      @DisplayName("Should reject year greater than maximum")
      void shouldRejectYearGreaterThanMax() {
        assertThatThrownBy(() -> MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH,
          (short) (MonthlyBudget.MAX_YEAR + 1), VALID_AMOUNT)).isInstanceOf(DomainException.class)
          .hasMessageContaining("El año debe ser entre");
      }

      @Test
      @DisplayName("Should accept year boundaries")
      void shouldAcceptYearBoundaries() {
        MonthlyBudget budgetMin = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH,
          MonthlyBudget.MIN_YEAR, VALID_AMOUNT);
        MonthlyBudget budgetMax = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH,
          MonthlyBudget.MAX_YEAR, VALID_AMOUNT);

        assertThat(budgetMin.getYear()).isEqualTo(MonthlyBudget.MIN_YEAR);
        assertThat(budgetMax.getYear()).isEqualTo(MonthlyBudget.MAX_YEAR);
      }
    }

    @Nested
    @DisplayName("Expected amount validation")
    class ExpectedAmount {
      @Test
      @DisplayName("Should reject null expected amount")
      void shouldRejectNullExpectedAmount() {
        assertThatThrownBy(() -> MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR, null))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede ser nula");
      }

      @Test
      @DisplayName("Should reject zero expected amount")
      void shouldRejectZeroExpectedAmount() {
        assertThatThrownBy(
          () -> MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR, BigDecimal.ZERO))
          .isInstanceOf(DomainException.class).hasMessageContaining("debe ser mayor a 0");
      }

      @Test
      @DisplayName("Should reject negative expected amount")
      void shouldRejectNegativeExpectedAmount() {
        assertThatThrownBy(() -> MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR,
          new BigDecimal("-10.00"))).isInstanceOf(DomainException.class).hasMessageContaining("debe ser mayor a 0");
      }

      @Test
      @DisplayName("Should accept positive expected amount")
      void shouldAcceptPositiveExpectedAmount() {
        BigDecimal amount = new BigDecimal("0.01");
        MonthlyBudget budget = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR, amount);
        assertThat(budget.getExpectedAmount()).isEqualTo(amount);
      }
    }
  }

  @Nested
  @DisplayName("Monthly budget reconstitution from persistence")
  class Reconstitution {
    @Test
    @DisplayName("Should reconstitute monthly budget without re-validating fields")
    void shouldReconstituteWithoutRevalidating() {
      MonthlyBudget budget = MonthlyBudget.reconstitute(null, null, null, null, null, null, null);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(budget).as("budget").isNotNull();
        softly.assertThat(budget.getId()).as("id").isNull();
        softly.assertThat(budget.getUserId()).as("userId").isNull();
        softly.assertThat(budget.getCategoryId()).as("categoryId").isNull();
        softly.assertThat(budget.getMonth()).as("month").isNull();
        softly.assertThat(budget.getYear()).as("year").isNull();
        softly.assertThat(budget.getExpectedAmount()).as("expectedAmount").isNull();
        softly.assertThat(budget.getDeletedAt()).as("deletedAt").isNull();
      });
    }

    @Test
    @DisplayName("Should reconstitute monthly budget with all fields")
    void shouldReconstituteWithAllFields() {
      UUID id = UUID.randomUUID();
      Instant deletedAt = Instant.now();

      MonthlyBudget budget = MonthlyBudget.reconstitute(id, VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR,
        VALID_AMOUNT, deletedAt);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(budget).as("budget").isNotNull();
        softly.assertThat(budget.getId()).as("id").isEqualTo(id);
        softly.assertThat(budget.getUserId()).as("userId").isEqualTo(VALID_USER_ID);
        softly.assertThat(budget.getCategoryId()).as("categoryId").isEqualTo(VALID_CATEGORY_ID);
        softly.assertThat(budget.getMonth()).as("month").isEqualTo(VALID_MONTH);
        softly.assertThat(budget.getYear()).as("year").isEqualTo(VALID_YEAR);
        softly.assertThat(budget.getExpectedAmount()).as("expectedAmount").isEqualTo(VALID_AMOUNT);
        softly.assertThat(budget.isDeleted()).as("isDeleted").isTrue();
        softly.assertThat(budget.getDeletedAt()).as("deletedAt").isEqualTo(deletedAt);
      });
    }
  }

  @Nested
  @DisplayName("Monthly budget deletion")
  class Deletion {
    @Test
    @DisplayName("Should mark monthly budget as deleted")
    void shouldDeleteMonthlyBudget() {
      MonthlyBudget budget = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR,
        VALID_AMOUNT);
      budget.delete();

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(budget.isDeleted()).as("isDeleted").isTrue();
        softly.assertThat(budget.getDeletedAt()).as("deletedAt").isNotNull();
      });
    }

    @Test
    @DisplayName("Should reject deleting already deleted monthly budget")
    void shouldRejectDeletingAlreadyDeletedMonthlyBudget() {
      MonthlyBudget budget = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR,
        VALID_AMOUNT);
      budget.delete();

      assertThatThrownBy(budget::delete).isInstanceOf(DomainException.class).hasMessageContaining("ya está eliminado");
    }
  }

  @Nested
  @DisplayName("Monthly budget mutation")
  class Mutation {
    @Nested
    @DisplayName("Change category id")
    class ChangeCategoryId {
      @Test
      @DisplayName("Should change category id")
      void shouldChangeCategoryId() {
        MonthlyBudget budget = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR,
          VALID_AMOUNT);
        UUID newCategoryId = UUID.randomUUID();
        budget.changeCategoryId(newCategoryId);

        assertThat(budget.getCategoryId()).isEqualTo(newCategoryId);
      }

      @Test
      @DisplayName("Should reject null category id")
      void shouldRejectNullCategoryId() {
        MonthlyBudget budget = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR,
          VALID_AMOUNT);

        assertThatThrownBy(() -> budget.changeCategoryId(null)).isInstanceOf(DomainException.class)
          .hasMessageContaining("no puede ser nulo");
      }
    }

    @Nested
    @DisplayName("Change month")
    class ChangeMonth {
      @Test
      @DisplayName("Should change month")
      void shouldChangeMonth() {
        MonthlyBudget budget = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR,
          VALID_AMOUNT);
        budget.changeMonth(MonthlyBudget.MAX_MONTH);

        assertThat(budget.getMonth()).isEqualTo(MonthlyBudget.MAX_MONTH);
      }

      @Test
      @DisplayName("Should reject invalid month")
      void shouldRejectInvalidMonth() {
        MonthlyBudget budget = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR,
          VALID_AMOUNT);

        assertThatThrownBy(() -> budget.changeMonth(null)).isInstanceOf(DomainException.class)
          .hasMessageContaining("no puede ser nulo");
      }
    }

    @Nested
    @DisplayName("Change year")
    class ChangeYear {
      @Test
      @DisplayName("Should change year")
      void shouldChangeYear() {
        MonthlyBudget budget = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR,
          VALID_AMOUNT);
        budget.changeYear(MonthlyBudget.MAX_YEAR);

        assertThat(budget.getYear()).isEqualTo(MonthlyBudget.MAX_YEAR);
      }

      @Test
      @DisplayName("Should reject invalid year")
      void shouldRejectInvalidYear() {
        MonthlyBudget budget = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR,
          VALID_AMOUNT);

        assertThatThrownBy(() -> budget.changeYear(null)).isInstanceOf(DomainException.class)
          .hasMessageContaining("no puede ser nulo");
      }
    }

    @Nested
    @DisplayName("Change expected amount")
    class ChangeExpectedAmount {
      @Test
      @DisplayName("Should change expected amount")
      void shouldChangeExpectedAmount() {
        MonthlyBudget budget = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR,
          VALID_AMOUNT);
        BigDecimal newAmount = new BigDecimal("750.00");
        budget.changeExpectedAmount(newAmount);

        assertThat(budget.getExpectedAmount()).isEqualTo(newAmount);
      }

      @Test
      @DisplayName("Should reject invalid expected amount")
      void shouldRejectInvalidExpectedAmount() {
        MonthlyBudget budget = MonthlyBudget.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_MONTH, VALID_YEAR,
          VALID_AMOUNT);

        assertThatThrownBy(() -> budget.changeExpectedAmount(null)).isInstanceOf(DomainException.class)
          .hasMessageContaining("no puede ser nula");
      }
    }
  }
}
