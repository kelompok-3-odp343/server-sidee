package com.example.wandoor.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.wandoor.model.entity.Profile;
import com.example.wandoor.model.entity.UserAuth;
import com.example.wandoor.model.response.UserListAdminResponse;
import com.example.wandoor.repository.AccountRepository;
import com.example.wandoor.repository.ProfileRepository;
import com.example.wandoor.repository.UserListAdminRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserListAdminService {

    private final UserListAdminRepository repository;
    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;

    public UserListAdminResponse getAllUsersList(String userIdHeader) {

        long totalUsers = repository.countNasabahUsers();
        long activeUsers = repository.countActiveNasabahUsers();
        long blockedUsers = repository.countBlockedNasabahUsers();

        List<UserAuth> users = repository.findAllNasabahUsers();

        List<UserListAdminResponse.UserItem> userList = new ArrayList<>();
        long totalAccounts = 0L;
        for (UserAuth ua : users) {
            Profile p = profileRepository.findById(ua.getUserId()).orElse(null);
            String fullName;
            if (p != null) {
                String mid = p.getMiddleName() == null ? "" : p.getMiddleName();
                fullName = p.getFirstName() + " " + mid + " " + p.getLastName();
            } else {
                fullName = ua.getUsername();
            }

            long countAcc = accountRepository.countByUserId(ua.getUserId());
            totalAccounts += countAcc;

            boolean blocked = ua.getIsUserBlocked() != null && ua.getIsUserBlocked() == 1;
            userList.add(new UserListAdminResponse.UserItem(
                    ua.getUserId(),
                    p != null ? p.getId() : ua.getUserId(),
                    fullName,
                    countAcc,
                    blocked
            ));
        }

        double avgAccountPerUser = users.isEmpty() ? 0.0 : (double) totalAccounts / users.size();

        return new UserListAdminResponse(
                true,
                "Success",
                totalUsers,
                activeUsers,
                blockedUsers,
                avgAccountPerUser,
                userList
        );
    }
    
}
