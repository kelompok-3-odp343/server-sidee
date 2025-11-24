package com.example.wandoor.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminTransactionListResponse {

    private String customerId;
    private String nik;
    private String customerName;
    private List<TransactionData> transactionHistories;

    @Data
    @Builder
    public static class TransactionData {
        private String transactionId;
        private String accountNumber;
        private String productType;
        private String productTypeLabel;
        private Double transactionAmount;
        private String status;
        private String paymentTime;
        private String transactionCategory;
    }
}
