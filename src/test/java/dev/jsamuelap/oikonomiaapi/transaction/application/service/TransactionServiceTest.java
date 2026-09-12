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

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;
import dev.jsamuelap.oikonomiaapi.transaction.domain.exception.TransactionNotFoundException;
import dev.jsamuelap.oikonomiaapi.transaction.domain.model.Transaction;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.CreateTransactionCommand;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.TransactionDetailView;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.TransactionView;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.UpdateTransactionCommand;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.CategoryLookupPort;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.CategorySummary;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionDetail;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction service")
class TransactionServiceTest {
  @Mock
  private TransactionRepository transactionRepository;
  @Mock
  private CategoryLookupPort categoryLookupPort;

  @InjectMocks
  private TransactionService transactionService;

  @Nested
  @DisplayName("Get all transactions")
  class GetAll {
    @ParameterizedTest(name = "should reject: {0}")
    @DisplayName("Should reject when year is invalid")
    @ValueSource(shorts = {-431, 0, 2024, 2101})
    void shouldRejectWhenYearIsInvalid(short year) {
      YearMonth yearMonth = YearMonth.of(year, Month.SEPTEMBER);

      assertThatThrownBy(() -> transactionService.getAll(UUID.randomUUID(), yearMonth))
        .isInstanceOf(DomainException.class).hasMessageContaining("El año debe ser entre");

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
}
