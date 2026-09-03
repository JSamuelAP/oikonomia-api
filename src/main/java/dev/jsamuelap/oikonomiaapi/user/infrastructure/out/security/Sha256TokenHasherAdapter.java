package dev.jsamuelap.oikonomiaapi.user.infrastructure.out.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

import dev.jsamuelap.oikonomiaapi.user.domain.port.out.TokenHasherPort;

@Component
public class Sha256TokenHasherAdapter implements TokenHasherPort {
  @Override
  public String hash(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashBytes);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 no disponible en este entorno", ex);
    }
  }
}
