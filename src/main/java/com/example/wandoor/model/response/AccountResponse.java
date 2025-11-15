package com.example.wandoor.model.response;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {

    private AccountData data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccountData {
        private TargetAccountDetail targetAccountDetail;
        private List<AccountListItem> accountList; // boleh null
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TargetAccountDetail {
        private String accountNumber;
        private String accountHolderName;
        private String productType;
        private BigDecimal effectiveBalance;
        private boolean isMainAccount;
        private String account_status;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccountListItem {
        private String accountNumber;
    }
}
