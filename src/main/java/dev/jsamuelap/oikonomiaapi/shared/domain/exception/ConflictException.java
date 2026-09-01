package dev.jsamuelap.oikonomiaapi.shared.domain.exception;

public class ConflictException extends DomainException {
  private static final long serialVersionUID = 1L;

  public ConflictException(String message) {
    super(message);
  }
}
