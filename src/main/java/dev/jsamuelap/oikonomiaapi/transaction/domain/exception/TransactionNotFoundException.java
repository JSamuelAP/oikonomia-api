package dev.jsamuelap.oikonomiaapi.transaction.domain.exception;

import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.NotFoundException;

public class TransactionNotFoundException extends NotFoundException {
  private static final long serialVersionUID = 1L;

  public TransactionNotFoundException(UUID id) {
    super("No se encontró la transacción con id " + id);
  }
}
