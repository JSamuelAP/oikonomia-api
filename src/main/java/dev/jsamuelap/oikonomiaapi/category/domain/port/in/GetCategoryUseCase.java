package dev.jsamuelap.oikonomiaapi.category.domain.port.in;

import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.category.domain.model.Category;

public interface GetCategoryUseCase {
  Category getById(UUID categoryId, UUID userId);
}
