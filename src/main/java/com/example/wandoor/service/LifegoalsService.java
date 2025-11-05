package com.example.wandoor.service;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.LifegoalsAccount;
import com.example.wandoor.model.request.LifegoalsDetailsRequest;
import com.example.wandoor.model.response.LifegoalsDetailsResponse;
import com.example.wandoor.model.response.LifegoalsGroupResponse;
import com.example.wandoor.model.response.LifegoalsListResponse;
import com.example.wandoor.model.response.LifegoalsResponse;
import com.example.wandoor.repository.LifegoalsAccountRepository;
import com.example.wandoor.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class LifegoalsService {

    private final ProfileRepository profileRepository;
    private final LifegoalsAccountRepository lifegoalsAccountRepository;

    private static final DateTimeFormatter RESP_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss:SSS'Z'");

    @Transactional(readOnly = true)
    public Map<String, LifegoalsGroupResponse> fetchAllLifegoals() {
        var userId = RequestContext.get().getUserId();
        var cif = RequestContext.get().getCif();

        try{
            // verify user
            var userExists = profileRepository.findByIdAndCif(userId, cif)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "INVALID_USER", "User not found"));


            var rows = lifegoalsAccountRepository.findByUserIdAndCif(userId, cif);
            if (rows == null || rows.isEmpty()) {
                return Collections.emptyMap();
            }

            Map<String, List<LifegoalsAccount>> byCategory = new LinkedHashMap<>();
            for (var a : rows) {
                String cat = Optional.ofNullable(a.getLifegoalsCategoryName()).orElse("Uncategorized");
                byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(a);
            }

            Map<String, LifegoalsGroupResponse> result = new LinkedHashMap<>();
            for (var entry : byCategory.entrySet()) {
                var category = entry.getKey();
                List<LifegoalsAccount> items = entry.getValue();

                // list items
                List<LifegoalsGroupResponse.Item> list = items.stream().map(a -> {
                    var id = a.getLifegoalsTrxCreationId() != null ? a.getLifegoalsTrxCreationId() : a.getId();
                    var created = formatCreated(a.getCreatedTime());
                    return new LifegoalsGroupResponse.Item(
                            id,
                            a.getAccountNumber(),
                            a.getLifegoalsName(),
                            a.getLifegoalsCategoryName(),
                            nz(a.getAccountTargetAmount()),
                            nz(a.getEstimationAmount()),
                            created
                    );
                }).toList();

                // total
                var totalTarget = list.stream()
                        .map(LifegoalsGroupResponse.Item::targetBalance)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                var currentBalance = list.stream()
                        .map(LifegoalsGroupResponse.Item::currentBalance)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                result.put(category, new LifegoalsGroupResponse(totalTarget, currentBalance, list));

            }
            return result;

        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while retrieving lifegoals data", e);
        }

    }

    public LifegoalsDetailsResponse fetchDetailLifegoals(LifegoalsDetailsRequest req){
        var userId = RequestContext.get().getUserId();
        var cif = RequestContext.get().getCif();

        try {
            // verify user
            var userExists = profileRepository.findByIdAndCif(userId, cif)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "INVALID_USER", "User not found"));

            // get detail by accountNumber
            var getData = lifegoalsAccountRepository.findByUserIdAndCifAndAccountNumber(userId, cif, req.accountNumber())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "Account Number lifegoals Not Found"));

            var maturityStr = getData.getMaturityDate() != null ? getData.getMaturityDate().format(RESP_FMT) : null;
            var createdStr = getData.getCreatedTime() != null ? getData.getCreatedTime().format(RESP_FMT) : null;
            BigDecimal interestRate = BigDecimal.valueOf(
                    2.5 + Math.random() * 3.5 // 2.5% sampai 6.0%
            ).setScale(2, RoundingMode.HALF_UP);

            return new LifegoalsDetailsResponse(
                    getData.getAccountNumber(),
                    nz(getData.getAccountTargetAmount()),
                    nz(getData.getAccountDeposit()),
                    interestRate,
                    maturityStr,
                    createdStr,
                    getData.getLifegoalsDuration() == null ? null : getData.getLifegoalsDuration().intValue(),
                    getData.getAccountNumber()
            );

        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while retrieving detail lifegoals data", e);
        }

    }
    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String formatCreated(Object createdTime) {
        if (createdTime == null) return null;
        if (createdTime instanceof LocalDateTime ldt) {
            return ldt.format(RESP_FMT);
        }
        return createdTime.toString();
    }
}
