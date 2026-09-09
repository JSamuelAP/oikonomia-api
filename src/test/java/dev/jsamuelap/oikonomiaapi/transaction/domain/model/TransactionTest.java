package dev.jsamuelap.oikonomiaapi.transaction.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

@DisplayName("Transaction domain model")
public class TransactionTest {
  private static final UUID VALID_USER_ID = UUID.randomUUID();
  private static final UUID VALID_CATEGORY_ID = UUID.randomUUID();
  private static final BigDecimal VALID_AMOUNT = new BigDecimal("100.50");
  private static final LocalDate VALID_DATE = LocalDate.now();
  private static final String VALID_NOTES = "Test transaction";

  @Nested
  @DisplayName("Transaction creation")
  class Creation {
    @Test
    @DisplayName("Should create valid transaction with all fields")
    void shouldCreateValidTransaction() {
      Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE,
        VALID_NOTES);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(transaction).as("transaction").isNotNull();
        softly.assertThat(transaction.getId()).as("id").isNotNull();
        softly.assertThat(transaction.getUserId()).as("userId").isEqualTo(VALID_USER_ID);
        softly.assertThat(transaction.getCategoryId()).as("categoryId").isEqualTo(VALID_CATEGORY_ID);
        softly.assertThat(transaction.getAmount()).as("amount").isEqualTo(VALID_AMOUNT);
        softly.assertThat(transaction.getDate()).as("date").isEqualTo(VALID_DATE);
        softly.assertThat(transaction.getNotes()).as("notes").isEqualTo(VALID_NOTES);
        softly.assertThat(transaction.isDeleted()).as("isDeleted").isFalse();
      });
    }

    @Nested
    @DisplayName("User id validation")
    class UserId {
      @Test
      @DisplayName("Should reject null user id")
      void shouldRejectNullUserId() {
        assertThatThrownBy(() -> Transaction.create(null, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE, VALID_NOTES))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede ser nulo");
      }
    }

    @Nested
    @DisplayName("Category id validation")
    class CategoryId {
      @Test
      @DisplayName("Should reject null category id")
      void shouldRejectNullCategoryId() {
        assertThatThrownBy(() -> Transaction.create(VALID_USER_ID, null, VALID_AMOUNT, VALID_DATE, VALID_NOTES))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede ser nulo");
      }
    }

    @Nested
    @DisplayName("Amount validation")
    class Amount {
      @Test
      @DisplayName("Should reject null amount")
      void shouldRejectNullAmount() {
        assertThatThrownBy(() -> Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, null, VALID_DATE, VALID_NOTES))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede ser nula");
      }

      @Test
      @DisplayName("Should reject zero amount")
      void shouldRejectZeroAmount() {
        assertThatThrownBy(
          () -> Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, BigDecimal.ZERO, VALID_DATE, VALID_NOTES))
          .isInstanceOf(DomainException.class).hasMessageContaining("debe ser mayor a 0");
      }

      @Test
      @DisplayName("Should reject negative amount")
      void shouldRejectNegativeAmount() {
        assertThatThrownBy(
          () -> Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, new BigDecimal("-10.00"), VALID_DATE, VALID_NOTES))
          .isInstanceOf(DomainException.class).hasMessageContaining("debe ser mayor a 0");
      }

      @Test
      @DisplayName("Should accept positive amount")
      void shouldAcceptPositiveAmount() {
        BigDecimal amount = new BigDecimal("0.01");
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, amount, VALID_DATE, VALID_NOTES);
        assertThat(transaction.getAmount()).isEqualTo(amount);
      }
    }

    @Nested
    @DisplayName("Date validation")
    class Date {
      @Test
      @DisplayName("Should reject null date")
      void shouldRejectNullDate() {
        assertThatThrownBy(() -> Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, null, VALID_NOTES))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede ser nula");
      }

      @Test
      @DisplayName("Should reject future date")
      void shouldRejectFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        assertThatThrownBy(
          () -> Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, futureDate, VALID_NOTES))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede ser futura");
      }

      @Test
      @DisplayName("Should accept today's date")
      void shouldAcceptTodayDate() {
        LocalDate today = LocalDate.now();
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, today,
          VALID_NOTES);
        assertThat(transaction.getDate()).isEqualTo(today);
      }

      @Test
      @DisplayName("Should accept past date")
      void shouldAcceptPastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, pastDate,
          VALID_NOTES);
        assertThat(transaction.getDate()).isEqualTo(pastDate);
      }
    }

    @Nested
    @DisplayName("Notes validation")
    class Notes {
      @Test
      @DisplayName("Should accept null notes")
      void shouldAcceptNullNotes() {
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE, null);
        assertThat(transaction.getNotes()).isNull();
      }

      @Test
      @DisplayName("Should accept empty notes")
      void shouldAcceptEmptyNotes() {
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE, "");
        assertThat(transaction.getNotes()).isEmpty();
      }

      @Test
      @DisplayName("Should reject notes exceeding maximum length")
      void shouldRejectNotesTooLong() {
        String tooLongNotes = "a".repeat(Transaction.MAX_NOTES_LENGTH + 1);
        assertThatThrownBy(
          () -> Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE, tooLongNotes))
          .isInstanceOf(DomainException.class)
          .hasMessageContaining("no pueden exceder más de %s caracteres".formatted(Transaction.MAX_NOTES_LENGTH));
      }

      @Test
      @DisplayName("Should accept notes at maximum length")
      void shouldAcceptNotesAtMaxLength() {
        String exactlyMaxNotes = "a".repeat(Transaction.MAX_NOTES_LENGTH);
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE,
          exactlyMaxNotes);
        assertThat(transaction.getNotes()).hasSize(Transaction.MAX_NOTES_LENGTH);
      }
    }
  }

  @Nested
  @DisplayName("Transaction reconstitution from persistence")
  class Reconstitution {
    @Test
    @DisplayName("Should reconstitute transaction without re-validating fields")
    void shouldReconstituteWithoutRevalidating() {
      Transaction transaction = Transaction.reconstitute(null, null, null, null, null, null, null);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(transaction).as("transaction").isNotNull();
        softly.assertThat(transaction.getId()).as("id").isNull();
        softly.assertThat(transaction.getUserId()).as("userId").isNull();
        softly.assertThat(transaction.getCategoryId()).as("categoryId").isNull();
        softly.assertThat(transaction.getAmount()).as("amount").isNull();
        softly.assertThat(transaction.getDate()).as("date").isNull();
        softly.assertThat(transaction.getNotes()).as("notes").isNull();
        softly.assertThat(transaction.getDeletedAt()).as("deletedAt").isNull();
      });
    }

    @Test
    @DisplayName("Should reconstitute transaction with all fields")
    void shouldReconstituteWithAllFields() {
      UUID id = UUID.randomUUID();
      Instant deletedAt = Instant.now();
      Transaction transaction = Transaction.reconstitute(id, VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE,
        VALID_NOTES, deletedAt);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(transaction).as("transaction").isNotNull();
        softly.assertThat(transaction.getId()).as("id").isEqualTo(id);
        softly.assertThat(transaction.getUserId()).as("userId").isEqualTo(VALID_USER_ID);
        softly.assertThat(transaction.getCategoryId()).as("categoryId").isEqualTo(VALID_CATEGORY_ID);
        softly.assertThat(transaction.getAmount()).as("amount").isEqualTo(VALID_AMOUNT);
        softly.assertThat(transaction.getDate()).as("date").isEqualTo(VALID_DATE);
        softly.assertThat(transaction.getNotes()).as("notes").isEqualTo(VALID_NOTES);
        softly.assertThat(transaction.isDeleted()).as("isDeleted").isTrue();
        softly.assertThat(transaction.getDeletedAt()).as("deletedAt").isEqualTo(deletedAt);
      });
    }
  }

  @Nested
  @DisplayName("Transaction deletion")
  class Deletion {
    @Test
    @DisplayName("Should mark transaction as deleted")
    void shouldDeleteTransaction() {
      Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE,
        VALID_NOTES);
      transaction.delete();

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(transaction.isDeleted()).as("isDeleted").isTrue();
        softly.assertThat(transaction.getDeletedAt()).as("deletedAt").isNotNull();
      });
    }

    @Test
    @DisplayName("Should reject deleting already deleted transaction")
    void shouldRejectDeletingAlreadyDeletedTransaction() {
      Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE,
        VALID_NOTES);
      transaction.delete();

      assertThatThrownBy(transaction::delete).isInstanceOf(DomainException.class)
        .hasMessageContaining("ya está eliminada");
    }
  }

  @Nested
  @DisplayName("Transaction mutation")
  class Mutation {
    @Nested
    @DisplayName("Change category id")
    class ChangeCategoryId {
      @Test
      @DisplayName("Should change category id")
      void shouldChangeCategoryId() {
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE,
          VALID_NOTES);
        UUID newCategoryId = UUID.randomUUID();
        transaction.changeCategoryId(newCategoryId);

        assertThat(transaction.getCategoryId()).isEqualTo(newCategoryId);
      }

      @Test
      @DisplayName("Should reject null category id")
      void shouldRejectNullCategoryId() {
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE,
          VALID_NOTES);

        assertThatThrownBy(() -> transaction.changeCategoryId(null)).isInstanceOf(DomainException.class)
          .hasMessageContaining("no puede ser nulo");
      }
    }

    @Nested
    @DisplayName("Change amount")
    class ChangeAmount {
      @Test
      @DisplayName("Should change amount")
      void shouldChangeAmount() {
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE,
          VALID_NOTES);
        BigDecimal newAmount = new BigDecimal("250.00");
        transaction.changeAmount(newAmount);

        assertThat(transaction.getAmount()).isEqualTo(newAmount);
      }

      @Test
      @DisplayName("Should reject invalid amount")
      void shouldRejectInvalidAmount() {
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE,
          VALID_NOTES);

        assertThatThrownBy(() -> transaction.changeAmount(null)).isInstanceOf(DomainException.class)
          .hasMessageContaining("no puede ser nula");
      }
    }

    @Nested
    @DisplayName("Change date")
    class ChangeDate {
      @Test
      @DisplayName("Should change date")
      void shouldChangeDate() {
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE,
          VALID_NOTES);
        LocalDate newDate = LocalDate.now().minusDays(10);
        transaction.changeDate(newDate);

        assertThat(transaction.getDate()).isEqualTo(newDate);
      }

      @Test
      @DisplayName("Should reject invalid date")
      void shouldRejectInvalidDate() {
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE,
          VALID_NOTES);

        assertThatThrownBy(() -> transaction.changeDate(null)).isInstanceOf(DomainException.class)
          .hasMessageContaining("no puede ser nula");
      }
    }

    @Nested
    @DisplayName("Change notes")
    class ChangeNotes {
      @Test
      @DisplayName("Should change notes")
      void shouldChangeNotes() {
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE,
          VALID_NOTES);
        String newNotes = "Updated notes";
        transaction.changeNotes(newNotes);

        assertThat(transaction.getNotes()).isEqualTo(newNotes);
      }

      @Test
      @DisplayName("Should reject notes exceeding maximum length")
      void shouldRejectNotesTooLong() {
        Transaction transaction = Transaction.create(VALID_USER_ID, VALID_CATEGORY_ID, VALID_AMOUNT, VALID_DATE,
          VALID_NOTES);
        String tooLongNotes = "a".repeat(Transaction.MAX_NOTES_LENGTH + 1);

        assertThatThrownBy(() -> transaction.changeNotes(tooLongNotes)).isInstanceOf(DomainException.class)
          .hasMessageContaining("no pueden exceder más de %s caracteres".formatted(Transaction.MAX_NOTES_LENGTH));
      }
    }
  }
}
