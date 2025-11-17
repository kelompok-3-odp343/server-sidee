package com.example.wandoor.service;

import java.util.List;
import java.util.stream.Collectors;

import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.Account;
import com.example.wandoor.model.response.*;
import com.example.wandoor.repository.TrxCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.model.entity.TrxHistory;
import com.example.wandoor.model.enums.AccountStatus;
import com.example.wandoor.model.request.TransactionHistoryRequest;
import com.example.wandoor.repository.AccountRepository;
import com.example.wandoor.repository.ProfileRepository;
import com.example.wandoor.repository.TrxHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static java.util.stream.Collectors.toList;


@Service
@Log4j2
@RequiredArgsConstructor
public class TransactionHistoryService {

    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    private final TrxHistoryRepository transactionHistoryRepository;
    private final TrxCategoryRepository trxCategoryRepository;

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
                    .filter(a -> {
                        if ("SAV".equalsIgnoreCase(productType) || "LFG".equalsIgnoreCase(productType)) {
                            return a.getAccountNumber().equals(request.accountNumber());
                        }
                        return true;
                    })
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
                            .accountNumber(t.getAccountNumber())
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

    public DetailTrxResponse fetchTransactionDetail(String transactionId){
        var userId = RequestContext.get().getUserId();
        var cif = RequestContext.get().getCif();

        try {
            var trx = transactionHistoryRepository.findById(transactionId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "Transaction Not  Found"));

            var userExists = profileRepository.findByIdAndCif(userId, cif)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "User Not Found"));

            var trxCategpry = trxCategoryRepository.findById(trx.getCategoryId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "Category Not Found"));

            return DetailTrxResponse.builder()
                    .transactionId(trx.getId())
                    .accountNumber(trx.getAccountNumber())
                    .transactionDate(trx.getTransactionDate())
                    .paymentMethod(trx.getPaymentMethod())
                    .transactionCategory(trxCategpry.getCategoryName())
                    .partyName(trx.getPartyName())
                    .partyDetail(trx.getPartyDetail())
                    .amount(trx.getTransactionAmount())
                    .debitCredit(trx.getDebitCredit().name())
                    .build();
        } catch (BusinessException e){
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching transaction detail", e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR",
                    "Something went wrong while retrieving transaction detail", e);
        }
    }
}