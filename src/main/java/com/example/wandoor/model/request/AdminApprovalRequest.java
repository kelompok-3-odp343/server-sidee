package com.example.wandoor.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminApprovalRequest {
    private String activityId;
    @JsonProperty("isApprove")
    private boolean approve;
    private ApproverData approverData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApproverData {
        private String userId;
        private String npp;
        private String fullName;
    }
}
