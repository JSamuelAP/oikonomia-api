package dev.jsamuelap.oikonomiaapi.category.domain.port.in;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface GetCategoriesByIdsUseCase {
  List<CategoryView> getByIds(Set<UUID> categoryIds);
}
