package com.example.wandoor.service;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.AdminProfile;
import com.example.wandoor.model.entity.TrActivity;
import com.example.wandoor.model.request.AdminActivityDetailRequest;
import com.example.wandoor.model.request.AdminBlockUnblockUserRequest;
import com.example.wandoor.model.response.ActivityDetailResponse;
import com.example.wandoor.model.response.ActivityListResponse;
import com.example.wandoor.model.response.AdminActivityCreationResponse;
import com.example.wandoor.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.rowset.serial.SerialClob;
import java.io.BufferedReader;
import java.io.Reader;
import java.sql.Clob;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class AdminService {
    private final RoleManagementRepository roleManagementRepository;
    private final TrActivityRepository trActivityRepository;
    private final AdminProfileRepository adminProfileRepository;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private final ObjectMapper objectMapper;
    private final UserAuthRepository userAuthRepository;
    private final EntityManager entityManager;
    private final ProfileRepository profileRepository;

    public ActivityListResponse getAllActivityPerAdmin() {
        try {
            var adminUserId = RequestContext.get().getUserId();

            var adminProfile = adminProfileRepository.findById(adminUserId)
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.CONFLICT,
                            "NO_SUCH_ADMIN",
                            "No Such Admin"
                    ));

            var roleData = roleManagementRepository.findById(adminProfile.getRoleId())
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.CONFLICT,
                            "INVALID_ROLE_ID",
                            "Invalid Role Id from Admin Profile"
                    ));

            List<TrActivity> activities = Collections.emptyList();

            String roleName = roleData.getRoleName() == null ? "" : roleData.getRoleName();

            if (roleName.equalsIgnoreCase("MAKER")) {
                activities = trActivityRepository.findByMakerId(adminProfile.getId());

            } else if (roleName.equalsIgnoreCase("CHECKER")) {
                activities = trActivityRepository.findByCheckerId(adminProfile.getId());

            } else if (roleName.equalsIgnoreCase("APPROVER")) {
                activities = trActivityRepository.findByApproverId(adminProfile.getId());

            } else {
                activities = Collections.emptyList();
            }

            List<ActivityListResponse.ActivityData> result = activities.stream()
                    .map(this::toActivity)
                    .toList();

            return ActivityListResponse.builder()
                    .activityList(result)
                    .build();



        } catch (ResponseStatusException e) {
            throw e;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch Admin Activity List");
        }


    }

    private ActivityListResponse.ActivityData toActivity(TrActivity a) {
        String createdTime = null;
        if (a.getCreatedTime() != null) {
            createdTime = a.getCreatedTime().format(ISO_FORMATTER);
        }
        return ActivityListResponse.ActivityData.builder()
                .id(a.getId())
                .menuId(a.getMenuId())
                .menuName(a.getMenuName())
                .actionFlow(a.getActionFlow())
                .actionMenu(a.getActionMenu())
                .createdTime(createdTime)
                .createdBy(nullSafe(a.getCreatedBy()))
                .createdByDetail(getUserDetail(a.getCreatedBy()))
                .checkerId(nullSafe(a.getCheckerId()))
                .checkerDetail(getUserDetail(a.getCheckerId()))
                .approverId(nullSafe(a.getApproverId()))
                .approverDetail(getUserDetail(a.getApproverId()))
                .activityStatus(nullSafe(a.getStatus()))
                .build();
    }

    private String getUserDetail(String userId) {
        if(userId == null || userId.isBlank()) return "";
        return adminProfileRepository.findById(userId)
                .map(p -> {
                    var npp = p.getNpp() == null ? "" : p.getNpp();
                    var fullName = p.getFullName() == null ? "" : p.getFullName();
                    if (npp.isBlank() && fullName.isBlank()) return "";
                    if (npp.isBlank()) return fullName;
                    if (fullName.isBlank()) return npp;
                    return npp + " - " + fullName;
                }).orElse("");
    }

    private String nullSafe(String s) {
        return Objects.requireNonNullElse(s, "");
    }

    public ActivityDetailResponse getActivityDetail(AdminActivityDetailRequest request) {

        try {
            TrActivity entity = trActivityRepository.findById(request.getActivityId())
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.CONFLICT,
                            "INVALID_ACTIVITY_ID",
                            "Activity not Found"));

            Map<String, Object> activityData = parseClobToMap(entity.getMetaData());

            // fetch Admin Profile
            AdminProfile maker = adminProfileRepository.findById(entity.getMakerId()).orElse(null);
            AdminProfile checker = entity.getCheckerId() == null ? null : adminProfileRepository.findById(entity.getCheckerId()).orElse(null);
            AdminProfile approver = entity.getApproverId() == null ? null : adminProfileRepository.findById(entity.getApproverId()).orElse(null);

            // Build Maker Data
            ActivityDetailResponse.MakerData makerData = maker == null ? null :
                    ActivityDetailResponse.MakerData.builder()
                            .userId(maker.getId())
                            .npp(maker.getNpp())
                            .fullName(maker.getFullName())
                            .build();

            ActivityDetailResponse.CheckerData checkerData = checker == null ? null :
                    ActivityDetailResponse.CheckerData.builder()
                            .userId(checker.getId())
                            .npp(checker.getNpp())
                            .fullName(checker.getFullName())
                            .build();
            ActivityDetailResponse.ApproverData approverData = approver == null ? null :
                    ActivityDetailResponse.ApproverData.builder()
                            .userId(approver.getId())
                            .npp(approver.getNpp())
                            .fullName(approver.getFullName())
                            .build();

            ActivityDetailResponse.MenuData menuData = ActivityDetailResponse.MenuData.builder()
                    .menuId(entity.getMenuId())
                    .menuName(entity.getMenuName())
                    .menuAction(entity.getActionMenu())
                    .actionFlow(entity.getActionFlow())
                    .build();

            return ActivityDetailResponse.builder()
                    .activityId(entity.getId())
                    .activityData(activityData)
                    .makerData(makerData)
                    .menuData(menuData)
                    .checkerData(checkerData)
                    .approverData(approverData)
                    .checkerUpdatedTime(entity.getUpdatedTimeChecker() == null ? null : entity.getUpdatedTimeChecker().toString())
                    .approverUpdatedTime(entity.getUpdatedTimeApprover() == null ? null : entity.getUpdatedTimeApprover().toString())
                    .createdTime(entity.getCreatedTime() == null ? null : entity.getCreatedTime().toString())
                    .createdBy(entity.getCreatedBy())
                    .build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch Admin Activity List");
        }
    }

    private Map<String, Object> parseClobToMap(Clob clob) {
        if (clob == null) return null;
        try (Reader reader = clob.getCharacterStream();
             BufferedReader br = new BufferedReader(reader)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            String json = sb.toString().trim();
            if (json.isEmpty()) {
                return Collections.emptyMap();
            }

            return objectMapper.readValue(sb.toString(),
                    new TypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            log.warn("Failed to parse clob to map, returning empty map. error={}", e.toString(), e);
            // throw new RuntimeException("Failed to read clob", e);
            return Collections.emptyMap();

        }
    }

    public AdminActivityCreationResponse adminBlockUser(AdminBlockUnblockUserRequest request) {
        try {
            return AdminActivityCreationResponse.builder().build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch Admin Activity List");
        }

        public AdminActivityCreationResponse blockUser(AdminBlockUnblockUserRequest request) {
            try {
                var adminUserId = RequestContext.get().getUserId();
                // validate user admin
                var adminProfile = adminProfileRepository.findById(adminUserId)
                        .orElseThrow(() -> new BusinessException(
                                HttpStatus.CONFLICT,
                                "NO_SUCH_ADMIN",
                                "No Such Admin"
                        ));

                var roleData = roleManagementRepository.findById(adminProfile.getRoleId())
                        .orElseThrow(() -> new BusinessException(
                                HttpStatus.CONFLICT,
                                "INVALID_ROLE_ID",
                                "Invalid Role Id from Admin Profile"
                        ));

                if ("CHECKER".equalsIgnoreCase(roleData.getRoleName())
                || "APPROVER".equalsIgnoreCase(roleData.getRoleName())) {
                    throw new BusinessException(
                            HttpStatus.CONFLICT,
                            "UNAUTHORIZED_AMDMIN_ROLE",
                            "Only Maker Can Make Activity"
                    );
                }

                // validate user
                var userProfileData = profileRepository.findById(adminUserId)
                        .orElseThrow(() -> new BusinessException(
                                HttpStatus.CONFLICT,
                                "INVALID_USER_TO_BLOCK",
                                "User To Block Not Found"
                        ));

                var userAuthData = userAuthRepository.findByUserId(userProfileData.getId())
                        .orElseThrow(() -> new BusinessException(
                                HttpStatus.CONFLICT,
                                "INVALID_USER_AUTH_DATA",
                                "Invalid user auth data"
                        ));

                if (userAuthData.getIsUserBlocked() != null && userAuthData.getIsUserBlocked() == 1) {
                    throw new BusinessException(
                            HttpStatus.CONFLICT,
                            "USER_HAS_ALREADY_BEEN_BLOCKED",
                            "User has already been blocked"
                    );
                }

                trActivityRepository.findExistingActivity(request.getUserData().getUserId(), request.getMenuData().getMenuId())
                        .ifPresent(a -> {
                            throw new BusinessException(
                                    HttpStatus.CONFLICT,
                                    "PENDING_SIMILAR_ACTIVITY",
                                    "Pending Similar Activity"
                            );
                        });



            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch Admin Activity List");
            }
        }


    }

    private String generateNextActivityId(){
        List<String> latestList = trActivityRepository.findLatestActivityIds();

        if (latestList.isEmpty()) {
            return "ACK00001";
        }
        String latest = latestList.get(0);
        String numberPart = latest.substring(3);
        int number = Integer.parseInt(numberPart) + 1;

        return "ACK" + String.format("%05d", number);
    }

    private Clob convertToClob(Object data) {
        try {
            String json = new ObjectMapper().writeValueAsString(data);
            return new SerialClob(json.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to Convert to Clob", e);
        }
    }

    private AdminActivityCreationResponse handleActionFlow(
            AdminBlockUnblockUserRequest request
    ) {

        String flow = request.getMenuData().getActionFlow().toUpperCase();
        return switch (flow) {
            case "CHECKER_AND_APPROVER" -> {
                String actId = createActivity(
                        request,
                        makerId
                )
            }
        }
    }

    private String createActivity(
            AdminBlockUnblockUserRequest request,
            String makerId,
            String status,
            boolean includeChecker,
            boolean includeApprover
    ) {
        List<String> list = trActivityRepository.findLatestActivityIds();
        String newActId;

        if (list.isEmpty()){
            newActId = "ACK00001";
        } else {
            String latest = list.get(0);
            int prefixEnd = 0;
            while (prefixEnd < latest.length() && !Character.isDigit(latest.charAt(prefixEnd))) {
                prefixEnd++;
            }

            String prefix = latest.substring(0, prefixEnd);
            String numberPart = latest.substring(prefixEnd);
            int next = Integer.parseInt(numberPart) + 1;

            newActId = prefix + String.format("%0" + numberPart.length() + "d", next);
        }

        // Build Activity Entity
        TrActivity activity = TrActivity.builder()
                .id(newActId)
                .makerId(makerId)
                .checkerId(includeChecker && request.getCheckerData() != null ? request.getCheckerData().getUserId() : null)
                .approverId(includeApprover && request.getApproverData() != null ? request.getApproverData().getUserId() : null)
                .menuId(request.getMenuData().getMenuId())
                .menuName(request.getMenuData().getMenuName())
                .actionMenu(request.getMenuData().getMenuAction())
                .actionFlow(request.getMenuData().getActionFlow())
                .identifier(request.getUserData().getUserId())
                .status(status)
                .metaData(convertToClob(request.getUserData()))
                .createdTime(LocalDateTime.now())
                .createdBy(makerId)
                .build();

            trActivityRepository.save(activity);
            return newActId;
    }
}
