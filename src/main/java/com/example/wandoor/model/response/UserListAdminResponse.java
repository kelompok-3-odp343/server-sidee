package com.example.wandoor.model.response;

import java.util.List;

public record UserListAdminResponse(
        boolean status,
        String message,
        long totalUsers,
        long activeUsers,
        long blockedUsers,
        double avgAccountPerUser,
        List<UserItem> users
) {
    public record UserItem(
            String userId,
            String customerId,
            String customerName,
            long countAccount,
            boolean isBlocked
    ){}
}
