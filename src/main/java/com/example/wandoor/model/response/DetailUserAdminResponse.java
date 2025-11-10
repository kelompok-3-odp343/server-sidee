package com.example.wandoor.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailUserAdminResponse {

    private String userId;
    private String customerId;
    private String customerName;
    private boolean isBlocked;
    private List<AccountDetail> accounts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDetail {
        private String accountNumber;
        private String productType;
        private String productName;
        private String accountStatus;
        private double effectiveBalance;
    }
}
