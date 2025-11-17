package com.example.wandoor.model.request;

public record PatchSplitBillRequest (
    String splitBillId,
    String memberId
) {}
