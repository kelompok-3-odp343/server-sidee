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
import com.example.wandoor.repository.ProfileRepository;
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
    private final ProfileRepository profileRepository;

    public TrxSummaryResponse getOverview() {
        try {
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

            List<String> catNames = summaryRepository.fetchAllCategoryNamesForNasabah();
            List<TrxSummaryResponse.Category> categories = new ArrayList<>();
            double grand = safe(summaryRepository.sumAllTransactionAmountForNasabah());
            int limit = Math.min(3, catNames.size());
            for (int i = 0; i < limit; i++) {
                String name = catNames.get(i);
                double val = safe(summaryRepository.sumCategoryAmountForNasabah(name));
                TrxSummaryResponse.Category c = new TrxSummaryResponse.Category();
                c.setCategoryName(name);
                c.setTotal(val);
                c.setPercentage(grand == 0 ? 0 : (int) Math.round((val / grand) * 100));
                categories.add(c);
            }

            List<String> userIds = summaryRepository.fetchAllNasabahUserIds();
            List<TrxSummaryResponse.UserItem> users = new ArrayList<>();
            for (String uid : userIds) {
                var p = profileRepository.findById(uid).orElse(null);
                TrxSummaryResponse.UserItem u = new TrxSummaryResponse.UserItem();
                u.setUserid(uid);
                u.setCustomerId(p != null ? p.getCif() : "");
                u.setNik(p != null ? p.getNik() : "");
                String mid = p != null && p.getMiddleName() != null ? p.getMiddleName() : "";
                String name = p != null ? (p.getFirstName() + " " + mid + " " + p.getLastName()) : "";
                u.setCustomerName(name);
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
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while fetching transaction summary", e);
        }
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
}