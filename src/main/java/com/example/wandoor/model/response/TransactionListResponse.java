package com.example.wandoor.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionListResponse {

    private String customerId;
    private String nik;
    private String customerName;
    private List<TransactionItem> transactionHistories;

    @Data
    @AllArgsConstructor
    public static class TransactionItem {
        private String transactionId;
        private String accountNumber;
        private String productType;
        private String productTypeLabel;
        private BigDecimal transactionAmount;
        private String status;
        private LocalDateTime paymentTime;
        private String transactionCategory;
    }
}
