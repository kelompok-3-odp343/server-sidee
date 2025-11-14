package com.example.wandoor.model.response;

import com.example.wandoor.model.enums.DebitCredit;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RecordBuilder
public record TransactionHistoryResponse(
        Integer month,
        String year,
        String productType,
        List<TrxResponse> transaction
) {

}