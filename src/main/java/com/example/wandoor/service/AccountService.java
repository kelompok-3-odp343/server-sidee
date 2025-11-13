package com.example.wandoor.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.Account;
import com.example.wandoor.model.enums.ProductType;
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

    /**
     * Ambil data akun bertipe SVG (Savings) untuk user yang sedang login.
     * Body (AccountRequest) opsional.
     */
    public AccountResponse dataAccount(AccountRequest request) {
        String userId = RequestContext.get().getUserId();
        String cif = RequestContext.get().getCif();

        try {
            // Validasi user berdasarkan JWT context
            profileRepository.findByIdAndCif(userId, cif)
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.NOT_FOUND, "INVALID_USER", "User not found"));

            //Ambil semua akun
            List<Account> allAccounts = accountRepository.findByUserIdAndCif(userId, cif);

            //accountType = SVG
            List<Account> savingsAccounts = allAccounts.stream()
                    .filter(acc -> acc.getAccountType() == ProductType.SVG)
                    .collect(Collectors.toList());

            if (savingsAccounts.isEmpty()) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "No savings accounts (SVG) found for this user");
            }

            Account selectedAccount = resolveSelectedAccount(savingsAccounts, request);

            // object targetAccountDetail (hanya dari akun SVG)
            TargetAccountDetail targetAccountDetail = TargetAccountDetail.builder()
                    .accountNumber(selectedAccount.getAccountNumber())
                    .accountName(selectedAccount.getAccountHolderName())
                    .productName(selectedAccount.getProductName()) // dari Account.productName
                    .effectiveBalance(selectedAccount.getEffectiveBalance())
                    .isMainAccount(selectedAccount.getIsMainAccount() == 1)
                    .accountStatus(selectedAccount.getAccountStatus().name())
                    .build();

            //daftar akun SVG lainnya
            List<AccountListItem> otherAccounts = savingsAccounts.stream()
                    .filter(a -> !a.getAccountNumber().equals(selectedAccount.getAccountNumber()))
                    .map(a -> new AccountListItem(a.getAccountNumber()))
                    .collect(Collectors.toList());

            // Gabungkan hasil
            AccountData accountData = new AccountData(targetAccountDetail, otherAccounts);
            return new AccountResponse(accountData);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Unexpected error while retrieving account data", e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR",
                    "Something went wrong while retrieving account data", e);
        }
    }

    /**
     * Pilih akun berdasarkan body request (opsional):
     * - Jika request.accountNumber ada → tampilkan akun tersebut.
     * - Jika tidak ada → tampilkan akun utama (isMainAccount = 1).
     * - Jika tidak ada akun utama → pakai akun pertama di list.
     */
    private Account resolveSelectedAccount(List<Account> accounts, AccountRequest request) {
        if (request != null && request.getAccountNumber() != null && !request.getAccountNumber().isBlank()) {
            return accounts.stream()
                    .filter(a -> a.getAccountNumber().equals(request.getAccountNumber()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "Account number not found (SVG only)"));
        }

        return accounts.stream()
                .filter(a -> a.getIsMainAccount() != null && a.getIsMainAccount() == 1)
                .findFirst()
                .orElse(accounts.get(0));
    }
}
