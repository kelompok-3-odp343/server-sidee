package com.example.wandoor.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.wandoor.model.entity.Account;
import com.example.wandoor.model.entity.Profile;
import com.example.wandoor.model.entity.TrxHistory;
import com.example.wandoor.model.enums.AccountStatus;
import com.example.wandoor.model.request.AdminTransactionListRequest;
import com.example.wandoor.model.response.AdminTransactionListResponse;
import com.example.wandoor.repository.AccountRepository;
import com.example.wandoor.repository.ProfileRepository;
import com.example.wandoor.repository.TrxHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminTransactionListService {

    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    private final TrxHistoryRepository trxHistoryRepository;

    public AdminTransactionListResponse getTransactionList(AdminTransactionListRequest request) {

        // ✅ Ambil profile user
        Profile profile = profileRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        String userId = profile.getId();
        String cif = profile.getCif(); // tetap dipakai untuk query repository yang sudah ada

        // ✅ Ambil semua rekening user (menggunakan method yang SUDAH ADA)
        List<Account> accounts = accountRepository.findByUserIdAndCif(userId, cif);

        // ✅ Filter rekening aktif saja (BUKA dan BARU)
        List<Account> activeAccounts = accounts.stream()
                .filter(a -> a.getAccountStatus() == AccountStatus.BUKA
                        || a.getAccountStatus() == AccountStatus.BARU)
                .toList();

        // ✅ Jika tidak ada rekening aktif → kembalikan response kosong
        if (activeAccounts.isEmpty()) {
            return AdminTransactionListResponse.builder()
                    .customerId(profile.getId())
                    .nik(profile.getNik())
                    .customerName(buildFullName(profile))
                    .transactionHistories(List.of())
                    .build();
        }

        // ✅ Ambil nomor rekening
        List<String> activeAccountNumbers = activeAccounts.stream()
                .map(Account::getAccountNumber)
                .toList();

        // ✅ Ambil & mapping transaksi
        List<AdminTransactionListResponse.TransactionData> trxList = activeAccountNumbers.stream()
                .flatMap(accNo -> trxHistoryRepository.findByAccountNumber(accNo).stream())
                .sorted(Comparator.comparing(TrxHistory::getTransactionDate).reversed())
                .map(t -> {
                    Account acc = activeAccounts.stream()
                            .filter(a -> a.getAccountNumber().equals(t.getAccountNumber()))
                            .findFirst()
                            .orElse(null);

                    return AdminTransactionListResponse.TransactionData.builder()
                            .transactionId(t.getId())
                            .accountNumber(t.getAccountNumber())
                            .productType(acc != null ? acc.getAccountType().name() : null) // ✅ Enum → String
                            .productTypeLabel(acc != null ? acc.getProductName() : null)
                            .transactionAmount(t.getTransactionAmount().doubleValue())
                            .status(t.getDebitCredit().name()) // ✅ "DEBIT" atau "CREDIT"
                            .paymentTime(t.getTransactionDate().toString()) // sementara → nanti bisa format kalau mau
                            .transactionCategory(t.getTransactionType())
                            .build();
                })
                .toList();

        return AdminTransactionListResponse.builder()
                .customerId(profile.getId())
                .nik(profile.getNik())
                .customerName(buildFullName(profile))
                .transactionHistories(trxList)
                .build();
    }

    private String buildFullName(Profile profile) {
        return (profile.getFirstName() + " " +
                (profile.getMiddleName() != null ? profile.getMiddleName() + " " : "") +
                profile.getLastName()).trim();
    }
}
