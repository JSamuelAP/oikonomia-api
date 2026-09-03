package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.in.rest;

import java.net.URI;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
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

import dev.jsamuelap.oikonomiaapi.shared.security.jwt.AuthenticatedPrincipal;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.CreateTransactionUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.GetTransactionUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.ListTransactionsUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {
  private final ListTransactionsUseCase listTransactionsUseCase;
  private final GetTransactionUseCase getTransactionUseCase;
  private final CreateTransactionUseCase createTransactionUseCase;
  private final TransactionRestMapper mapper;

  @GetMapping()
  public ResponseEntity<List<TransactionResponse>> getAll(
    @AuthenticationPrincipal final AuthenticatedPrincipal principal,
    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") final YearMonth yearMonth) {
    List<TransactionResponse> transactions = mapper
      .toResponse(listTransactionsUseCase.getAll(principal.userId(), yearMonth));
    return ResponseEntity.ok(transactions);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TransactionResponse> get(@AuthenticationPrincipal final AuthenticatedPrincipal principal,
    @PathVariable final UUID id) {
    TransactionResponse transaction = mapper.toResponse(getTransactionUseCase.getById(id, principal.userId()));
    return ResponseEntity.ok(transaction);
  }

  @PostMapping
  public ResponseEntity<Void> create(@Valid @RequestBody final CreateTransactionRequest request,
    @AuthenticationPrincipal final AuthenticatedPrincipal principal) {
    UUID transactionId = createTransactionUseCase.create(mapper.toCommand(request, principal.userId()));
    return ResponseEntity.created(URI.create("/api/v1/transactions/" + transactionId)).build();
  }
}
