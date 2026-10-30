package dev.jsamuelap.oikonomiaapi.budget.infrastructure.in.rest;

import java.net.URI;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.CreateMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.DeleteMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.GetMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.ListMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.UpdateMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.shared.security.jwt.AuthenticatedPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/monthly-budgets")
@RequiredArgsConstructor
@Validated
@Tag(name = "Monthly Budgets", description = "Monthly budget management endpoints")
public class MonthlyBudgetController {
  private final ListMonthlyBudgetUseCase listMonthlyBudgetUseCase;
  private final GetMonthlyBudgetUseCase getMonthlyBudgetUseCase;
  private final CreateMonthlyBudgetUseCase createMonthlyBudgetUseCase;
  private final UpdateMonthlyBudgetUseCase updateMonthlyBudgetUseCase;
  private final DeleteMonthlyBudgetUseCase deleteMonthlyBudgetUseCase;
  private final MonthlyBudgetRestMapper mapper;

  // spotless:off
  @GetMapping()
  @Operation(
    summary = "Get all monthly budgets",
    description = "Retrieves a list of monthly budgets for the authenticated user, optionally filtered by year-month"
  )
  @ApiResponse(responseCode = "200", description = "List of monthly budgets retrieved successfully")
  public ResponseEntity<List<MonthlyBudgetResponse>> getAll(
    @Parameter(hidden = true)
    @AuthenticationPrincipal
    final AuthenticatedPrincipal principal,
    // TODO: estandarizar a month y hacerlo obligatorio, igual en transactions
    @Parameter(description = "Filter by year-month (format: yyyy-MM)", example = "2026-01")
    @RequestParam(required = false)
    @DateTimeFormat(pattern = "yyyy-MM")
    final YearMonth yearMonth
  ) {
    // spotless:on
    List<MonthlyBudgetResponse> budgets = mapper
      .toResponse(listMonthlyBudgetUseCase.getAll(principal.userId(), yearMonth));
    return ResponseEntity.ok(budgets);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get monthly budget by ID", description = "Retrieves detailed information for a specific monthly budget")
  @ApiResponse(responseCode = "200", description = "Monthly budget retrieved successfully")
  public ResponseEntity<MonthlyBudgetDetailResponse> get(
    @Parameter(hidden = true) @AuthenticationPrincipal final AuthenticatedPrincipal principal,
    @Parameter(description = "Monthly budget ID", required = true) @PathVariable final UUID id) {
    MonthlyBudgetDetailResponse budget = mapper.toResponse(getMonthlyBudgetUseCase.getById(id, principal.userId()));
    return ResponseEntity.ok(budget);
  }

  @PostMapping()
  @Operation(summary = "Create a new monthly budget", description = "Creates a new monthly budget for the authenticated user")
  @ApiResponse(responseCode = "201", description = "Monthly budget created successfully")
  @ApiResponse(responseCode = "409", description = "Monthly budget for this category and period already exists")
  public ResponseEntity<Void> create(
    @Parameter(description = "Monthly budget creation request", required = true) @Valid @RequestBody final CreateMonthlyBudgetRequest request,
    @Parameter(hidden = true) @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    UUID monthlyBudgetId = createMonthlyBudgetUseCase.create(mapper.toCommand(request, principal.userId()));
    return ResponseEntity.created(URI.create("/api/v1/monthly-budgets/" + monthlyBudgetId)).build();
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a monthly budget", description = "Updates an existing monthly budget for the authenticated user")
  @ApiResponse(responseCode = "204", description = "Monthly budget updated successfully")
  @ApiResponse(responseCode = "409", description = "Monthly budget for this category and period already exists")
  public ResponseEntity<Void> update(
    @Parameter(description = "Monthly budget ID", required = true) @PathVariable final UUID id,
    @Parameter(description = "Monthly budget update request", required = true) @Valid @RequestBody final UpdateMonthlyBudgetRequest request,
    @Parameter(hidden = true) @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    updateMonthlyBudgetUseCase.update(mapper.toCommand(request, id, principal.userId()));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a monthly budget", description = "Deletes a monthly budget by ID for the authenticated user")
  @ApiResponse(responseCode = "204", description = "Monthly budget deleted successfully")
  public ResponseEntity<Void> delete(
    @Parameter(description = "Monthly budget ID", required = true) @PathVariable final UUID id,
    @Parameter(hidden = true) @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    deleteMonthlyBudgetUseCase.deleteById(id, principal.userId());
    return ResponseEntity.noContent().build();
  }
}
