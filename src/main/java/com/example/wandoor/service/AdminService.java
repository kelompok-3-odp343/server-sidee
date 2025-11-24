package com.example.wandoor.service;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.AdminProfile;
import com.example.wandoor.model.entity.RoleManagement;
import com.example.wandoor.model.entity.TrActivity;
import com.example.wandoor.model.request.AdminActivityDetailRequest;
import com.example.wandoor.model.request.AdminApprovalRequest;
import com.example.wandoor.model.request.AdminBlockUnblockUserRequest;
import com.example.wandoor.model.response.ActivityDetailResponse;
import com.example.wandoor.model.response.ActivityListResponse;
import com.example.wandoor.model.response.AdminActivityCreationResponse;
import com.example.wandoor.model.response.AdminApprovalResponse;
import com.example.wandoor.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final AdminMenuRepository adminMenuRepository;

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
        } catch (BusinessException e) {
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

    @Transactional
    public AdminActivityCreationResponse adminBlockUser(AdminBlockUnblockUserRequest request) {
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

            System.out.println("roleData" + roleData);

            if ("CHECKER".equalsIgnoreCase(roleData.getRoleName())
                    || "APPROVER".equalsIgnoreCase(roleData.getRoleName())) {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "UNAUTHORIZED_AMDMIN_ROLE",
                        "Only Maker Can Make Activity"
                );
            }

            // validate user
            var userProfileData = profileRepository.findById(request.getUserData().getUserId())
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.CONFLICT,
                            "INVALID_USER_TO_BLOCK",
                            "User To Block Not Found"
                    ));

            System.out.println("userPorileData" + userProfileData);

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

            System.out.println("success find latest activity");

            return handleActionFlow(
                    request,
                    adminProfile,
                    () -> userAuthRepository.markBlockedById(userProfileData.getId()),
                    "Actvitiy created successfully"
            );
        } catch (BusinessException e) {
            throw e;
        } catch (ResponseStatusException e) {
            log.error("Critical error during user block process for user: {}", request.getUserData().getUserId(), e);
            throw e;
        } catch (Exception e) {
            log.error("BLOCK USER ERROR: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch Admin Activity List");
        }
    }

    @Transactional
    public AdminActivityCreationResponse adminUnblockUser(AdminBlockUnblockUserRequest request) {
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
            var userProfileData = profileRepository.findById(request.getUserData().getUserId())
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

            if (userAuthData.getIsUserBlocked() != null && userAuthData.getIsUserBlocked() == 0) {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "USER_HAS_ALREADY_BEEN_UNBLOCKED",
                        "User has already been unblocked"
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

            return handleActionFlow(
                    request,
                    adminProfile,
                    () -> userAuthRepository.markUnblockedById(userProfileData.getId()),
                    "Actvitiy created successfully"
            );
        } catch (BusinessException e) {
            throw e;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("UNBBLOCK USER ERROR: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch Admin Activity List");
        }
    }

    @Transactional
    public AdminApprovalResponse approveActivity(AdminApprovalRequest request) {
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

            if ("MAKER".equalsIgnoreCase(roleData.getRoleName())) {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "UNAUTHORIZED_AMDMIN_ROLE",
                        "Only Maker Can Make Activity"
                );
            }

            var trActivityData = trActivityRepository.findById(request.getActivityId())
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.CONFLICT,
                            "INVALID_ACTIVITY_ID",
                            "Invalid Activity Id"
                    ));

            return handleApprovalFlow(
                    request,
                    adminProfile,
                    roleData,
                    trActivityData);
