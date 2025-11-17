package com.example.wandoor.model.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record DetailTrxResponse(
        String transactionId,
        LocalDateTime transactionDate,
        String transactionType,
        String paymentMethod,       // QRIS / E-Wallet / Transfer / VA
        String transactionCategory, // Food, Utilities, dll
        String partyName,
        String partyDetail,
        BigDecimal amount,
        String debitCredit,
        String productSubCategory
) {
}
