package dev.jsamuelap.oikonomiaapi.transaction.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.transaction.domain.model.Transaction;

public interface TransactionRepository {
  List<Transaction> findByUser(UUID userId);

  Optional<Transaction> findByIdAndUser(UUID transactionId, UUID userId);

  Transaction save(Transaction transaction);
}
