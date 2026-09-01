package dev.jsamuelap.oikonomiaapi.transaction.domain.port.out;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface CategoryLookupPort {
  Map<UUID, CategorySummary> findByIds(Set<UUID> categoryIds);
}
