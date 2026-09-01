package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

import java.util.List;
import java.util.UUID;

public interface ListTransactionsUseCase {
  List<TransactionView> getAll(UUID userId);
}
