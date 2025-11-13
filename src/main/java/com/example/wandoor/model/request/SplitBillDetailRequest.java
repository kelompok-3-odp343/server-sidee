package com.example.wandoor.model.request;
import jakarta.validation.constraints.NotBlank;

public record SplitBillDetailRequest(
    @NotBlank
    String splitBillId
    ) {}
