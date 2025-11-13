package com.example.wandoor.model.response;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavingsResponse {

    @JsonProperty("targetAccountDetail")
    private TargetAccountDetail targetAccountDetail;

    @JsonProperty("accountList")
    private List<AccountListItem> accountList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetAccountDetail {
        @JsonProperty("Total_EffectiveBalance")
        private BigDecimal totalEffectiveBalance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountListItem {
        @JsonProperty("account_number")
        private String accountNumber;

        @JsonProperty("account_name")
        private String accountName;

        @JsonProperty("product_name")
        private String productName;

        @JsonProperty("effective_balance_total")
        private BigDecimal effectiveBalanceTotal;

        @JsonProperty("is_main_account")
        private Boolean isMainAccount;

        @JsonProperty("account_status")
        private String accountStatus;
    }
}
