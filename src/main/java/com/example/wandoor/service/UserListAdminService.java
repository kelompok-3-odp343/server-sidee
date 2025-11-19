package com.example.wandoor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import com.example.wandoor.repository.UserListAdminRepository;
import com.example.wandoor.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserListAdminService {

    private final UserListAdminRepository repository;

    public Map<String, Object> getAllUsersList(String userIdHeader) {
        try {
            Object[] summary = repository.getUserSummary().get(0);
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
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while fetching user list", e);
        }
    }
    

    private boolean parseBoolean(Object val) {
        return val instanceof Boolean b ? b : Integer.parseInt(val.toString()) == 1;
    }

    private String cleanName(String name) {
        return name.trim().replaceAll(" +", " ");
    }
}