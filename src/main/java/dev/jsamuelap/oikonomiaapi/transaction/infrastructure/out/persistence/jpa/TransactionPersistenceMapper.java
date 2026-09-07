package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.out.persistence.jpa;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import dev.jsamuelap.oikonomiaapi.transaction.domain.model.Transaction;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.TransactionDetail;

@Mapper(componentModel = "spring")
public interface TransactionPersistenceMapper {
  default Transaction toDomain(TransactionJpaEntity entity) {
    return Transaction.reconstitute(entity.getId(), entity.getUserId(), entity.getCategoryId(), entity.getAmount(),
      entity.getTransactionDate(), entity.getNotes(), entity.getDeletedAt());
  }

  default TransactionJpaEntity toEntity(Transaction domain) {
    TransactionJpaEntity entity = new TransactionJpaEntity();
    entity.setId(domain.getId());
    entity.setUserId(domain.getUserId());
    entity.setCategoryId(domain.getCategoryId());
    entity.setAmount(domain.getAmount());
    entity.setTransactionDate(domain.getDate());
    entity.setNotes(domain.getNotes());
    entity.setDeletedAt(domain.getDeletedAt());
    return entity;
  }

  @Mapping(target = "date", source = "transactionDate")
  TransactionDetail toDetail(TransactionJpaEntity entity);
}
