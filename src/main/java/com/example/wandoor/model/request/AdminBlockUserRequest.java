package com.example.wandoor.model.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBlockUserRequest {
    private UserData userData;
    private String reason;
    private CheckerData checkerData;
    private MenuData menuData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserData {
        private String userId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckerData {
        private String userId;
        private String npp;
        private String fullName;
    }

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
