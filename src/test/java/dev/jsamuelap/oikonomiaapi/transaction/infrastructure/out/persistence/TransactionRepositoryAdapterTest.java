package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;
import dev.jsamuelap.oikonomiaapi.support.IntegrationTestSupport;
import dev.jsamuelap.oikonomiaapi.transaction.domain.model.Transaction;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionDetail;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionRepositoryPort;

@DisplayName("Transaction repository adapter")
public class TransactionRepositoryAdapterTest extends IntegrationTestSupport {
  @Autowired
  private TransactionRepositoryPort transactionRepository;

  @Nested
  @DisplayName("Save transaction")
  class Save {
    @Test
    @DisplayName("Should persist all fields")
    void shouldPersistAllFields() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      Transaction transaction = Transaction.create(userId, categoryId, new BigDecimal("200.00"),
        LocalDate.of(2026, 10, 13), "");
      transactionRepository.save(transaction);

      Transaction found = transactionRepository.findByIdAndUser(transaction.getId(), userId).orElseThrow();
      assertThat(found).usingRecursiveComparison().isEqualTo(transaction);
    }

    @ParameterizedTest
    @DisplayName("Should reject invalid amount")
    @ValueSource(strings = {"-200.00", "-0.01", "0.00"})
    void shouldRejectInvalidAmount(String amount) {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      Transaction transaction = Transaction.reconstitute(UUID.randomUUID(), userId, categoryId, new BigDecimal(amount),
        LocalDate.of(2026, 10, 12), "", null);

      assertThatThrownBy(() -> transactionRepository.save(transaction))
        .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should persist smallest positive amount")
    void shouldPersistSmallestPositiveAmount() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      Transaction transaction = Transaction.create(userId, categoryId, new BigDecimal("0.01"),
        LocalDate.of(2026, 10, 12), "");

      assertThatCode(() -> transactionRepository.save(transaction)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should persist maximum amount")
    void shouldPersistMaximumAmount() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      Transaction transaction = Transaction.create(userId, categoryId, new BigDecimal("9999999999.99"),
        LocalDate.of(2026, 10, 12), "");

      assertThatCode(() -> transactionRepository.save(transaction)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject amount that overflows the column")
    void shouldRejectAmountThatOverflowsTheColumn() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      Transaction transaction = Transaction.reconstitute(UUID.randomUUID(), userId, categoryId,
        new BigDecimal("10000000000.00"), LocalDate.of(2026, 10, 12), "", null);

      assertThatThrownBy(() -> transactionRepository.save(transaction))
        .isInstanceOf(DataIntegrityViolationException.class);
    }
  }

  @Nested
  @DisplayName("Update transaction")
  class Update {
    @Test
    @DisplayName("Should persist every mutable field")
    void shouldPersistEveryMutableField() {
      UUID userId = insertUser();
      UUID originalCategory = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      UUID newCategory = insertCategory(userId, "Salary", FlowType.INCOME);
      Transaction transaction = newTransactionOn(userId, originalCategory, LocalDate.of(2026, 10, 12));

      transaction.changeCategoryId(newCategory);
      transaction.changeAmount(new BigDecimal("50.50"));
      transaction.changeDate(LocalDate.of(2026, 10, 9));
      transaction.changeNotes("Updated");
      transactionRepository.save(transaction);

      Transaction found = transactionRepository.findByIdAndUser(transaction.getId(), userId).orElseThrow();
      assertThat(found).usingRecursiveComparison().isEqualTo(transaction);
    }
  }

  @Nested
  @DisplayName("Soft delete transaction")
  class SoftDelete {
    @Test
    @DisplayName("Should persist deleted at when inserted already deleted")
    void shouldPersistDeletedAtWhenInsertedAlreadyDeleted() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      Instant deletedAt = Instant.now();
      Transaction deleted = Transaction.reconstitute(UUID.randomUUID(), userId, categoryId, new BigDecimal("200.00"),
        LocalDate.of(2026, 10, 12), "", deletedAt);
      transactionRepository.save(deleted);

      OffsetDateTime stored = jdbc.queryForObject("SELECT deleted_at FROM transactions WHERE id = ?",
        OffsetDateTime.class, deleted.getId());
      assertThat(stored.toInstant()).isCloseTo(deletedAt, within(1, ChronoUnit.MILLIS));
    }

    @Test
    @DisplayName("Should persist deletion of an existing transaction")
    void shouldPersistDeletionOfAnExistingTransaction() {
      Transaction deleted = deleteTransaction(insertUser(), FlowType.EXPENSE);

      Boolean isDeleted = jdbc.queryForObject("SELECT deleted_at IS NOT NULL FROM transactions WHERE id = ?",
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
      Transaction transaction = newTransaction(userId, FlowType.EXPENSE);
      deleteTransaction(userId, FlowType.INCOME);
      newTransaction(insertUser(), FlowType.EXPENSE);

      assertThat(transactionRepository.findByUser(userId, YearMonth.of(2026, 10))).extracting(Transaction::getId)
        .containsExactly(transaction.getId());
    }

    @Test
    @DisplayName("Should include boundary days and exclude adjacent ones")
    void shouldIncludeBoundaryDaysAndExcludeAdjacentOnes() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      newTransactionOn(userId, categoryId, LocalDate.of(2025, 2, 28));
      Transaction first = newTransactionOn(userId, categoryId, LocalDate.of(2025, 3, 1));
      Transaction last = newTransactionOn(userId, categoryId, LocalDate.of(2025, 3, 31));
      newTransactionOn(userId, categoryId, LocalDate.of(2025, 4, 1));

      assertThat(transactionRepository.findByUser(userId, MARCH)).extracting(Transaction::getId)
        .containsExactlyInAnyOrder(first.getId(), last.getId());
    }

    @ParameterizedTest
    @DisplayName("Should handle year rollover and leap year")
    @CsvSource({"2025-12, 2025-12-31, 2026-01-01", "2024-02, 2024-02-29, 2024-03-01"})
    void shouldHandleYearRolloverAndLeapYear(YearMonth month, LocalDate lastDay, LocalDate nextDay) {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      Transaction inside = newTransactionOn(userId, categoryId, lastDay);
      newTransactionOn(userId, categoryId, nextDay);

      assertThat(transactionRepository.findByUser(userId, month)).extracting(Transaction::getId)
        .containsExactly(inside.getId());
    }

    @Test
    @DisplayName("Should return empty when month has not transactions")
    void shouldReturnEmptyWhenMonthHasNotTransactions() {
      UUID userId = insertUser();
      newTransaction(userId, FlowType.EXPENSE);

      assertThat(transactionRepository.findByUser(userId, YearMonth.of(2025, 11))).isEmpty();
    }

    @Test
    @DisplayName("Should return transactions ordered by date descending")
    void shouldReturnTransactionsOrderedByDateDescending() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);

