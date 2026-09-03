package dev.jsamuelap.oikonomiaapi.user.domain.port.out;

public interface TokenHasherPort {
  String hash(String rawToken);
}
