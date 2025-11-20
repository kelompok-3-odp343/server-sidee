package com.example.wandoor.model.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record EditSplitBillRequest (
        @NotBlank
        String splitBillId,
        @NotBlank
        String transactionId,
        @NotBlank
        String splitBillTitle,
        @NotNull
        BigDecimal totalAmount,
        @NotEmpty
        @NotNull
        List<BillMembers> billMembers
) {
    public record BillMembers (
        String memberId,
        @NotNull String memberName,
        @NotNull BigDecimal amountShare,
        @NotNull Boolean hasPaid
    ){}
}
