package com.example.wandoor.model.response;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.math.BigDecimal;
import java.util.List;

@RecordBuilder
public record SplitBillsListResponse(
        List<SplitBillData> data
) {
    public record SplitBillData (
        String splitBillId,
        String splitBillTitle,
        String transactionId,
        String currency,
        BigDecimal totalBill,
        BigDecimal remainingBillAmount,
        BigDecimal paidBillAmount,
        Integer countPaidMember,
        Integer countUnpaidMember,
        List<SplitBillMemberDetail> splitBillMemberDetail
    ) {
        public record SplitBillMemberDetail(
                String memberId,
                String memberName,
                BigDecimal totalBillAmount,
                Boolean hasPaid
        ){}
    }
}
