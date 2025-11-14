package com.example.wandoor.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@RecordBuilder
public record TransactionHistoryRequest(
        @JsonProperty("month") Integer month,
        @JsonProperty("year") Integer year,
        @JsonProperty("accountNumber") String accountNumber,
        @JsonProperty("productType") @NotBlank String productType
) {
    public TransactionHistoryRequest {
        if (month == null) month = LocalDate.now().getMonthValue();
        if (year == null) year = LocalDate.now().getYear();
    }

    @AssertTrue(message = "Account number is required for product type SAV")
    public boolean isAccountNumberValid() {
        if ("SAV".equalsIgnoreCase(productType)) {
            return accountNumber != null && !accountNumber.isBlank();
        }
        return true;
    }
}