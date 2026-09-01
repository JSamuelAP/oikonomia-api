package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

import java.util.UUID;

public interface CreateTransactionUseCase {
  UUID create(CreateTransactionCommand command);
}
