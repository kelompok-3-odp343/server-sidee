package com.example.wandoor.service;

import com.example.wandoor.model.request.DetailUserAdminRequest;
import com.example.wandoor.model.response.DetailUserAdminResponse;
import com.example.wandoor.repository.DetailUserAdminRepository;
import com.example.wandoor.repository.RoleManagementRepository;
import com.example.wandoor.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public DetailUserAdminResponse getUserDetail(DetailUserAdminRequest request) {

        // ✅ Ambil user admin dari JWT (bukan dari header / RequestContext)
        String adminUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (adminUserId == null || adminUserId.isBlank()) {
            throw new RuntimeException("User admin tidak ditemukan dari token JWT");
        }

        var admin = userAuthRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("User admin tidak ditemukan"));

        var role = roleManagementRepository.findById(admin.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role admin tidak ditemukan"));

        if (!"ADMIN".equalsIgnoreCase(role.getRoleName())) {
            throw new RuntimeException("Anda bukan admin — akses ditolak");
        }

        // ✅ Ambil userId nasabah dari body request
        String targetUserId = request.getTargetUserId();
        if (targetUserId == null || targetUserId.isBlank()) {
            throw new RuntimeException("User ID nasabah wajib diisi di body request");
        }

        // ✅ Eksekusi query detail nasabah
        List<Object[]> results = detailUserAdminRepository.findNasabahDetailByUserId(targetUserId);
        if (results.isEmpty()) {
            throw new RuntimeException("Data user tidak ditemukan atau bukan nasabah");
        }

        return buildResponse(results);
    }

    // ------------------ Helper ---------------------

    private DetailUserAdminResponse buildResponse(List<Object[]> results) {
        Object[] base = results.get(0);

        DetailUserAdminResponse response = new DetailUserAdminResponse();
        response.setUserId(toString(base[0]));
        response.setBlocked(parseBoolean(base[1]));
        response.setCustomerName(buildFullName(base[2], base[3], base[4]));
        response.setCustomerId(toString(base[10]));

        List<DetailUserAdminResponse.AccountDetail> accounts = new ArrayList<>();
        for (Object[] row : results) {
            accounts.add(new DetailUserAdminResponse.AccountDetail(
                    toString(row[5]),
                    toString(row[6]),
                    toString(row[7]),
                    toString(row[8]),
                    toDouble(row[9])
            ));
        }
        response.setAccounts(accounts);

        return response;
    }

    private String buildFullName(Object f, Object m, Object l) {
        StringBuilder sb = new StringBuilder();
        if (f != null) sb.append(f);
        if (m != null && !m.toString().isBlank()) sb.append(" ").append(m);
        if (l != null) sb.append(" ").append(l);
        return sb.toString().trim();
    }

    private String toString(Object o) { return o == null ? "" : o.toString(); }

    private boolean parseBoolean(Object o) {
        if (o == null) return false;
        var v = o.toString().trim();
        return v.equals("1") || v.equalsIgnoreCase("true") || v.equalsIgnoreCase("Y");
    }

    private double toDouble(Object o) {
        try { return o == null ? 0 : Double.parseDouble(o.toString()); }
        catch (Exception e) { return 0; }
    }
}
