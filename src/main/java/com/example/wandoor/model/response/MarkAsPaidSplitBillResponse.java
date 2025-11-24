package com.example.wandoor.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarkAsPaidSplitBillResponse {
    private String message;
    private String splitBillId;
    private String memberId;
}
