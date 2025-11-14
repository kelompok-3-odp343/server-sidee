package com.example.wandoor.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListAdminResponse {
    private String message;
    private boolean status;
    private List<Map<String, Object>> data;
}
