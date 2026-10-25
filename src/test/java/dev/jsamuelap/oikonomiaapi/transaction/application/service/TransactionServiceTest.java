package dev.jsamuelap.oikonomiaapi.transaction.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;
import dev.jsamuelap.oikonomiaapi.shared.domain.validation.PeriodRules;
import dev.jsamuelap.oikonomiaapi.transaction.domain.exception.TransactionNotFoundException;
import dev.jsamuelap.oikonomiaapi.transaction.domain.model.Transaction;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.CreateTransactionCommand;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.MonthlyBalance;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.TransactionDetailView;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.TransactionView;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.UpdateTransactionCommand;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.CategoryLookupPort;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.CategorySummary;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionDetail;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction service")
class TransactionServiceTest {
  @Mock
  private TransactionRepositoryPort transactionRepository;
  @Mock
  private CategoryLookupPort categoryLookupPort;

  @InjectMocks
  private TransactionService transactionService;

  @Nested
  @DisplayName("Get all transactions")
  class GetAll {
    @ParameterizedTest(name = "should reject: {0}")
    @DisplayName("Should reject when year is invalid")
    @ValueSource(shorts = {-431, 0, PeriodRules.MIN_YEAR - 1, PeriodRules.MAX_YEAR + 1})
    void shouldRejectWhenYearIsInvalid(short year) {
      YearMonth yearMonth = YearMonth.of(year, Month.SEPTEMBER);

      assertThatThrownBy(() -> transactionService.getAll(UUID.randomUUID(), yearMonth))
        .isInstanceOf(DomainException.class).hasMessageContaining("El año debe estar entre");

      verify(transactionRepository, never()).findByUser(any(), any());
      verify(categoryLookupPort, never()).findByIds(any(), any());
    }

    @Test
    @DisplayName("Should calculate current year-month when null")
    void shouldCalculateCurrentYearMonthWhenNull() {
      UUID userId = UUID.randomUUID();
      transactionService.getAll(userId, null);

      verify(transactionRepository).findByUser(userId, YearMonth.now());
    }

    @Test
    @DisplayName("Should return transaction view")
    void shouldReturnTransactionView() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      YearMonth yearMonth = YearMonth.now();
      UUID categoryId = UUID.randomUUID();
      BigDecimal amount = BigDecimal.TEN;
      LocalDate now = LocalDate.now();
      Transaction transaction = Transaction.reconstitute(id, userId, categoryId, amount, now, null, null);
      when(transactionRepository.findByUser(userId, yearMonth)).thenReturn(List.of(transaction));

      CategorySummary category = new CategorySummary(categoryId, "Groceries", "EXPENSE", false);
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of(categoryId, category));

      TransactionView expected = new TransactionView(id, amount, now, null, category);

      List<TransactionView> transactions = transactionService.getAll(userId, yearMonth);
      assertThat(transactions).isEqualTo(List.of(expected));

