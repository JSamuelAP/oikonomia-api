package dev.jsamuelap.oikonomiaapi.category.infrastructure.in.rest;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CreateCategoryUseCase;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.DeleteCategoryUseCase;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.GetCategoryUseCase;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.ListCategoriesUseCase;
import dev.jsamuelap.oikonomiaapi.shared.security.jwt.AuthenticatedPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {
  private final ListCategoriesUseCase listCategoriesUseCase;
  private final CreateCategoryUseCase createCategoryUseCase;
  private final GetCategoryUseCase getCategoryUseCase;
  private final DeleteCategoryUseCase deleteCategoryUseCase;
  private final CategoryRestMapper mapper;

  @GetMapping()
  public ResponseEntity<List<CategoryResponse>> getAll(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
    List<CategoryResponse> categories = mapper.toResponse(listCategoriesUseCase.getAll(principal.userId()));
    return ResponseEntity.ok(categories);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoryDetailResponse> getById(@PathVariable UUID id,
    @AuthenticationPrincipal AuthenticatedPrincipal principal) {
    CategoryDetailResponse category = mapper.toDetail(getCategoryUseCase.getById(id, principal.userId()));
    return ResponseEntity.ok(category);
  }

  @PostMapping()
  public ResponseEntity<Void> create(@Valid @RequestBody CreateCategoryRequest request,
    @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    UUID categoryId = createCategoryUseCase.createCategory(mapper.toCommand(request, principal.userId()));
    return ResponseEntity.created(URI.create("/api/v1/categories/" + categoryId)).build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id,
    @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    deleteCategoryUseCase.deleteById(id, principal.userId());
    return ResponseEntity.noContent().build();
  }
}
