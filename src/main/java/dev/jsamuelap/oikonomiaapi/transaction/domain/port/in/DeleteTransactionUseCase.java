package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

import java.util.UUID;

public interface DeleteTransactionUseCase {
  void deleteById(UUID id, UUID userId);
}
