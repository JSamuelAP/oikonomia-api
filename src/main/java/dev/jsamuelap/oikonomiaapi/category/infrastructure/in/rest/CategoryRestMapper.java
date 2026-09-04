package dev.jsamuelap.oikonomiaapi.category.infrastructure.in.rest;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;

import dev.jsamuelap.oikonomiaapi.category.domain.model.Category;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CategoryDetail;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CreateCategoryCommand;

@Mapper(componentModel = "spring")
public interface CategoryRestMapper {
  CategoryResponse toResponse(Category category);

  CategoryDetailResponse toDetail(CategoryDetail category);

  List<CategoryResponse> toResponse(List<Category> categories);

  CreateCategoryCommand toCommand(CreateCategoryRequest request, UUID userId);
}
