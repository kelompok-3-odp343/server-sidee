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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {

    @JsonProperty("data")
    private AccountData data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AccountData {
        @JsonProperty("targetAccountDetail")
        private TargetAccountDetail targetAccountDetail;

        @JsonProperty("accountList")
        private List<AccountListItem> accountList;
    }

    // ✅ builder perlu di sini
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TargetAccountDetail {
        @JsonProperty("account_number")
        private String accountNumber;

        @JsonProperty("account_name")
        private String accountName;

        @JsonProperty("product_name")
        private String productName;

        @JsonProperty("effective_balance")
        private BigDecimal effectiveBalance;

        @JsonProperty("is_main_account")
        private Boolean isMainAccount;

        @JsonProperty("account_status")
        private String accountStatus;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AccountListItem {
        @JsonProperty("accountNumber")
        private String accountNumber;
    }
}
