package com.example.wandoor.service;

import com.example.wandoor.model.entity.MsMenu;
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
        List<MsMenu> allMenus = adminMenuRepository.findAll();
        return allMenus.stream()
                .collect(Collectors.groupingBy(
                        MsMenu::getMenuName,
                        Collectors.toMap(MsMenu::getMenuAction, MsMenu::getActionFlow)
                ));
    }
}
