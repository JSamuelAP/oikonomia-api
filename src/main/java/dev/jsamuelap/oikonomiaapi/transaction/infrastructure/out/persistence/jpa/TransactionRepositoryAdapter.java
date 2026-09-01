package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.jsamuelap.oikonomiaapi.transaction.domain.model.Transaction;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {
  private final TransactionJpaRepository jpaRepository;
  private final TransactionPersistenceMapper mapper;

  @Override
  public List<Transaction> findByUser(UUID userId) {
    return jpaRepository.findByUserIdAndDeletedAtIsNull(userId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public Optional<Transaction> findByIdAndUser(UUID transactionId, UUID userId) {
    return jpaRepository.findByIdAndUserIdAndDeletedAtIsNull(transactionId, userId).map(mapper::toDomain);
  }

  @Override
  public Transaction save(Transaction transaction) {
    TransactionJpaEntity entity = mapper.toEntity(transaction);
    TransactionJpaEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }
}