      verify(transactionRepository).findByUser(userId, yearMonth);
      verify(categoryLookupPort).findByIds(Set.of(categoryId), userId);
    }
  }

  @Nested
  @DisplayName("Get transaction by ID")
  class GetById {
    @Test
    @DisplayName("Should reject when transaction does not exist")
    void shouldRejectWhenTransactionDoesNotExist() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      when(transactionRepository.findDetailByIdAndUser(id, userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> transactionService.getById(id, userId)).isInstanceOf(TransactionNotFoundException.class)
        .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Should return transaction detail")
    void shouldReturnTransactionDetail() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      BigDecimal amount = BigDecimal.TEN;
      LocalDate now = LocalDate.now();
      Instant instant = Instant.now();
      TransactionDetail transaction = new TransactionDetail(id, categoryId, amount, now, null, instant, instant);
      when(transactionRepository.findDetailByIdAndUser(id, userId)).thenReturn(Optional.of(transaction));

      CategorySummary category = new CategorySummary(categoryId, "Groceries", "EXPENSE", false);
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of(categoryId, category));

      TransactionDetailView expected = new TransactionDetailView(id, amount, now, null, category, instant, instant);

      TransactionDetailView response = transactionService.getById(id, userId);
      assertThat(response).isEqualTo(expected);
    }
  }

  @Nested
  @DisplayName("Create transaction")
  class Create {
    @Test
    @DisplayName("Should reject when category does not exist")
    void shouldRejectWhenCategoryDoesNotExist() {
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of());

      var command = new CreateTransactionCommand(userId, categoryId, BigDecimal.TEN, LocalDate.now(), null);
      assertThatThrownBy(() -> transactionService.create(command)).isInstanceOf(DomainException.class)
        .hasMessageContaining("Categoría no encontrada");

      verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should persist transaction")
    void shouldPersistTransaction() {
      UUID categoryId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      CategorySummary category = new CategorySummary(categoryId, "Groceries", "EXPENSE", false);
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of(categoryId, category));

      BigDecimal amount = BigDecimal.TEN;
      LocalDate now = LocalDate.now();
      Transaction saved = Transaction.create(userId, categoryId, amount, now, null);
      when(transactionRepository.save(any())).thenReturn(saved);

      var command = new CreateTransactionCommand(userId, categoryId, amount, now, null);
      UUID responseId = transactionService.create(command);

      assertThat(responseId).isNotNull();

      verify(transactionRepository).save(argThat(transaction -> transaction.getUserId().equals(userId)));
    }
  }

  @Nested
  @DisplayName("Update transaction")
  class Update {
    @Test
    @DisplayName("Should reject when transaction does not exist")
    void shouldRejectWhenTransactionDoesNotExist() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      when(transactionRepository.findByIdAndUser(id, userId)).thenReturn(Optional.empty());

      var command = new UpdateTransactionCommand(id, userId, UUID.randomUUID(), BigDecimal.TEN, LocalDate.now(), null);
      assertThatThrownBy(() -> transactionService.update(command)).isInstanceOf(TransactionNotFoundException.class)
        .hasMessageContaining(id.toString());

      verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject when category does not exist")
    void shouldRejectWhenCategoryDoesNotExist() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      BigDecimal amount = BigDecimal.TEN;
      LocalDate now = LocalDate.now();
      Transaction transaction = Transaction.reconstitute(id, userId, categoryId, amount, now, null, null);
      when(transactionRepository.findByIdAndUser(id, userId)).thenReturn(Optional.of(transaction));

      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of());

      var command = new UpdateTransactionCommand(id, userId, categoryId, amount, now, null);
      assertThatThrownBy(() -> transactionService.update(command)).isInstanceOf(DomainException.class)
        .hasMessageContaining("Categoría no encontrada");

      verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update")
    void shouldUpdate() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      BigDecimal amount = BigDecimal.TEN;
      LocalDate now = LocalDate.now();
      Transaction transaction = Transaction.reconstitute(id, userId, UUID.randomUUID(), amount, now, null, null);
      when(transactionRepository.findByIdAndUser(id, userId)).thenReturn(Optional.of(transaction));

      UUID updatedCategoryId = UUID.randomUUID();
      CategorySummary category = new CategorySummary(updatedCategoryId, "Salary", "INCOME", false);
      when(categoryLookupPort.findByIds(Set.of(updatedCategoryId), userId))
        .thenReturn(Map.of(updatedCategoryId, category));

      BigDecimal updatedAmount = BigDecimal.TWO;
      LocalDate updatedDate = LocalDate.now().minusDays(1);
      String updatedNotes = "Update transaction";
      var command = new UpdateTransactionCommand(id, userId, updatedCategoryId, updatedAmount, updatedDate,
        updatedNotes);
      transactionService.update(command);

      ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
      verify(transactionRepository).save(captor.capture());

      Transaction expected = Transaction.reconstitute(id, userId, updatedCategoryId, updatedAmount, updatedDate,
        updatedNotes, null);
      assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(expected);
    }
  }

  @Nested
  @DisplayName("Delete transaction")
  class Delete {
    @Test
    @DisplayName("Should reject when transaction does not exist")
    void shouldRejectWhenTransactionDoesNotExist() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      when(transactionRepository.findByIdAndUser(id, userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> transactionService.deleteById(id, userId))
        .isInstanceOf(TransactionNotFoundException.class).hasMessageContaining(id.toString());

      verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not delete when transaction is already deleted")
    void shouldNotDeleteWhenTransactionIsAlreadyDeleted() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      Transaction deletedTransaction = Transaction.reconstitute(id, userId, UUID.randomUUID(), BigDecimal.TEN,
        LocalDate.now(), null, Instant.now());
      when(transactionRepository.findByIdAndUser(id, userId)).thenReturn(Optional.of(deletedTransaction));

      transactionService.deleteById(id, userId);

      verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete transaction")
    void shouldDelete() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      Transaction existingTransaction = Transaction.reconstitute(id, userId, UUID.randomUUID(), BigDecimal.TEN,
        LocalDate.now(), null, null);
      when(transactionRepository.findByIdAndUser(id, userId)).thenReturn(Optional.of(existingTransaction));

      transactionService.deleteById(id, userId);

      verify(transactionRepository)
        .save(argThat(transaction -> transaction.getId().equals(id) && transaction.isDeleted()));
    }
  }

  @Nested
  @DisplayName("Get monthly balance")
  class GetMonthlyBalance {
    private final UUID userId = UUID.randomUUID();
    private final YearMonth month = YearMonth.of(PeriodRules.MIN_YEAR, 1);

    @Test
    @DisplayName("Should reject month when year is not supported")
    void shouldRejectMonthWhenYearIsNotSupported() {
      var invalidMonth = YearMonth.of(PeriodRules.MIN_YEAR - 1, 1);
      assertThatThrownBy(() -> transactionService.getMonthlyBalance(userId, invalidMonth))
        .isInstanceOf(DomainException.class);
      verify(transactionRepository, never()).sumAmountsByCategory(any(), any());
    }

    @Test
    @DisplayName("Should return zero balance when month has no transactions")
    void shouldReturnZeroBalanceWhenMonthHasNotTransactions() {
      when(transactionRepository.sumAmountsByCategory(userId, month)).thenReturn(Map.of());

      var result = transactionService.getMonthlyBalance(userId, month);
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(result.month()).isEqualTo(month);
        softly.assertThat(result.totalIncome()).isEqualTo(MonthlyBalance.ZERO_AMOUNT);
        softly.assertThat(result.totalExpense()).isEqualTo(MonthlyBalance.ZERO_AMOUNT);
        softly.assertThat(result.balance()).isEqualTo(MonthlyBalance.ZERO_AMOUNT);
      });
      verify(categoryLookupPort, never()).findByIds(any(), any());
    }

    @Test
    @DisplayName("Should sum income and expense by flow type")
    void shouldSumIncomeAndExpenseByFlowType() {
      var incomeCat1 = UUID.randomUUID();
      var incomeCat2 = UUID.randomUUID();
      var expenseCat1 = UUID.randomUUID();
      var expenseCat2 = UUID.randomUUID();

      when(transactionRepository.sumAmountsByCategory(userId, month))
        .thenReturn(Map.of(incomeCat1, new BigDecimal("1000.00"), incomeCat2, new BigDecimal("500.00"), expenseCat1,
          new BigDecimal("300.00"), expenseCat2, new BigDecimal("200.00")));

      when(categoryLookupPort.findByIds(Set.of(incomeCat1, incomeCat2, expenseCat1, expenseCat2), userId)).thenReturn(
        Map.of(incomeCat1, category(incomeCat1, "INCOME", false), incomeCat2, category(incomeCat2, "INCOME", false),
          expenseCat1, category(expenseCat1, "EXPENSE", false), expenseCat2, category(expenseCat2, "EXPENSE", false)));

      var result = transactionService.getMonthlyBalance(userId, month);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(result.month()).isEqualTo(month);
        softly.assertThat(result.totalIncome()).isEqualTo(new BigDecimal("1500.00"));
        softly.assertThat(result.totalExpense()).isEqualTo(new BigDecimal("500.00"));
        softly.assertThat(result.balance()).isEqualTo(new BigDecimal("1000.00"));
      });
    }

    @Test
    @DisplayName("Should keep two decimals when month has only expenses")
    void shouldKeepTwoDecimalsWhenMonthHasOnlyExpenses() {
      UUID categoryId = UUID.randomUUID();
      when(transactionRepository.sumAmountsByCategory(userId, month))
        .thenReturn(Map.of(categoryId, new BigDecimal("300.50")));
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId))
        .thenReturn(Map.of(categoryId, category(categoryId, "EXPENSE", false)));

      var result = transactionService.getMonthlyBalance(userId, month);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(result.totalIncome()).as("income").isEqualTo(new BigDecimal("0.00"));
        softly.assertThat(result.balance()).as("balance").isEqualTo(new BigDecimal("-300.50"));
      });
    }

    @Test
    @DisplayName("Should include transactions of deleted categories")
    void shouldIncludeTransactionsOfDeletedCategories() {
      UUID categoryId = UUID.randomUUID();
      when(transactionRepository.sumAmountsByCategory(userId, month))
        .thenReturn(Map.of(categoryId, new BigDecimal("200.00")));
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId))
        .thenReturn(Map.of(categoryId, category(categoryId, "INCOME", true)));

      assertThat(transactionService.getMonthlyBalance(userId, month).totalIncome()).isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Should fail when category is missing from lookup")
    void shouldFailWhenCategoryIsMissingFromLookup() {
      UUID categoryId = UUID.randomUUID();
      when(transactionRepository.sumAmountsByCategory(userId, month))
        .thenReturn(Map.of(categoryId, new BigDecimal("100.00")));
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId)).thenReturn(Map.of());

      assertThatThrownBy(() -> transactionService.getMonthlyBalance(userId, month))
        .isInstanceOf(IllegalStateException.class).hasMessageContaining(categoryId.toString());
    }

    @ParameterizedTest
    @DisplayName("Should fail when flow type is unknown")
    @NullSource
    @ValueSource(strings = {"TRANSFER", "income", ""})
    void shouldFailWhenFlowTypeIsUnknown(String flowType) {
      UUID categoryId = UUID.randomUUID();
      when(transactionRepository.sumAmountsByCategory(userId, month))
        .thenReturn(Map.of(categoryId, new BigDecimal("100.00")));
      when(categoryLookupPort.findByIds(Set.of(categoryId), userId))
        .thenReturn(Map.of(categoryId, category(categoryId, flowType, false)));

      assertThatThrownBy(() -> transactionService.getMonthlyBalance(userId, month))
        .isInstanceOf(IllegalStateException.class).hasMessageContaining("Tipo de flujo desconocido");
    }

    private static CategorySummary category(UUID id, String flowType, boolean deleted) {
      return new CategorySummary(id, "Any", flowType, deleted);
    }
  }
}
