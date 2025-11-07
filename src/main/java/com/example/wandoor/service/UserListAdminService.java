package com.example.wandoor.service;

import com.example.wandoor.repository.UserListAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserListAdminService {

    private final UserListAdminRepository repository;

    public Map<String, Object> getAllUsersList(String userIdHeader) {

        String userRole = repository.findRoleByUserId(userIdHeader);
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            return Map.of(
                "status", false,
                "message", "Forbidden - Anda bukan admin"
            );
        }
    
        // ✅ Ambil summary
        Object[] summary = repository.getUserSummary().get(0);
    
        // ✅ Ambil list user
        List<Object[]> users = repository.findAllNasabahUserList();
    
        List<Map<String, Object>> userList = new ArrayList<>();
        for (Object[] row : users) {
            userList.add(Map.of(
                "userId", row[0],
                "customerId", row[1],
                "customerName", row[2],
                "countAccount", row[3],
                "isBlocked", row[4]
            ));
        }
    
        return Map.of(
            "status", true,
            "message", "Success",
            "totalUsers", summary[0],
            "activeUsers", summary[1],
            "blockedUsers", summary[2],
            "avgAccountPerUser", summary[3],
            "users", userList
        );
    }
    

    private boolean parseBoolean(Object val) {
        return val instanceof Boolean b ? b : Integer.parseInt(val.toString()) == 1;
    }

    private String cleanName(String name) {
        return name.trim().replaceAll(" +", " ");
    }
}