package com.example.wandoor.model.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavingsResponse {
    private Meta meta;
    private Summary summary;
    private Insights insights;
    private List<CategoryBreakdown> category_breakdown;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Meta {
        private String month;    // e.g. "2025-05"
        private String currency; // e.g. "IDR"
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Summary {
        private BigDecimal total_debit;
        private BigDecimal total_credit;
        private BigDecimal net_growth;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Insights {
        private TopCategory top_category;
        private BiggestIncoming biggest_incoming;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopCategory {
        private String name;
        private BigDecimal spent_amount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BiggestIncoming {
        private String type;
        private BigDecimal amount;
        private String source;    // e.g. "Taplus Bisnis"
        private String date;      // ISO string e.g. "2025-05-31T09:20:11Z"
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private BigDecimal total_amount;
        private Integer percent;
    }
}