//            var menuData = adminMenuRepository.findById(trActivityData.getMenuId());
        } catch (BusinessException e) {
            throw e;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Approval error: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch Admin Activity List");
        }
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
            AdminBlockUnblockUserRequest request,
            AdminProfile adminProfile,
            Runnable noApprovalOperation,
            String successMessage
    ) {

        String flow = request.getMenuData().getActionFlow() == null ? "" : request.getMenuData().getActionFlow().toUpperCase();
        return switch (flow) {
            case "CHECKER_AND_APPROVER" -> {
                String actId = createActivity(
                        request,
                        adminProfile.getId(),
                        "PENDING_CHECKER",
                        true,
                        true,
                        successMessage

                );
                yield buildResponse(actId, request, LocalDateTime.now(), successMessage);
            }
            case "APPROVER_ONLY" -> {
                String actId = createActivity(
                        request,
                        adminProfile.getId(),
                        "PENDING_APPROVER",
                        false,
                        true,
                        successMessage
                );
                yield buildResponse(actId, request, LocalDateTime.now(), successMessage);
            }
            case "NO_APPROVER" -> {
                noApprovalOperation.run();
                String actId = createActivity(
                        request,
                        adminProfile.getId(),
                        "APPROVED",
                        false,
                        true,
                        successMessage
                );
                yield buildResponse(actId, request, LocalDateTime.now(), successMessage);
            }
            default -> throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "INVALID_ACTION_FLOW",
                    "Action flow is not supported"
            );
        };
    }

    private AdminApprovalResponse handleApprovalFlow(
            AdminApprovalRequest request,
            AdminProfile adminProfile,
            RoleManagement roleData,
            TrActivity trActivityData
    ) {
        String message;
        if ("PENDING_CHECKER".equalsIgnoreCase(trActivityData.getStatus())) {
            if ("CHECKER".equalsIgnoreCase(roleData.getRoleName())) {
                // logic to insert new activity
                if (request.isApprove()){
                    var validateApproverData = adminProfileRepository.findById(request.getApproverData().getUserId())
                                    .orElseThrow(() -> new BusinessException(
                                            HttpStatus.CONFLICT,
                                            "INVALID_APPROVER_ID",
                                            "No such approver found"
                                    ));
                    updateActivity(
                            request.getActivityId(),
                            "PENDING_APPROVER",
                            true,
                            false,
                            request.isApprove(),
                            validateApproverData
                    );
                    message = "Success Approve Activity By Checker";
                    return buildApprovalResponse(trActivityData, message);

                } else {
                    updateActivity(
                            request.getActivityId(),
                            "REJECTED",
                            true,
                            false,
                            request.isApprove(),
                            null
                    );
                    message = "Success Reject Activity By Checker";
                    return buildApprovalResponse(trActivityData, message);
                }
            } else {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "INVALID_ROLE_TO_APPROVE",
                        "Activity Should be Approved by Checker"
                );
            }
        } else if ("PENDING_APPROVER".equalsIgnoreCase(trActivityData.getStatus())) {
            if ("APPROVER".equalsIgnoreCase(roleData.getRoleName())) {
                // logic for execute per service
                return switch (trActivityData.getActionMenu()) {
                    case "UNBLOCK_USER" -> {
                        if (request.isApprove()) {
                            Map<String, Object> metaData = parseClobToMap(trActivityData.getMetaData());
                            String userId = metaData.get("userId").toString();
                            userAuthRepository.markUnblockedById(userId);
                            updateActivity(
                                    request.getActivityId(),
                                    "APPROVED",
                                    false,
                                    true,
                                    request.isApprove(),
                                    null

                            );
                            yield buildApprovalResponse(trActivityData, "Success Approve Activity By Approver");
                        } else {
                            Map<String, Object> metaData = parseClobToMap(trActivityData.getMetaData());
                            String userId = metaData.get("userId").toString();
                            updateActivity(
                                    request.getActivityId(),
                                    "REJECTED",
                                    false,
                                    true,
                                    request.isApprove(),
                                    null
                            );
                            yield buildApprovalResponse(trActivityData, "Success Approve Activity By Approver");

                        }
                    }
                    case "BLOCK_USER" -> {
                        Map<String, Object> metaData = parseClobToMap(trActivityData.getMetaData());
                        String userId = metaData.get("userId").toString();
                        if (request.isApprove()) {
                            userAuthRepository.markBlockedById(userId);
                            updateActivity(
                                    request.getActivityId(),
                                    "APPROVED",
                                    false,
                                    true,
                                    request.isApprove(),
                                    null
                            );
                            message = "Success Approve Activity By Approver";
                            yield buildApprovalResponse(trActivityData, message);
                        } else {
                            updateActivity(
                                    request.getActivityId(),
                                    "REJECTED",
                                    false,
                                    true,
                                    request.isApprove(),
                                    null
                            );
                            message = "Success Approve Activity By Approver";
                            yield buildApprovalResponse(trActivityData, message);
                        }
                    }
                    default -> throw new BusinessException(
                            HttpStatus.CONFLICT,
                            "INVALID_MENU_ACTION",
                            "Invalid Menu Action"
                    );
                };
            } else {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "INVALID_ROLE_TO_APPROVE",
                        "Activity Should be Approved by Checker"
                );
            }
        } else {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "NO_NEED_TO_APPROVE",
                    "Status not showing a need to be approved"
            );
        }

    }

    private String createActivity(
            AdminBlockUnblockUserRequest request,
            String makerId,
            String status,
            boolean includeChecker,
            boolean includeApprover,
            String successMessage
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
                .updatedBy(makerId)
                .updatedTime(LocalDateTime.now())
                .build();

            trActivityRepository.save(activity);
            return newActId;
    };

    @Transactional
    private void updateActivity(
        String activityId,
        String newStatus,
        boolean updateChecker,
        boolean updateApprover,
        boolean isApprove,
        AdminProfile approverData
    ) {
        TrActivity activity = trActivityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.CONFLICT,
                        "INVALID_ACTIVITY_ID",
                        "Activity not found when updating tr activity table"
                ));

        activity.setStatus(newStatus);

        if (updateChecker) {
            activity.setUpdatedTimeChecker(LocalDateTime.now());
        }

        if (updateApprover) {
            activity.setUpdatedTimeApprover(LocalDateTime.now());
        }

        if (updateChecker && isApprove) {
            activity.setApproverId(approverData.getId());
        }

        activity.setUpdatedTime(LocalDateTime.now());

        trActivityRepository.save(activity);
    }

    private AdminActivityCreationResponse buildResponse(
            String actId,
            AdminBlockUnblockUserRequest request,
            LocalDateTime time,
            String message
    ) {
        return AdminActivityCreationResponse.builder()
                .activityId(actId)
                .message(message)
                .menuData(AdminActivityCreationResponse.MenuData.builder()
                        .menuId(request.getMenuData().getMenuId())
                        .menuName(request.getMenuData().getMenuName())
                        .menuAction(request.getMenuData().getMenuAction())
                        .actionFlow(request.getMenuData().getActionFlow())
                        .build())
                .createdTime(time.toString())
                .build();
    };

    private AdminApprovalResponse buildApprovalResponse(
            TrActivity trActivityData,
            String message
    ) {
        return AdminApprovalResponse.builder()
                .menudata(AdminApprovalResponse.MenuData.builder()
                        .menuId(trActivityData.getMenuId())
                        .menuAction(trActivityData.getActionMenu())
                        .actionFlow(trActivityData.getActionFlow())
                        .menuName(trActivityData.getMenuName())
                        .build())
                .updatedTime(trActivityData.getUpdatedTime().toString())
                .message(message)
                .build();
    }



}
