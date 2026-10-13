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
import dev.jsamuelap.oikonomiaapi.support.PersistenceTestSupport;
import dev.jsamuelap.oikonomiaapi.transaction.domain.model.Transaction;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionDetail;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionRepositoryPort;

public class TransactionRepositoryAdapterTest extends PersistenceTestSupport {
  @Autowired
  private TransactionRepositoryPort transactionRepository;

  @Nested
  class Save {
    @Test
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
    void shouldAcceptSmallestPositiveAmount() {
      UUID userId = insertUser();
      UUID categoryId = insertCategory(userId, "Groceries", FlowType.EXPENSE);
      Transaction transaction = Transaction.reconstitute(UUID.randomUUID(), userId, categoryId, new BigDecimal("0.01"),
        LocalDate.of(2026, 10, 12), "", null);

      assertThatCode(() -> transactionRepository.save(transaction)).doesNotThrowAnyException();
    }
  }

  @Nested
  class Update {
    @Test
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
  class SoftDelete {
    @Test
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
    void shouldPersistDeletionOfAnExistingTransaction() {
      Transaction deleted = deleteTransaction(insertUser(), FlowType.EXPENSE);

      Boolean isDeleted = jdbc.queryForObject("SELECT deleted_at IS NOT NULL FROM transactions WHERE id = ?",
        Boolean.class, deleted.getId());
      assertThat(isDeleted).isTrue();
    }
  }

  @Nested
  class FindByUserAndMonth {
    private static final YearMonth MARCH = YearMonth.of(2025, 3);

    @Test
    void shouldReturnOnlyActiveOfTheUser() {
      UUID userId = insertUser();
      Transaction transaction = newTransaction(userId, FlowType.EXPENSE);
      deleteTransaction(userId, FlowType.INCOME);
      newTransaction(insertUser(), FlowType.EXPENSE);

      assertThat(transactionRepository.findByUser(userId, YearMonth.of(2026, 10))).extracting(Transaction::getId)
        .containsExactly(transaction.getId());
    }

    @Test
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
  class FindByIdAndUser {
    @Test
    void shouldNotFindTransactionOfAnotherUser() {
      Transaction transaction = newTransaction(insertUser(), FlowType.EXPENSE);

      assertThat(transactionRepository.findByIdAndUser(transaction.getId(), insertUser())).isEmpty();
    }
  }

  @Nested
  class FindDetailByIdAndUser {
    @Test
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
    void shouldNotReturnDetailOfAnotherUser() {
      Transaction transaction = newTransaction(insertUser(), FlowType.EXPENSE);

      assertThat(transactionRepository.findDetailByIdAndUser(transaction.getId(), insertUser())).isEmpty();
    }

    @Test
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
