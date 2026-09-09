package dev.jsamuelap.oikonomiaapi;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTest {
  private final ApplicationModules modules = ApplicationModules.of(OikonomiaApiApplication.class);

  @Test
  void verifyModularStructure() {
    modules.verify();
  }
}
