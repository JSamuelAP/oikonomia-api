package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.in.rest;

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

import dev.jsamuelap.oikonomiaapi.shared.security.jwt.AuthenticatedPrincipal;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.CreateTransactionUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.DeleteTransactionUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.GetMonthlyBalanceUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.GetTransactionUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.ListTransactionsUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.MonthlyBalance;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.UpdateTransactionUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionController {
  private final ListTransactionsUseCase listTransactionsUseCase;
  private final GetTransactionUseCase getTransactionUseCase;
  private final CreateTransactionUseCase createTransactionUseCase;
  private final UpdateTransactionUseCase updateTransactionUseCase;
  private final DeleteTransactionUseCase deleteTransactionUseCase;
  private final GetMonthlyBalanceUseCase getMonthlyBalanceUseCase;
  private final TransactionRestMapper mapper;

  // spotless:off
  @GetMapping()
  @Operation(
    summary = "Get all transactions",
    description = "Retrieves a list of transactions for the authenticated user, optionally filtered by year-month"
  )
  @ApiResponse(responseCode = "200", description = "List of transactions retrieved successfully")
  public ResponseEntity<List<TransactionResponse>> getAll(
    @Parameter(hidden = true)
    @AuthenticationPrincipal
    final AuthenticatedPrincipal principal,
    @Parameter(description = "Filter by year-month (format: yyyy-MM)", example = "2026-01")
    @RequestParam(required = false)
    @DateTimeFormat(pattern = "yyyy-MM")
    final YearMonth yearMonth
  ) {
    // spotless:on
    List<TransactionResponse> transactions = mapper
      .toResponse(listTransactionsUseCase.getAll(principal.userId(), yearMonth));
    return ResponseEntity.ok(transactions);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get transaction by ID", description = "Retrieves detailed information for a specific transaction")
  @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully")
  public ResponseEntity<TransactionDetailResponse> get(
    @Parameter(hidden = true) @AuthenticationPrincipal final AuthenticatedPrincipal principal,
    @Parameter(description = "Transaction ID", required = true) @PathVariable final UUID id) {
    TransactionDetailResponse transaction = mapper.toResponse(getTransactionUseCase.getById(id, principal.userId()));
    return ResponseEntity.ok(transaction);
  }

  @PostMapping
  @Operation(summary = "Create a new transaction", description = "Creates a new transaction for the authenticated user")
  @ApiResponse(responseCode = "201", description = "Transaction created successfully")
  public ResponseEntity<Void> create(
    @Parameter(description = "Transaction creation request", required = true) @Valid @RequestBody final CreateTransactionRequest request,
    @Parameter(hidden = true) @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    UUID transactionId = createTransactionUseCase.create(mapper.toCommand(request, principal.userId()));
    return ResponseEntity.created(URI.create("/api/v1/transactions/" + transactionId)).build();
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a transaction", description = "Updates an existing transaction for the authenticated user")
  @ApiResponse(responseCode = "204", description = "Transaction updated successfully")
  public ResponseEntity<Void> update(
    @Parameter(description = "Transaction ID", required = true) @PathVariable final UUID id,
    @Parameter(description = "Transaction update request", required = true) @Valid @RequestBody final UpdateTransactionRequest request,
    @Parameter(hidden = true) @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    updateTransactionUseCase.update(mapper.toCommand(request, id, principal.userId()));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a transaction", description = "Deletes a transaction by ID for the authenticated user")
  @ApiResponse(responseCode = "204", description = "Transaction deleted successfully")
  public ResponseEntity<Void> delete(
    @Parameter(description = "Transaction ID", required = true) @PathVariable final UUID id,
    @Parameter(hidden = true) @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    deleteTransactionUseCase.deleteById(id, principal.userId());
    return ResponseEntity.noContent().build();
  }

  // spotless:off
  @GetMapping("/balance")
  @Operation(
    summary = "Get monthly balance",
    description = "Retrieves the monthly balance (income, expense, net) for the authenticated user")
  @ApiResponse(responseCode = "200", description = "Monthly balance retrieved successfully")
  public ResponseEntity<MonthlyBalanceResponse> getMonthlyBalance(
    @Parameter(description = "Filter by year-month (format: yyyy-MM)", example = "2026-01", required = true)
    @DateTimeFormat(pattern = "yyyy-MM")
    @RequestParam final YearMonth month,
    @Parameter(hidden = true)
    @AuthenticationPrincipal final AuthenticatedPrincipal principal
  ) {
    // spotless:on
    MonthlyBalance balance = getMonthlyBalanceUseCase.getMonthlyBalance(principal.userId(), month);
    return ResponseEntity.ok(mapper.toResponse(balance));
  }
}