      Transaction day1 = newTransactionOn(userId, categoryId, LocalDate.of(2025, 3, 1));
      Transaction day15 = newTransactionOn(userId, categoryId, LocalDate.of(2025, 3, 15));
      Transaction day31 = newTransactionOn(userId, categoryId, LocalDate.of(2025, 3, 31));
      Transaction day10 = newTransactionOn(userId, categoryId, LocalDate.of(2025, 3, 10));

      assertThat(transactionRepository.findByUser(userId, YearMonth.of(2025, 3))).extracting(Transaction::getId)
        .containsExactly(day31.getId(), day15.getId(), day10.getId(), day1.getId());
    }
  }

  @Nested
  @DisplayName("Find by ID and user")
  class FindByIdAndUser {
    @Test
    @DisplayName("Should not find transaction of another user")
    void shouldNotFindTransactionOfAnotherUser() {
      Transaction transaction = newTransaction(insertUser(), FlowType.EXPENSE);

      assertThat(transactionRepository.findByIdAndUser(transaction.getId(), insertUser())).isEmpty();
    }
  }

  @Nested
  @DisplayName("Find detail by ID and user")
  class FindDetailByIdAndUser {
    @Test
    @DisplayName("Should return audit timestamps")
    void shouldReturnAuditTimestamps() {
      UUID userId = insertUser();
      Transaction transaction = newTransaction(userId, FlowType.EXPENSE);

      TransactionDetail detail = transactionRepository.findDetailByIdAndUser(transaction.getId(), userId).orElseThrow();
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(detail.createdAt()).as("createdAt").isNotNull();
        softly.assertThat(detail.updatedAt()).as("updatedAt").isNotNull();
      });
    }

    @Test
    @DisplayName("Should return all fields")
    void shouldReturnAllFields() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      Transaction transaction = newTransactionOn(userId, categoryId, LocalDate.of(2026, 10, 12));

      Transaction detail = transactionRepository.findByIdAndUser(transaction.getId(), userId).orElseThrow();
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(detail.getId()).as("id").isEqualTo(transaction.getId());
        softly.assertThat(detail.getCategoryId()).as("catgoryId").isEqualTo(categoryId);
        softly.assertThat(detail.getAmount()).as("amount").isEqualByComparingTo(transaction.getAmount());
        softly.assertThat(detail.getDate()).as("date").isEqualTo(LocalDate.of(2026, 10, 12));
        softly.assertThat(detail.getNotes()).as("notes").isEqualTo(transaction.getNotes());
      });
    }

    @Test
    @DisplayName("Should not return detail of another user")
    void shouldNotReturnDetailOfAnotherUser() {
      Transaction transaction = newTransaction(insertUser(), FlowType.EXPENSE);

      assertThat(transactionRepository.findDetailByIdAndUser(transaction.getId(), insertUser())).isEmpty();
    }

    @Test
    @DisplayName("Should not return deleted transaction")
    void shouldNotReturnDeletedTransaction() {
      UUID userId = insertUser();
      Transaction deleted = deleteTransaction(userId, FlowType.EXPENSE);

      assertThat(transactionRepository.findDetailByIdAndUser(deleted.getId(), userId)).isEmpty();
    }
  }

  private Transaction newTransaction(UUID userId, FlowType flowType) {
    UUID categoryId = insertCategory(userId, "Cat-" + UUID.randomUUID(), flowType);
    return transactionRepository
      .save(Transaction.create(userId, categoryId, new BigDecimal("200.00"), LocalDate.of(2026, 10, 12), ""));
  }

  private Transaction newTransactionOn(UUID userId, UUID categoryId, LocalDate date) {
    return transactionRepository.save(Transaction.create(userId, categoryId, new BigDecimal("200.00"), date, ""));
  }

  private Transaction deleteTransaction(UUID userId, FlowType flowType) {
    Transaction transaction = newTransaction(userId, flowType);
    transaction.delete();
    return transactionRepository.save(transaction);
  }
}
