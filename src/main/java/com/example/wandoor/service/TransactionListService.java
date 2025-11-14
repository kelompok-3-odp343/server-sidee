package com.example.wandoor.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.wandoor.model.entity.Account;
import com.example.wandoor.model.entity.Profile;
import com.example.wandoor.model.entity.TrxHistory;
import com.example.wandoor.model.entity.UserAuth;
import com.example.wandoor.model.enums.AccountStatus;
import com.example.wandoor.model.request.TransactionListRequest;
import com.example.wandoor.model.response.TransactionListResponse;
import com.example.wandoor.repository.AccountRepository;
import com.example.wandoor.repository.ProfileRepository;
import com.example.wandoor.repository.TrxHistoryRepository;
import com.example.wandoor.repository.UserAuthRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionListService {

    private final TrxHistoryRepository trxHistoryRepository;
    private final AccountRepository accountRepository;
    private final UserAuthRepository userAuthRepository;
    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public TransactionListResponse getTransactionList(TransactionListRequest request) {

        String userId = request.getUserId();
        log.info("🔍 Fetching transaction list for userId={}", userId);

        // 1️⃣ Ambil user
        UserAuth user = userAuthRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User dengan ID " + userId + " tidak ditemukan"));

        // 2️⃣ Ambil profile user (untuk nik + nama lengkap)
        Profile profile = profileRepository.findById(userId).orElse(null);

        String nik = (profile != null) ? profile.getNik() : "-";
        String customerName = (profile != null)
                ? buildFullName(profile)
                : user.getUsername();

        // 3️⃣ Ambil semua akun user (kecuali DORM & TUTUP)
        List<Account> accounts = accountRepository.findAll().stream()
                .filter(acc -> acc.getUserId().equals(userId))
                .filter(acc -> acc.getAccountStatus() != AccountStatus.DORM)
                .filter(acc -> acc.getAccountStatus() != AccountStatus.TUTUP)
                .toList();

        if (accounts.isEmpty()) {
            log.warn("⚠️ Tidak ditemukan akun aktif untuk userId={}", userId);
            return new TransactionListResponse(userId, nik, customerName, List.of());
        }

        // 4️⃣ Ambil semua transaksi untuk semua rekening aktif user
        List<TrxHistory> trxList = accounts.stream()
                .flatMap(acc -> trxHistoryRepository.findByAccountNumber(acc.getAccountNumber()).stream())
                .sorted(Comparator.comparing(TrxHistory::getTransactionDate).reversed())
                .toList();

        if (trxList.isEmpty()) {
            log.info("ℹ️ Tidak ada transaksi untuk userId={}", userId);
            return new TransactionListResponse(userId, nik, customerName, List.of());
        }

        // 5️⃣ Mapping transaksi → TransactionListResponse.TransactionItem
        List<TransactionListResponse.TransactionItem> trxResponses = trxList.stream()
                .map(trx -> {
                    Account acc = accounts.stream()
                            .filter(a -> a.getAccountNumber().equals(trx.getAccountNumber()))
                            .findFirst()
                            .orElse(null);

                    return new TransactionListResponse.TransactionItem(
                            trx.getId(),                                        // transactionId
                            trx.getAccountNumber(),                             // accountNumber
                            (acc != null) ? acc.getAccountType().name() : "-",  // productType
                            (acc != null) ? acc.getProductName() : "-",         // productTypeLabel
                            trx.getTransactionAmount(),                         // amount
                            "SUCCESS",                                          // status
                            trx.getTransactionDate(),                           // paymentTime
                            trx.getTransactionType()                            // transactionCategory
                    );
                })
                .toList();

        log.info("✅ {} transaksi ditemukan untuk userId={}", trxResponses.size(), userId);

        // 6️⃣ Return final response
        return new TransactionListResponse(
                userId,
                nik,
                customerName,
                trxResponses
        );
    }

    private String buildFullName(Profile profile) {
        return String.join(" ",
                Optional.ofNullable(profile.getFirstName()).orElse(""),
                Optional.ofNullable(profile.getMiddleName()).orElse(""),
                Optional.ofNullable(profile.getLastName()).orElse("")
        ).trim();
    }
}
