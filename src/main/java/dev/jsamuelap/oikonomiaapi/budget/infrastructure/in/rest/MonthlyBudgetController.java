package dev.jsamuelap.oikonomiaapi.budget.infrastructure.in.rest;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.CreateMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.GetMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.ListMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.shared.security.jwt.AuthenticatedPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/monthly-budgets")
@RequiredArgsConstructor
@Validated
public class MonthlyBudgetController {
  private final ListMonthlyBudgetUseCase listMonthlyBudgetUseCase;
  private final GetMonthlyBudgetUseCase getMonthlyBudgetUseCase;
  private final CreateMonthlyBudgetUseCase createMonthlyBudgetUseCase;
  private final MonthlyBudgetRestMapper mapper;

  @GetMapping()
  public ResponseEntity<List<MonthlyBudgetResponse>> getAll(
    @AuthenticationPrincipal final AuthenticatedPrincipal principal, @RequestParam(required = false) final Short year) {
    List<MonthlyBudgetResponse> budgets = mapper.toResponse(listMonthlyBudgetUseCase.getAll(principal.userId(), year));
    return ResponseEntity.ok(budgets);
  }

  @GetMapping("/{id}")
  public ResponseEntity<MonthlyBudgetResponse> get(@PathVariable final UUID id,
    @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    MonthlyBudgetResponse budget = mapper.toResponse(getMonthlyBudgetUseCase.getById(id, principal.userId()));
    return ResponseEntity.ok(budget);
  }

  @PostMapping()
  public ResponseEntity<Void> create(@Valid @RequestBody final CreateMonthlyBudgetRequest request,
    @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    UUID monthlyBudgetId = createMonthlyBudgetUseCase.create(mapper.toCommand(request, principal.userId()));
    return ResponseEntity.created(URI.create("/api/v1/monthly-budgets/" + monthlyBudgetId)).build();
  }
}
