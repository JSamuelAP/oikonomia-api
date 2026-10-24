package dev.jsamuelap.oikonomiaapi.transaction.domain.port.out;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.transaction.domain.model.Transaction;

public interface TransactionRepositoryPort {
  List<Transaction> findByUser(UUID userId, YearMonth yearMonth);

  Optional<Transaction> findByIdAndUser(UUID id, UUID userId);

  Optional<TransactionDetail> findDetailByIdAndUser(UUID transactionId, UUID userId);

  Transaction save(Transaction transaction);

  Map<UUID, BigDecimal> sumAmountsByCategory(UUID userId, YearMonth month);
}
