package com.example.wandoor.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.wandoor.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.model.entity.Account;
import com.example.wandoor.model.request.AccountRequest;
import com.example.wandoor.model.response.AccountResponse;
import com.example.wandoor.model.response.AccountResponse.AccountData;
import com.example.wandoor.model.response.AccountResponse.AccountListItem;
import com.example.wandoor.model.response.AccountResponse.TargetAccountDetail;
import com.example.wandoor.repository.AccountRepository;
import com.example.wandoor.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class AccountService {

    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    
    public AccountResponse dataAccount(AccountRequest request) {
        var userId = RequestContext.get().getUserId();
        var cif = RequestContext.get().getCif();

        try {
            // Verifikasi user
            profileRepository.findByIdAndCif(userId, cif)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "INVALID_USER", "User not found"));

            // Ambil semua akun milik user
            List<Account> accounts = accountRepository.findByUserIdAndCif(userId, cif);
            if (accounts == null || accounts.isEmpty()) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND" ,"Data account not found");
            }

            // akun yang dipilih
            Account selectedAccount;
            if (request != null && request.getAccountNumber() != null && !request.getAccountNumber().isBlank()) {
                Optional<Account> accountOpt = accounts.stream()
                        .filter(a -> a.getAccountNumber().equals(request.getAccountNumber()))
                        .findFirst();

                if (accountOpt.isEmpty()) {
                    throw new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "Account number not found");
                }

                selectedAccount = accountOpt.get();
            } else {
                // Jika tidak ada input → ambil akun utama
                selectedAccount = accounts.stream()
                        .filter(a -> a.getIsMainAccount() != null && a.getIsMainAccount() == 1)
                        .findFirst()
                        .orElse(accounts.get(0));
            }

            //targetAccountDetail
            TargetAccountDetail targetAccountDetail = new TargetAccountDetail(

                    selectedAccount.getAccountNumber(),
                    selectedAccount.getAccountHolderName(),
                    selectedAccount.getProductName(),
                    selectedAccount.getEffectiveBalance(),
                    selectedAccount.getIsMainAccount() == 1,
                    selectedAccount.getAccountStatus().toString()
            );

            // daftar akun selain yang ditampilkan)
            List<AccountListItem> otherAccounts = accounts.stream()
                    .filter(a -> !a.getAccountNumber().equals(selectedAccount.getAccountNumber()))
                    .map(a -> new AccountListItem(a.getAccountNumber()))
                    .collect(Collectors.toList());

            // AccountData & AccountResponse
            AccountData accountData = new AccountData(targetAccountDetail, otherAccounts.isEmpty() ? null : otherAccounts);
            return new AccountResponse(accountData);

        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while retrieving account data", e);
        }
    }
}