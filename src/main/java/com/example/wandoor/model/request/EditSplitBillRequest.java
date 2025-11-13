package com.example.wandoor.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record EditSplitBillRequest (
        @NotBlank
        String splitBillId,
        @NotBlank
        String transactionId,
        @NotBlank
        String splitBillTitle,
        @NotBlank
        BigDecimal totalAmount,
        @NotEmpty
        @NotNull
        List<BillMembers> billMembers
) {
    public record BillMembers (
        @NotBlank String memberId,
        @NotBlank String memberName,
        @NotNull BigDecimal amountShare,
        @NotNull Boolean hasPaid
    ){}
}
