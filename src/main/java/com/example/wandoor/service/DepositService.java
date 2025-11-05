package com.example.wandoor.service;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.TimeDepositAccount;
import com.example.wandoor.model.response.DepositResponse;
import com.example.wandoor.repository.DepositRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class DepositService {
    private final DepositRepository depositRepository;
    
    public DepositResponse fetchDeposit(){
        var userId = RequestContext.get().getUserId();
        var cif = RequestContext.get().getCif();

        try {
            List<TimeDepositAccount> deposits = depositRepository.findByUserIdAndCif(userId, cif);
            if (deposits.isEmpty()) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "Data deposit not found");
            }

            var fund_id = cif;
            var title = "Time Deposits";
            // ✅ Hitung total balance dan jumlah akun
            BigDecimal totalBalance = deposits.stream()
                    .map(td -> td.getEffectiveBalance() == null ? BigDecimal.ZERO : td.getEffectiveBalance())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            var countAccounts = deposits.size();

            // ✅ Mapping entity → response item
            List<DepositResponse.Items> items = new ArrayList<>();
            for (TimeDepositAccount d : deposits) {
                items.add(new DepositResponse.Items(
                        d.getId(),
                        d.getDepositAccountNumber(),
                        d.getEffectiveBalance() == null ? BigDecimal.ZERO : d.getEffectiveBalance(),
                        d.getTenorMonths(),
                        d.getMaturityDate() == null
                                ? null
                                : d.getMaturityDate().atOffset(ZoneOffset.UTC).toString(),
                        d.getInterestRate(),
                        d.getDepositAccountStatus()
                ));
            }

            // ✅ Bungkus ke dalam DepositData
            DepositResponse.DepositData data = new DepositResponse.DepositData(
                    fund_id,
                    title,
                    totalBalance,
                    countAccounts,
                    items
            );

            // ✅ Return response lengkap
            return new DepositResponse(
                    true,
                    "Time deposits fetched successfully",
                    data
            );
        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while retrieving deposit data", e);
        }
    }
}