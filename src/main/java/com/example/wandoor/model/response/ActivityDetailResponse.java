package com.example.wandoor.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ActivityDetailResponse {
    private String activityId;
    private Map<String, Object> activityData;
    private MakerData makerData;
    private CheckerData checkerData;
    private MenuData menuData;
    private ApproverData approverData;
    private String checkerUpdatedTime;
    private String approverUpdatedTime;
    private String createdTime;
    private String createdBy;

    @Data
    @Builder
    public static class CheckerData {
        private String userId;
        private String npp;
        private String fullName;
    }

    @Data
    @Builder
    public static class MakerData {
        private String userId;
        private String npp;
        private String fullName;
    }

    @Data
    @Builder
    public static class ApproverData {
        private String userId;
        private String npp;
        private String fullName;
    }

    @Data
    @Builder
    public static class MenuData {
        private String menuId;
        private String menuName;
        private String menuAction;
        private String actionFlow;
    }
}
