package dev.jsamuelap.oikonomiaapi.category.domain.port.in;

import java.util.List;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.category.domain.model.Category;

public interface ListCategoriesUseCase {
  List<Category> getAll(UUID userId);
}
