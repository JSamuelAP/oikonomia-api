package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.in.rest;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;

import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.CreateTransactionCommand;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.TransactionDetailView;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.TransactionView;

@Mapper(componentModel = "spring")
public interface TransactionRestMapper {
  List<TransactionResponse> toResponse(List<TransactionView> transactions);

  TransactionResponse toResponse(TransactionView transaction);

  TransactionDetailResponse toResponse(TransactionDetailView transaction);

  CreateTransactionCommand toCommand(CreateTransactionRequest request, UUID userId);
}
