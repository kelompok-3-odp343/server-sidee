package com.example.wandoor.service;

import com.example.wandoor.model.entity.AdminMenuAccess;
import com.example.wandoor.repository.AdminMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMenuAccessService {
    private final AdminMenuRepository adminMenuRepository;

    public Map<String, Map<String, String>> getMenuStructure() {
        List<AdminMenuAccess> allMenus = adminMenuRepository.findAll();
        return allMenus.stream()
                .collect(Collectors.groupingBy(
                        AdminMenuAccess::getMenuName,
                        Collectors.toMap(AdminMenuAccess::getMenuAction, AdminMenuAccess::getActionFlow)
                ));
    }
}
