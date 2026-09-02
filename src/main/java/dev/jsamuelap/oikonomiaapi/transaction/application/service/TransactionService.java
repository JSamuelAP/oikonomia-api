package dev.jsamuelap.oikonomiaapi.transaction.application.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jsamuelap.oikonomiaapi.transaction.domain.exception.TransactionNotFoundException;
import dev.jsamuelap.oikonomiaapi.transaction.domain.model.Transaction;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.CreateTransactionCommand;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.CreateTransactionUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.GetTransactionUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.ListTransactionsUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.TransactionView;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.CategoryLookupPort;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.CategorySummary;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService implements ListTransactionsUseCase, GetTransactionUseCase, CreateTransactionUseCase {
  private final TransactionRepository transactionRepository;
  private final CategoryLookupPort categoryLookupPort;

  @Override
  @Transactional(readOnly = true)
  public List<TransactionView> getAll(UUID userId) {
    List<Transaction> transactions = transactionRepository.findByUser(userId);

    Set<UUID> categoryIds = transactions.stream().map(Transaction::getCategoryId).collect(Collectors.toSet());
    Map<UUID, CategorySummary> categories = categoryLookupPort.findByIds(categoryIds);

    return transactions.stream().map(
      t -> new TransactionView(t.getId(), t.getAmount(), t.getDate(), t.getNotes(), categories.get(t.getCategoryId())))
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public TransactionView getById(UUID transactionId, UUID userId) {
    Transaction transaction = transactionRepository.findByIdAndUser(transactionId, userId)
      .orElseThrow(() -> new TransactionNotFoundException(transactionId));

    CategorySummary category = categoryLookupPort.findByIds(Set.of(transaction.getCategoryId()))
      .get(transaction.getCategoryId());

    return new TransactionView(transaction.getId(), transaction.getAmount(), transaction.getDate(),
      transaction.getNotes(), category);
  }

  @Override
  @Transactional
  public UUID create(CreateTransactionCommand command) {
    Transaction transaction = Transaction.create(command.userId(), command.categoryId(), command.amount(),
      command.date(), command.notes());
    Transaction saved = transactionRepository.save(transaction);
    return saved.getId();
  }
}
