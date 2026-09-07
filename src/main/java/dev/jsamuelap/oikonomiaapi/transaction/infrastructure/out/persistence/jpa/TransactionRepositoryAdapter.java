package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.out.persistence.jpa;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.jsamuelap.oikonomiaapi.transaction.domain.model.Transaction;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionDetail;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {
  private final TransactionJpaRepository jpaRepository;
  private final TransactionPersistenceMapper mapper;

  @Override
  public List<Transaction> findByUser(UUID userId, YearMonth yearMonth) {
    Short month = (short) yearMonth.getMonthValue();
    Short year = (short) yearMonth.getYear();
    return jpaRepository.findByUserIdAndMonthAndYear(userId, month, year).stream().map(mapper::toDomain).toList();
  }

  @Override
  public Optional<Transaction> findByIdAndUser(UUID id, UUID userId) {
    return jpaRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).map(mapper::toDomain);
  }

  @Override
  public Optional<TransactionDetail> findDetailByIdAndUser(UUID transactionId, UUID userId) {
    return jpaRepository.findByIdAndUserIdAndDeletedAtIsNull(transactionId, userId).map(mapper::toDetail);
  }

  @Override
  public Transaction save(Transaction transaction) {
    TransactionJpaEntity entity = jpaRepository.findById(transaction.getId())
      .map(existing -> updateEntity(existing, transaction)).orElseGet(() -> mapper.toEntity(transaction));
    TransactionJpaEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  private TransactionJpaEntity updateEntity(TransactionJpaEntity entity, Transaction transaction) {
    entity.setCategoryId(transaction.getCategoryId());
    entity.setAmount(transaction.getAmount());
    entity.setTransactionDate(transaction.getDate());
    entity.setNotes(transaction.getNotes());
    entity.setDeletedAt(transaction.getDeletedAt());
    return entity;
  }
}
