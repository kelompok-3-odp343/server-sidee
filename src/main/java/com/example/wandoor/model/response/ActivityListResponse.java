package com.example.wandoor.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityListResponse {
    List<ActivityData> activityList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityData{
        private String id;
        private String menuId;
        private String menuName;
        private String actionFlow;
        private String actionMenu;
        private String createdTime;
        private String createdBy;
        private String createdByDetail;
        private String checkerId;
        private String checkerDetail;
        private String approverId;
        private String approverDetail;
        private String activityStatus;
    }
}
