package dev.jsamuelap.oikonomiaapi.category.domain.port.in;

import java.util.UUID;

public interface GetCategoryUseCase {
  CategoryDetail getById(UUID categoryId, UUID userId);
}
