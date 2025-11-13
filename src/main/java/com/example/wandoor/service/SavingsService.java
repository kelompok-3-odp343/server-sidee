package com.example.wandoor.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.model.entity.Account;
import com.example.wandoor.model.enums.ProductType;
import com.example.wandoor.model.response.SavingsResponse;
import com.example.wandoor.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingsService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getSavingsForLoggedInUser() {
        RequestContext ctx = RequestContext.get();
        String userId = ctx.getUserId();
        String cif = ctx.getCif();

        if (userId == null || cif == null) {
            throw new IllegalStateException("User ID atau CIF tidak ditemukan (JWT invalid)");
        }

        // 🔹 Ambil semua akun user dan filter hanya yang bertipe SVG (savings)
        List<Account> savingsAccounts = accountRepository.findByUserIdAndCif(userId, cif)
                .stream()
                .filter(acc -> acc.getAccountType() == ProductType.SVG)
                .collect(Collectors.toList());

        if (savingsAccounts.isEmpty()) {
            return Map.of("data", new SavingsResponse(null, List.of()));
        }

        // 🔹 Hitung total saldo semua akun savings (SVG saja)
        BigDecimal totalEffectiveBalance = savingsAccounts.stream()
                .map(Account::getEffectiveBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 🔹 Buat daftar akun savings dengan detail lengkap
        List<SavingsResponse.AccountListItem> accountList = savingsAccounts.stream()
                .map(acc -> SavingsResponse.AccountListItem.builder()
                        .accountNumber(acc.getAccountNumber())
                        .accountName(acc.getAccountHolderName())
                        .productName(acc.getProductName()) // dari entity Account
                        .effectiveBalanceTotal(acc.getEffectiveBalance())
                        .isMainAccount(acc.getIsMainAccount() == 1)
                        .accountStatus(acc.getAccountStatus().name())
                        .build())
                .collect(Collectors.toList());

        // 🔹 Bungkus total saldo dalam objek targetAccountDetail
        SavingsResponse.TargetAccountDetail targetAccountDetail =
                SavingsResponse.TargetAccountDetail.builder()
                        .totalEffectiveBalance(totalEffectiveBalance)
                        .build();

        // 🔹 Bentuk response akhir
        SavingsResponse response = SavingsResponse.builder()
                .targetAccountDetail(targetAccountDetail)
                .accountList(accountList)
                .build();

        return Map.of("data", response);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSavingsDetail(String accountNumber) {
        RequestContext ctx = RequestContext.get();
        String userId = ctx.getUserId();
        String cif = ctx.getCif();

        Optional<Account> accountOpt = accountRepository
                .findByUserIdAndCifAndAccountNumber(userId, cif, accountNumber);

        if (accountOpt.isEmpty()) {
            return Map.of("message", "Account not found or not owned by user");
        }

        Account acc = accountOpt.get();

        // 🔹 Validasi bahwa account ini bertipe SVG
        if (acc.getAccountType() != ProductType.SVG) {
            return Map.of("message", "Account is not a savings (SVG) account");
        }

        var detail = SavingsResponse.AccountListItem.builder()
                .accountNumber(acc.getAccountNumber())
                .accountName(acc.getAccountHolderName())
                .productName(acc.getProductName())
                .effectiveBalanceTotal(acc.getEffectiveBalance())
                .isMainAccount(acc.getIsMainAccount() == 1)
                .accountStatus(acc.getAccountStatus().name())
                .build();

        return Map.of("data", detail);
    }
}
