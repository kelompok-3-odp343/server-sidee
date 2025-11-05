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

        // ✅ 1. Validasi role
        String userRole = repository.findRoleByUserId(userIdHeader);

        if (userRole == null) {
            return Map.of(
                "message", "User tidak ditemukan",
                "status", false,
                "data", List.of()
            );
        }

        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            return Map.of(
                "message", "Forbidden - Anda bukan admin",
                "status", false,
                "data", List.of()
            );
        }

        // ✅ 2. Ambil semua user dengan role NASABAH
        List<Object[]> users = repository.findAllNasabahUserList();

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Object[] row : users) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", row[0]);
            map.put("customerId", row[1]);
            map.put("customerName", row[2]);
            map.put("countAccount", row[3]);
            map.put("isBlocked", parseBlocked(row[4]));
            dataList.add(map);
        }

        // ✅ 3. Kembalikan response
        return Map.of(
            "message", "Users list fetched successfully",
            "status", true,
            "data", dataList
        );
    }

    private boolean parseBlocked(Object val) {
        if (val == null) return false;
        if (val instanceof Boolean b) return b;
        if (val instanceof Number n) return n.intValue() == 1;
        if (val instanceof String s) return s.equals("1") || s.equalsIgnoreCase("true");
        return false;
    }

    private String buildFullName(String first, String middle, String last) {
        return String.join(" ",
                Optional.ofNullable(first).orElse(""),
                Optional.ofNullable(middle).orElse(""),
                Optional.ofNullable(last).orElse("")).trim().replaceAll(" +", " ");
    }
}
