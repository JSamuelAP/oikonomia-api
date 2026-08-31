package dev.jsamuelap.oikonomiaapi.category.domain.port.in;

import java.util.UUID;

public interface CreateCategoryUseCase {
  UUID createCategory(CreateCategoryCommand command);
}
