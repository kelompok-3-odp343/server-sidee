package com.example.wandoor.service;

import java.util.List;
import java.util.stream.Collectors;

import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.Account;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.model.entity.TrxHistory;
import com.example.wandoor.model.enums.AccountStatus;
import com.example.wandoor.model.request.TransactionHistoryRequest;
import com.example.wandoor.model.response.TransactionHistoryResponse;
import com.example.wandoor.model.response.TransactionHistoryResponseBuilder;
import com.example.wandoor.model.response.TrxResponse;
import com.example.wandoor.model.response.TrxResponseBuilder;
import com.example.wandoor.repository.AccountRepository;
import com.example.wandoor.repository.ProfileRepository;
import com.example.wandoor.repository.TrxHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Service
@Log4j2
@RequiredArgsConstructor
public class TransactionHistoryService {

    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    private final TrxHistoryRepository transactionHistoryRepository;

    public TransactionHistoryResponse fetchTransactionHistory(TransactionHistoryRequest request){
        var userId = RequestContext.get().getUserId();
        var cif = RequestContext.get().getCif();
//        log.info("DEBUG: Incoming request = {}", request);

        try {
            var userExists = profileRepository.findByIdAndCif(userId, cif)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "User Not Found"));

            var accountList = accountRepository.fetchActiveAccounts(userId, cif, List.of(AccountStatus.BUKA, AccountStatus.BARU));
            if (accountList.isEmpty()) throw new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND" ,"Account not found");

            var productType = request.productType().toUpperCase();

            var targetAccount = accountList.stream()
                    .filter(a -> a.getAccountType().name().equalsIgnoreCase(productType))
                    .filter(a -> !"SAV".equalsIgnoreCase(productType) || a.getAccountNumber().equals(request.accountNumber()))
                    .toList();

            if (targetAccount.isEmpty()) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND",
                        "No accounts found for product type " + productType);
            }

            var month = request.month();
            var year = request.year();
            List<TrxHistory> trxList = targetAccount.stream()
                    .flatMap(acc -> transactionHistoryRepository
                            .findByUserIdAndAccountNumberAndMonthYear(userId, acc.getAccountNumber(), month, year)
                            .stream())
                    .toList();

            var accountSubCatMap = targetAccount.stream()
                    .collect(Collectors.toMap(Account::getAccountNumber, Account::getSubCat));

            log.info("fetchTransactionHistory request -> size={}, userId={}, cif={}, productType={}, accountNumber={}, month={}, year={}",
                    trxList.size(), userId, cif, request.productType(), request.accountNumber(), request.month(), request.year());

            var trxResponse = trxList.stream()
                    .map(t -> TrxResponseBuilder.builder()
                            .transactionId(t.getId())
                            .accountnNumber(t.getAccountNumber())
                            .transactionDate(t.getTransactionDate())
                            .transactionType(t.getTransactionType())
                            .debitCredit(t.getDebitCredit().name())
                            .productSubCategory(accountSubCatMap.get(t.getAccountNumber()))
                            .partyName(t.getPartyName())
                            .partyDetail(t.getPartyDetail())
                            .amount(t.getTransactionAmount())
                            .build()).toList();

            return TransactionHistoryResponseBuilder.builder()
                    .month(request.month())
                    .year(String.valueOf(request.year()))
                    .productType(targetAccount.get(0).getAccountType().name())
                    .transaction(trxResponse)
                    .build();
        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while retrieving transaction history data", e);
        }
    }
}