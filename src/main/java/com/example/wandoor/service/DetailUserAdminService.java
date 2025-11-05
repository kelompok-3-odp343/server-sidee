package com.example.wandoor.service;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.model.request.DetailUserAdminRequest;
import com.example.wandoor.model.response.DetailUserAdminResponse;
import com.example.wandoor.repository.DetailUserAdminRepository;
import com.example.wandoor.repository.RoleManagementRepository;
import com.example.wandoor.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetailUserAdminService {

    private final DetailUserAdminRepository detailUserAdminRepository;
    private final UserAuthRepository userAuthRepository;
    private final RoleManagementRepository roleManagementRepository;
    private final RequestContext requestContext;

    public DetailUserAdminResponse getUserDetail(DetailUserAdminRequest request) {
        // 🧩 1️⃣ Validasi admin berdasarkan header (RequestContext)
        String adminUserId = requestContext.getUserId();

        if (adminUserId == null || adminUserId.isBlank()) {
            throw new RuntimeException("User ID admin tidak ditemukan dalam header");
        }

        var admin = userAuthRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("User admin tidak ditemukan"));

        var adminRole = roleManagementRepository.findById(admin.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role admin tidak ditemukan"));

        if (!"ADMIN".equalsIgnoreCase(adminRole.getRoleName())) {
            throw new RuntimeException("Anda bukan admin — akses ditolak");
        }

        // 🧩 2️⃣ Validasi input nasabah dari body
        // 🧩 3️⃣ Validasi body request (user nasabah)
        String targetUserId = request.getTargetUserId();
        if (targetUserId == null || targetUserId.isBlank()) {
            throw new RuntimeException("User ID nasabah wajib diisi di body request");
        }


        // 🧩 3️⃣ Ambil detail nasabah
        List<Object[]> results = detailUserAdminRepository.findNasabahDetailByUserId(targetUserId);

        if (results.isEmpty()) {
            throw new RuntimeException("Data user tidak ditemukan atau bukan nasabah");
        }

        // 🧩 4️⃣ Bangun response akhir
        return buildUserDetailResponse(results);
    }

    // --------------------- Helper Methods ---------------------

    private DetailUserAdminResponse buildUserDetailResponse(List<Object[]> results) {
        Object[] firstRow = results.get(0);

        String userId = toStringSafe(firstRow[0]);
        boolean isBlocked = parseBoolean(firstRow[1]);
        String fullName = buildFullName(firstRow[2], firstRow[3], firstRow[4]);
        String customerId = toStringSafe(firstRow[10]);

        List<DetailUserAdminResponse.AccountDetail> accountList = new ArrayList<>();
        for (Object[] row : results) {
            accountList.add(buildAccountDetail(row));
        }

        DetailUserAdminResponse response = new DetailUserAdminResponse();
        response.setUserId(userId);
        response.setCustomerId(customerId);
        response.setCustomerName(fullName);
        response.setBlocked(isBlocked);
        response.setAccounts(accountList);
        return response;
    }

    private DetailUserAdminResponse.AccountDetail buildAccountDetail(Object[] row) {
        DetailUserAdminResponse.AccountDetail account = new DetailUserAdminResponse.AccountDetail();
        account.setAccountNumber(toStringSafe(row[5]));
        account.setProductType(toStringSafe(row[6]));
        account.setProductName(toStringSafe(row[7]));
        account.setAccountStatus(toStringSafe(row[8]));
        account.setEffectiveBalance(parseDouble(row[9]));
        return account;
    }

    private String buildFullName(Object first, Object middle, Object last) {
        StringBuilder name = new StringBuilder();
        if (first != null) name.append(first);
        if (middle != null && !middle.toString().isBlank()) name.append(" ").append(middle);
        if (last != null) name.append(" ").append(last);
        return name.toString().trim();
    }

    private String toStringSafe(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    private boolean parseBoolean(Object obj) {
        if (obj == null) return false;
        String val = obj.toString().trim();
        return val.equals("1") || val.equalsIgnoreCase("true") || val.equalsIgnoreCase("Y");
    }

    private double parseDouble(Object obj) {
        try {
            return obj == null ? 0.0 : Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
