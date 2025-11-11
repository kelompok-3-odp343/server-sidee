package com.example.wandoor.service;

import java.util.List;

import com.example.wandoor.exception.BusinessException;
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

            var targetAccount = (request.accountNumber() != null && !request.accountNumber().isBlank())
                    ? accountList.stream().filter(a -> a.getAccountNumber().equals(request.accountNumber()))
                        .findFirst().orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND" ,"Account not found"))
                    : accountList.stream().filter(a -> a.getIsMainAccount() == 1).findFirst()
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND" , "Main account not found"));

            var month = request.month();
            var year = request.year();
            List<TrxHistory> trxList = transactionHistoryRepository.findByUserIdAndAccountNumberAndMonthYear(
                    userId, targetAccount.getAccountNumber(), month, year);

            List<TrxResponse> trxResponse = trxList.stream()
                    .map(t -> TrxResponseBuilder.builder()
                            .transactionId(t.getId())
                            .transactionDate(t.getTransactionDate())
                            .transactionType(t.getTransactionType())
                            .debitCredit(t.getDebitCredit().name())
                            .partyName(t.getPartyName())
                            .partyDetail(t.getPartyDetail())
                            .amount(t.getTransactionAmount())
                            .build()).toList();

            return TransactionHistoryResponseBuilder.builder()
                    .month(request.month())
                    .year(String.valueOf(request.year()))
                    .productType(targetAccount.getAccountType().name())
                    .productSubCategory(targetAccount.getSubCat())
                    .transaction(trxResponse)
                    .build();
        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while retrieving transaction history data", e);
        }
    }
}