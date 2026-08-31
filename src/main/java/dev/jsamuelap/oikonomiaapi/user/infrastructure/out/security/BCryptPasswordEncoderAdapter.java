package dev.jsamuelap.oikonomiaapi.user.infrastructure.out.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import dev.jsamuelap.oikonomiaapi.user.domain.port.out.PasswordEncoderPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {
  private final PasswordEncoder passwordEncoder;

  @Override
  public String encode(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
  }

  @Override
  public boolean matches(String rawPassword, String encodedPassword) {
    return passwordEncoder.matches(rawPassword, encodedPassword);
  }
}
