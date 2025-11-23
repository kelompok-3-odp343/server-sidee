package com.example.wandoor.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.Account;
import com.example.wandoor.model.entity.TrxHistory;
import com.example.wandoor.model.enums.ProductType;
import com.example.wandoor.model.enums.DebitCredit;
import com.example.wandoor.model.enums.AccountStatus;
import com.example.wandoor.model.response.SavingDetailResponse;
import com.example.wandoor.model.response.SavingDetailResponse.BiggestIncoming;
import com.example.wandoor.model.response.SavingDetailResponse.CategoryBreakdown;
import com.example.wandoor.model.response.SavingDetailResponse.Insights;
import com.example.wandoor.model.response.SavingDetailResponse.Meta;
import com.example.wandoor.model.response.SavingDetailResponse.Summary;
import com.example.wandoor.model.response.SavingDetailResponse.TopCategory;
import com.example.wandoor.repository.AccountRepository;
import com.example.wandoor.repository.ProfileRepository;
import com.example.wandoor.repository.TrxHistoryRepository;
import com.example.wandoor.repository.TrxCategoryRepository;
import com.example.wandoor.model.entity.TrxCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class SavingDetailService {

    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    private final TrxHistoryRepository trxHistoryRepository;
    private final TrxCategoryRepository trxCategoryRepository;

    

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Transactional(readOnly = true)
    public SavingDetailResponse getSavingsDetail(String month, String accountNumber) {

        java.time.YearMonth ym = ymFor(month);
        Account selectedAccount = null;
        try {
            log.info("getSavingsDetail request: month={}, accountHeader={}", ym, accountNumber);
            String userId = RequestContext.get().getUserId();
            String cif = RequestContext.get().getCif();
            if (userId == null || userId.isBlank()) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "User ID tidak ditemukan (JWT invalid)");
            }
            if (cif == null || cif.isBlank()) {
                cif = profileRepository.findById(userId)
                        .map(p -> p.getCif())
                        .orElse(null);
            }
            if (cif == null || cif.isBlank()) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "CIF_NOT_FOUND", "CIF tidak ditemukan di token maupun profil");
            }
            profileRepository.findByIdAndCif(userId, cif)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND" ,  "User not found"));

            List<Account> accounts = accountRepository.fetchActiveAccounts(userId, cif, java.util.List.of(AccountStatus.BUKA, AccountStatus.BARU))
                    .stream()
                    .filter(a -> a.getAccountType() == ProductType.SVG || a.getAccountType() == ProductType.SAV)
                    .filter(a -> a.getAccountStatus() == AccountStatus.BUKA || a.getAccountStatus() == AccountStatus.BARU)
                    .collect(Collectors.toList());
            if (accounts.isEmpty()) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND" ,"No savings account found");
            }

            selectedAccount = selectTargetAccount(accountNumber, accounts);
            log.info("getSavingsDetail selectedAccount: {}", selectedAccount.getAccountNumber());

            int targetMonth = ym.getMonthValue();
            int targetYear = ym.getYear();
            List<TrxHistory> monthList = (accountNumber == null || accountNumber.isBlank())
                    ? accounts.stream()
                        .flatMap(acc -> trxHistoryRepository
                                .findByUserIdAndAccountNumberAndMonthYear(userId, acc.getAccountNumber(), targetMonth, targetYear)
                                .stream())
                        .collect(Collectors.toList())
                    : trxHistoryRepository
                        .findByUserIdAndAccountNumberAndMonthYear(userId, selectedAccount.getAccountNumber(), targetMonth, targetYear);
            log.info("getSavingsDetail monthList size: {} for month={}, year={}", monthList.size(), targetMonth, targetYear);

            if (monthList.isEmpty()) {
                return buildEmptyResponse(selectedAccount, ym);
            }

            Summary summary = calculateSummary(monthList);

            Map<String, String> categoryMap;
            try {
                categoryMap = trxCategoryRepository.findAll().stream()
                        .filter(c -> c.getId() != null && !c.getId().isBlank())
                        .collect(Collectors.toMap(
                                TrxCategory::getId,
                                c -> {
                                    String name = c.getCategoryName();
                                    return name == null ? "" : name;
                                },
                                (v1, v2) -> v1
                        ));
            } catch (Exception ex) {
                categoryMap = new HashMap<>();
            }

            Insights insights = calculateInsights(selectedAccount.getAccountNumber(), monthList, categoryMap);

            List<CategoryBreakdown> breakdown = calculateCategoryBreakdown(selectedAccount.getAccountNumber(), monthList, categoryMap);

            Meta meta = buildMeta(monthList, selectedAccount, ym);

            return new SavingDetailResponse(meta, summary, insights, breakdown);
        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            log.error("Unexpected error in getSavingsDetail", e);
            String currency = selectedAccount != null ? Optional.ofNullable(selectedAccount.getCurrencyCode()).orElse("IDR") : "IDR";
            Meta meta = new Meta(ym.toString(), currency);
            Summary summary = new Summary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            return new SavingDetailResponse(meta, summary, null, null);
        }

    }

    private Account selectTargetAccount(String accountNumber, List<Account> accounts) {
        if (accountNumber != null && !accountNumber.isBlank()) {
            return accounts.stream()
                    .filter(a -> a.getAccountNumber().equals(accountNumber))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "Account number not found for user"));
        }
        return accounts.stream()
                .filter(a -> a.getIsMainAccount() != null && a.getIsMainAccount() == 1)
                .findFirst()
                .orElse(accounts.get(0));
    }
    private Summary calculateSummary(List<TrxHistory> trxList) {
        BigDecimal totalDebit = sumByDebitCredit(trxList, DebitCredit.D);
        BigDecimal totalCredit = sumByDebitCredit(trxList, DebitCredit.C);
        BigDecimal netGrowth = totalCredit.subtract(totalDebit);
        return new Summary(totalDebit, totalCredit, netGrowth);
    }
    private BigDecimal sumByDebitCredit(List<TrxHistory> trxList, DebitCredit dc) {
        return trxList.stream()
                .filter(t -> t.getDebitCredit() == dc)
                .map(t -> t.getTransactionAmount() == null ? BigDecimal.ZERO : t.getTransactionAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private Insights calculateInsights(String accountNumber, List<TrxHistory> trxList, Map<String, String> categoryMap) {
        try {
        Map<String, BigDecimal> sumByCategory = trxList.stream()
                .filter(t -> t.getDebitCredit() == DebitCredit.D)
                .collect(Collectors.groupingBy(
                        t -> {
                            String key = t.getCategoryId();
                            String name = key == null ? null : categoryMap.getOrDefault(key, key);
                            return (name == null || name.isBlank()) ? "Others" : name;
                        }, Collectors.mapping(
                                t -> t.getTransactionAmount() == null ? BigDecimal.ZERO : t.getTransactionAmount(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        TopCategory topCategory = sumByCategory.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .findFirst()
                .map(e -> new TopCategory(e.getKey(), e.getValue()))
                .orElse(null);

        Optional<TrxHistory> biggestIncomingTrx = trxList.stream()
                .filter(t -> t.getDebitCredit() == DebitCredit.C)
                .max(Comparator.comparing(t -> t.getTransactionAmount() == null ? BigDecimal.ZERO : t.getTransactionAmount()));

        BiggestIncoming biggestIncoming = biggestIncomingTrx.map(t -> {
            String formattedDate = t.getTransactionDate() != null
                    ? t.getTransactionDate().atZone(java.time.ZoneId.systemDefault())
                            .withZoneSameInstant(ZoneOffset.UTC)
                            .toInstant()
                            .toString()
                    : null;

        String catId = t.getCategoryId();
        String catName = catId == null ? null : categoryMap.getOrDefault(catId, catId);
        String typeName = (catName == null || catName.isBlank()) ? "Others" : catName;
        String source = topCategory != null ? topCategory.getName() : "Others";

            return new BiggestIncoming(
                    typeName,
                    t.getTransactionAmount() == null ? BigDecimal.ZERO : t.getTransactionAmount(),
                    source,
                    formattedDate
            );
        }).orElse(null);

        return new Insights(topCategory, biggestIncoming);
        } catch (Exception ex) {
            return new Insights(null, null);
        }
    }

    private List<CategoryBreakdown> calculateCategoryBreakdown(String accountNumber, List<TrxHistory> trxList, Map<String, String> categoryMap) {
        try {
        Map<String, BigDecimal> totals = trxList.stream()
                .filter(t -> t.getDebitCredit() == DebitCredit.D)
                .collect(Collectors.groupingBy(
                        t -> {
                            String key = t.getCategoryId();
                            String name = key == null ? null : categoryMap.getOrDefault(key, key);
                            return (name == null || name.isBlank()) ? "Others" : name;
                        }, Collectors.mapping(
                                t -> t.getTransactionAmount() == null ? BigDecimal.ZERO : t.getTransactionAmount(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        if (totals.isEmpty()) return List.of();

        BigDecimal totalSpent = totals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totals.entrySet().stream()
                .map(e -> {
                    BigDecimal val = e.getValue();
                    int percent = totalSpent.compareTo(BigDecimal.ZERO) > 0
                            ? val.multiply(BigDecimal.valueOf(100)).divide(totalSpent, 0, RoundingMode.HALF_UP).intValue()
                            : 0;
                    return new CategoryBreakdown(e.getKey(), val, percent);
                })
                .sorted(Comparator.comparing(CategoryBreakdown::getTotal_amount).reversed())
                .collect(Collectors.toList());
        } catch (Exception ex) {
            return List.of();
        }

    }
    private Meta buildMeta(List<TrxHistory> trxList, Account account, java.time.YearMonth requestedMonth) {
        try {
            String month = requestedMonth == null
                    ? (trxList.isEmpty()
                        ? java.time.LocalDate.now().format(MONTH_FORMATTER)
                        : trxList.get(0).getTransactionDate().format(MONTH_FORMATTER))
                    : requestedMonth.toString();
            return new Meta(month, Optional.ofNullable(account.getCurrencyCode()).orElse("IDR"));
        } catch (Exception ex) {
            return new Meta(java.time.LocalDate.now().format(MONTH_FORMATTER), Optional.ofNullable(account.getCurrencyCode()).orElse("IDR"));
        }
    }
    private SavingDetailResponse buildEmptyResponse(Account selectedAccount, java.time.YearMonth requestedMonth) {
        String monthStr = requestedMonth == null ? java.time.LocalDate.now().format(MONTH_FORMATTER) : requestedMonth.toString();
        Meta meta = new Meta(monthStr,
                Optional.ofNullable(selectedAccount.getCurrencyCode()).orElse("IDR"));
        Summary summary = new Summary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        return new SavingDetailResponse(meta, summary, null, null);
    }

    private java.time.YearMonth ymFor(String month) {
        try {
            return (month == null || month.isBlank()) ? java.time.YearMonth.now() : java.time.YearMonth.parse(month);
        } catch (Exception ex) {
            return java.time.YearMonth.now();
        }
    }
}