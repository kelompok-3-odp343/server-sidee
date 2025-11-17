package com.example.wandoor.model.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminActivityCreationResponse {
    private String activityId;
    private String Message;
    private MenuData menuData;
    private String createdTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuData {
        private String menuId;
        private String menuName;
        private String menuAction;
        private String actionFlow;
    }
}
