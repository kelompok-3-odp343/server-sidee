package com.example.wandoor.model.response;

import java.util.List;

import lombok.Data;
//adding
@Data
public class TrxSummaryResponse {
    private double totalAsset;
    private double totalTimeDeposit;
    private double totalLifegoals;
    private double totalSaving;
    private double totalPensionFund;

    private List<Category> categories;
    private List<UserItem> users;

    @Data
    public static class Category {
        private String categoryName;
        private double total;
        private int percentage;
    }

    @Data
    public static class UserItem {
        private String userid;
        private String customerId;
        private String nik;
        private String customerName;
    }
}