package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.out.persistence.jpa;

import java.math.BigDecimal;
import java.util.UUID;

public interface CategoryTotal {
  UUID getCategoryId();

  BigDecimal getTotal();
}
