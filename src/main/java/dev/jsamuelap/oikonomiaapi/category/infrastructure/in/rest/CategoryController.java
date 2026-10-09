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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {
  private final ListCategoriesUseCase listCategoriesUseCase;
  private final CreateCategoryUseCase createCategoryUseCase;
  private final GetCategoryUseCase getCategoryUseCase;
  private final DeleteCategoryUseCase deleteCategoryUseCase;
  private final CategoryRestMapper mapper;

  @GetMapping()
  @Operation(summary = "Get all categories", description = "Retrieves a list of all categories for the authenticated user")
  @ApiResponse(responseCode = "200", description = "List of categories retrieved successfully")
  public ResponseEntity<List<CategoryResponse>> getAll(
    @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal) {
    List<CategoryResponse> categories = mapper.toResponse(listCategoriesUseCase.getAll(principal.userId()));
    return ResponseEntity.ok(categories);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get category by ID", description = "Retrieves detailed information for a specific category")
  @ApiResponse(responseCode = "200", description = "Category retrieved successfully")
  public ResponseEntity<CategoryDetailResponse> getById(
    @Parameter(description = "Category ID", required = true) @PathVariable UUID id,
    @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal) {
    CategoryDetailResponse category = mapper.toDetail(getCategoryUseCase.getById(id, principal.userId()));
    return ResponseEntity.ok(category);
  }

  @PostMapping()
  @Operation(summary = "Create a new category", description = "Creates a new category for the authenticated user")
  @ApiResponse(responseCode = "201", description = "Category created successfully")
  @ApiResponse(responseCode = "409", description = "Category with this name and flow type already exists for the user")
  public ResponseEntity<Void> create(
    @Parameter(description = "Category creation request", required = true) @Valid @RequestBody CreateCategoryRequest request,
    @Parameter(hidden = true) @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    UUID categoryId = createCategoryUseCase.createCategory(mapper.toCommand(request, principal.userId()));
    return ResponseEntity.created(URI.create("/api/v1/categories/" + categoryId)).build();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a category", description = "Deletes a category by ID for the authenticated user")
  @ApiResponse(responseCode = "204", description = "Category deleted successfully")
  public ResponseEntity<Void> delete(@Parameter(description = "Category ID", required = true) @PathVariable UUID id,
    @Parameter(hidden = true) @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    deleteCategoryUseCase.deleteById(id, principal.userId());
    return ResponseEntity.noContent().build();
  }
}
