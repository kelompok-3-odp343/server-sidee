package com.example.wandoor.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Collections;
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
    public SavingsResponse getSavingsForLoggedInUser() {
        RequestContext ctx = RequestContext.get();
        String userId = ctx.getUserId();
        String cif = ctx.getCif();

        if (userId == null || cif == null) {
            throw new IllegalStateException("User ID atau CIF tidak ditemukan (JWT invalid)");
        }

        List<Account> savingsAccounts = Optional.ofNullable(accountRepository.findByUserIdAndCif(userId, cif))
                .orElse(Collections.emptyList())
                .stream()
                .filter(acc -> acc.getAccountType() == ProductType.SVG || acc.getAccountType() == ProductType.SAV)
                .collect(Collectors.toList());

        BigDecimal totalEffectiveBalance = savingsAccounts.stream()
                .map(a -> a.getEffectiveBalance() == null ? BigDecimal.ZERO : a.getEffectiveBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SavingsResponse.AccountListItem> accountList = savingsAccounts.stream()
                .map(acc -> new SavingsResponse.AccountListItem(
                        acc.getAccountNumber(),
                        acc.getAccountHolderName(),
                        acc.getProductName(),
                        acc.getEffectiveBalance() == null ? BigDecimal.ZERO : acc.getEffectiveBalance(),
                        acc.getIsMainAccount() != null && acc.getIsMainAccount() == 1,
                        acc.getAccountStatus() == null ? null : acc.getAccountStatus().name()
                ))
                .collect(Collectors.toList());

        SavingsResponse.TargetAccountDetail target = new SavingsResponse.TargetAccountDetail(totalEffectiveBalance);
        return new SavingsResponse(target, accountList.isEmpty() ? null : accountList);
    }

    @Transactional(readOnly = true)
    public SavingsResponse.AccountListItem getSavingsDetail(String accountNumber) {
        RequestContext ctx = RequestContext.get();
        String userId = ctx.getUserId();
        String cif = ctx.getCif();

        Optional<Account> accountOpt = accountRepository.findByUserIdAndCifAndAccountNumber(userId, cif, accountNumber);
        if (accountOpt.isEmpty()) {
            throw new IllegalStateException("Account not found or not owned by user");
        }

        Account acc = accountOpt.get();
        if (acc.getAccountType() != ProductType.SVG && acc.getAccountType() != ProductType.SAV) {
            throw new IllegalStateException("Account is not a savings account");
        }

        return new SavingsResponse.AccountListItem(
                acc.getAccountNumber(),
                acc.getAccountHolderName(),
                acc.getProductName(),
                acc.getEffectiveBalance() == null ? BigDecimal.ZERO : acc.getEffectiveBalance(),
                acc.getIsMainAccount() != null && acc.getIsMainAccount() == 1,
                acc.getAccountStatus() == null ? null : acc.getAccountStatus().name()
        );
    }
}
