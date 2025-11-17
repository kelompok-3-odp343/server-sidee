// for service
package com.example.wandoor.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.response.TrxSummaryResponse;
import com.example.wandoor.repository.RoleManagementRepository;
import com.example.wandoor.repository.TrxSummaryRepository;
import com.example.wandoor.repository.UserAuthRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrxSummaryService {

    private final TrxSummaryRepository summaryRepository;
    private final UserAuthRepository userAuthRepository;
    private final RoleManagementRepository roleManagementRepository;

    public TrxSummaryResponse getOverview() {
        String adminUserId = getAdminUserId();
        var admin = userAuthRepository.findById(adminUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User admin tidak ditemukan"));

        var role = roleManagementRepository.findById(admin.getRoleId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND", "Role admin tidak ditemukan"));

        var roleName = role.getRoleName();
        if (!("MAKER".equalsIgnoreCase(roleName) || "CHECKER".equalsIgnoreCase(roleName) || "APPROVAL".equalsIgnoreCase(roleName))) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Anda bukan admin — akses ditolak");
        }

        double totalSaving = safe(summaryRepository.sumSaving());
        double totalTimeDeposit = safe(summaryRepository.sumTimeDeposit());
        double totalLifegoals = safe(summaryRepository.sumLifegoals());
        double totalPensionFund = safe(summaryRepository.sumPensionFund());
        double totalAsset = totalSaving + totalTimeDeposit + totalLifegoals + totalPensionFund;

        List<Object[]> catRows = summaryRepository.fetchCategoryTotalsForNasabah();
        List<TrxSummaryResponse.Category> categories = new ArrayList<>();
        double grand = catRows.stream().mapToDouble(r -> safe(r, 1)).sum();
        for (Object[] r : catRows) {
            TrxSummaryResponse.Category c = new TrxSummaryResponse.Category();
            c.setCategoryName(str(r, 0));
            double val = safe(r, 1);
            c.setTotal(val);
            c.setPercentage(grand == 0 ? 0 : (int) Math.round((val / grand) * 100));
            categories.add(c);
        }

        List<Object[]> userRows = summaryRepository.fetchAllNasabahUsers();
        List<TrxSummaryResponse.UserItem> users = new ArrayList<>();
        for (Object[] r : userRows) {
            TrxSummaryResponse.UserItem u = new TrxSummaryResponse.UserItem();
            u.setUserid(str(r, 0));
            u.setCustomerId(str(r, 1));
            u.setNik(str(r, 2));
            u.setCustomerName(str(r, 3));
            users.add(u);
        }

        TrxSummaryResponse res = new TrxSummaryResponse();
        res.setTotalSaving(totalSaving);
        res.setTotalTimeDeposit(totalTimeDeposit);
        res.setTotalLifegoals(totalLifegoals);
        res.setTotalPensionFund(totalPensionFund);
        res.setTotalAsset(totalAsset);
        res.setCategories(categories);
        res.setUsers(users);
        return res;
    }

    private String getAdminUserId() {
        String uid = RequestContext.get().getUserId();
        if (uid != null && !uid.isBlank()) return uid;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) return auth.getName();
        throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "User ID admin tidak ditemukan");
    }

    private double safe(Double d) {
        return d == null ? 0.0 : d;
    }

    private double safe(Object[] arr, int idx) {
        if (arr == null || arr.length <= idx || arr[idx] == null) return 0.0;
        try { return Double.parseDouble(arr[idx].toString()); } catch (Exception e) { return 0.0; }
    }

    private String str(Object[] arr, int idx) {
        if (arr == null || arr.length <= idx || arr[idx] == null) return "";
        return arr[idx].toString();
    }
}