package com.example.wandoor.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.wandoor.model.entity.MsMenu;
import com.example.wandoor.repository.AdminMenuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMenuAccessService {
    private final AdminMenuRepository adminMenuRepository;

    public Map<String, Map<String, Map<String, Object>>> getMenuStructure() {
        try {
                List<MsMenu> allMenus = adminMenuRepository.findAll();
                return allMenus.stream()
                        .collect(Collectors.groupingBy(
                                MsMenu::getMenuName,
                                Collectors.toMap(
                                    MsMenu::getMenuAction,
                                    menu -> Map.of(
                                        "menu_id", menu.getId(),
                                        "action_flow", menu.getActionFlow()
                                    )
                                )
                        ));
        } catch (ResponseStatusException e) {
            throw e;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch split bills");
        }
    }
}
