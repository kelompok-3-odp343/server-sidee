package com.example.wandoor.service;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.response.DplkListResponse;
import com.example.wandoor.repository.DplkAccountRepository;
import com.example.wandoor.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Log4j2
@RequiredArgsConstructor
public class DplkService {

    private final ProfileRepository profileRepository;
    private final DplkAccountRepository dplkAccountRepository;

    public DplkListResponse fetchDplkData(){
        var userId = RequestContext.get().getUserId();
        var cif = RequestContext.get().getCif();

        try {
            var userExists = profileRepository.findByIdAndCif(userId, cif)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "INVALID_USER", "User not found"));

            var getDplkAccount = dplkAccountRepository.findAllByUserIdAndCif(userId, cif);
            if (getDplkAccount.isEmpty()){
                throw new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "No DPLK accounts found");
            }

            var items = getDplkAccount.stream().map(
                    acc -> {
                        var initialDeposit = acc.getDplkInitialDeposit();
                        var growth = randomGrowth();
                        var years = calculateYearsSince(acc.getCreatedTime());
                        var accumulated = calculateAccumulated(initialDeposit, growth, years);

                        return  new DplkListResponse.Data.Items(
                                acc.getId(),
                                acc.getAccountNumberDplk(),
                                initialDeposit,
                                null,
                                acc.getCurrencyCode(),
                                growth,
                                accumulated
                        );
                    }).toList();

            var title = "apa ya title nya??";
            var totalBalance = items.stream()
                    .map(DplkListResponse.Data.Items::accumulatedBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            var fundData = new DplkListResponse.Data(
                    title,
                    totalBalance,
                    items
            );

            return new DplkListResponse(
                    "true",
                    "Pension funds fetch successfully",
                    List.of(fundData)
            );
        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while retrieving dplk data", e);
        }

    }

    private double randomGrowth(){
        return ThreadLocalRandom.current().nextDouble(0.04, 0.08);
    }

    private double calculateYearsSince(LocalDateTime createdAt){
        var days = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        return days / 365.0;
    }

    private BigDecimal calculateAccumulated(BigDecimal principal, double growth, double years){
        var result = principal.doubleValue() * Math.pow((1 + growth), years);
        return BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP);
    }

}

