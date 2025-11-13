package com.example.wandoor.model.response;
import java.math.BigDecimal;
import java.util.List;

public record SplitBillDetailResponse (
    Data data
) {
    public record Data(
        String splitBillId,
        String splitBillTitle,
        String currency,
        String transactionId,
        String refId,
        BigDecimal totalBill,
        String createdTime,
        String transactionDate,
        List<Member> members
    ) {
        public record Member(
            String memberId,
            String memberName,
            BigDecimal amount,
            String paymentTime,
            Boolean hasPaid
        ) {
        }
    }
}
