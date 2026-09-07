package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

public interface UpdateTransactionUseCase {
  void update(UpdateTransactionCommand command);
}
