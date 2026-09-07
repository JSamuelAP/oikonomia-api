package dev.jsamuelap.oikonomiaapi.transaction.application.service;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;
import dev.jsamuelap.oikonomiaapi.transaction.domain.exception.TransactionNotFoundException;
import dev.jsamuelap.oikonomiaapi.transaction.domain.model.Transaction;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.CreateTransactionCommand;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.CreateTransactionUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.GetTransactionUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.ListTransactionsUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.TransactionDetailView;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.TransactionView;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.UpdateTransactionCommand;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.UpdateTransactionUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.CategoryLookupPort;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.CategorySummary;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionDetail;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService
  implements
    ListTransactionsUseCase,
    GetTransactionUseCase,
    CreateTransactionUseCase,
    UpdateTransactionUseCase {
  private final TransactionRepository transactionRepository;
  private final CategoryLookupPort categoryLookupPort;

  @Override
  @Transactional(readOnly = true)
  public List<TransactionView> getAll(UUID userId, YearMonth yearMonth) {
    YearMonth effectiveYearMonth = Objects.requireNonNullElse(yearMonth, YearMonth.now());
    if (effectiveYearMonth.getYear() < 2025 || effectiveYearMonth.getYear() > 2100) {
      throw new DomainException("El año debe ser entre 2025 y 2100");
    }

    List<Transaction> transactions = transactionRepository.findByUser(userId, effectiveYearMonth);

    Set<UUID> categoryIds = transactions.stream().map(Transaction::getCategoryId).collect(Collectors.toSet());
    Map<UUID, CategorySummary> categories = categoryLookupPort.findByIds(categoryIds, userId);

    return transactions.stream().map(
      t -> new TransactionView(t.getId(), t.getAmount(), t.getDate(), t.getNotes(), categories.get(t.getCategoryId())))
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public TransactionDetailView getById(UUID transactionId, UUID userId) {
    TransactionDetail transaction = transactionRepository.findDetailByIdAndUser(transactionId, userId)
      .orElseThrow(() -> new TransactionNotFoundException(transactionId));

    CategorySummary category = categoryLookupPort.findByIds(Set.of(transaction.categoryId()), userId)
      .get(transaction.categoryId());

    return new TransactionDetailView(transaction.id(), transaction.amount(), transaction.date(), transaction.notes(),
      category, transaction.createdAt(), transaction.updatedAt());
  }

  @Override
  @Transactional
  public UUID create(CreateTransactionCommand command) {
    validateCategoryExists(command.userId(), command.categoryId());
    Transaction transaction = Transaction.create(command.userId(), command.categoryId(), command.amount(),
      command.date(), command.notes());
    Transaction saved = transactionRepository.save(transaction);
    return saved.getId();
  }

  @Override
  @Transactional
  public void update(UpdateTransactionCommand command) {
    Transaction transaction = transactionRepository.findByIdAndUser(command.id(), command.userId())
      .orElseThrow(() -> new TransactionNotFoundException(command.id()));

    validateCategoryExists(command.userId(), command.categoryId());

    transaction.changeCategoryId(command.categoryId());
    transaction.changeAmount(command.amount());
    transaction.changeDate(command.date());
    transaction.changeNotes(command.notes());

    transactionRepository.save(transaction);
  }

  private void validateCategoryExists(UUID userId, UUID categoryId) {
    Map<UUID, CategorySummary> categories = categoryLookupPort.findByIds(Set.of(categoryId), userId);
    if (!categories.containsKey(categoryId) || categories.get(categoryId).deleted()) {
      throw new DomainException("Categoría no encontrada");
    }
  }
}
