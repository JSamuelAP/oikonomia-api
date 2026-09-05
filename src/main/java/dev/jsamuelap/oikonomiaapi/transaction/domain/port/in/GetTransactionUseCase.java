package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

import java.util.UUID;

public interface GetTransactionUseCase {
  TransactionDetailView getById(UUID transactionId, UUID userId);
}
