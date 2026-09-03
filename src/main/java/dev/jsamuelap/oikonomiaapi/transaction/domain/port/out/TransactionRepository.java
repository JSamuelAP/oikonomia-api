package dev.jsamuelap.oikonomiaapi.transaction.domain.port.out;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.transaction.domain.model.Transaction;

public interface TransactionRepository {
  List<Transaction> findByUser(UUID userId, YearMonth yearMonth);

  Optional<Transaction> findByIdAndUser(UUID transactionId, UUID userId);

  Transaction save(Transaction transaction);
}
