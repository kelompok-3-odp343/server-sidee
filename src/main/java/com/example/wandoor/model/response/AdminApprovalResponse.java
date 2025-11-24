package com.example.wandoor.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminApprovalResponse {
    private String message;
    private MenuData menudata;
    private String updatedTime;

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
