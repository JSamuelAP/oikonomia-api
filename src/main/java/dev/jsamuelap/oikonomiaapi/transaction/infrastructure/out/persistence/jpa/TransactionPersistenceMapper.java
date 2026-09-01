package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.out.persistence.jpa;

import org.mapstruct.Mapper;

import dev.jsamuelap.oikonomiaapi.transaction.domain.model.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionPersistenceMapper {
  default Transaction toDomain(TransactionJpaEntity entity) {
    return Transaction.reconstitute(entity.getId(), entity.getUserId(), entity.getCategoryId(), entity.getAmount(),
      entity.getTransactionDate(), entity.getNotes());
  }

  default TransactionJpaEntity toEntity(Transaction domain) {
    TransactionJpaEntity entity = new TransactionJpaEntity();
    entity.setId(domain.getId());
    entity.setUserId(domain.getUserId());
    entity.setCategoryId(domain.getCategoryId());
    entity.setAmount(domain.getAmount());
    entity.setTransactionDate(domain.getDate());
    entity.setNotes(domain.getNotes());
    return entity;
  }
}
