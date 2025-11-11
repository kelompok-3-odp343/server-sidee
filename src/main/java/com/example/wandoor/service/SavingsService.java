package com.example.wandoor.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.Account;
import com.example.wandoor.model.entity.TrxHistory;
import com.example.wandoor.model.enums.AccountStatus;
import com.example.wandoor.model.enums.ProductType;
import com.example.wandoor.model.request.SavingsRequest;
import com.example.wandoor.model.response.SavingsResponse;
import com.example.wandoor.model.response.SavingsResponse.BiggestIncoming;
import com.example.wandoor.model.response.SavingsResponse.CategoryBreakdown;
import com.example.wandoor.model.response.SavingsResponse.Insights;
import com.example.wandoor.model.response.SavingsResponse.Meta;
import com.example.wandoor.model.response.SavingsResponse.Summary;
import com.example.wandoor.model.response.SavingsResponse.TopCategory;
import com.example.wandoor.repository.AccountRepository;
import com.example.wandoor.repository.ProfileRepository;
import com.example.wandoor.repository.TrxHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class SavingsService {

    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    private final TrxHistoryRepository trxHistoryRepository;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter ISO_DATETIME_WITH_ZONE = DateTimeFormatter.ISO_DATE_TIME;

    // Keyword → category (ref_id)
    private static final Map<String, String> DESCRIPTION_KEYWORDS = Map.ofEntries(
            Map.entry("kopi", "REF_FOOD"),
            Map.entry("coffee", "REF_FOOD"),
            Map.entry("starbucks", "REF_FOOD"),
            Map.entry("boba", "REF_FOOD"),
            Map.entry("grabfood", "REF_FOOD"),
            Map.entry("gofood", "REF_FOOD"),
            Map.entry("resto", "REF_FOOD"),

            Map.entry("baju", "REF_SHOPPING"),
            Map.entry("kaos", "REF_SHOPPING"),
            Map.entry("pakaian", "REF_SHOPPING"),
            Map.entry("fashion", "REF_SHOPPING"),
            Map.entry("tokopedia", "REF_SHOPPING"),
            Map.entry("shopee", "REF_SHOPPING"),

            Map.entry("grab", "REF_TRANSPORT"),
            Map.entry("gojek", "REF_TRANSPORT"),
            Map.entry("ojek", "REF_TRANSPORT"),
            Map.entry("taxi", "REF_TRANSPORT"),

            Map.entry("gaji", "REF_SALARY"),
            Map.entry("salary", "REF_SALARY"),
            Map.entry("payroll", "REF_SALARY"),

            Map.entry("pln", "REF_BILLS"),
            Map.entry("telkom", "REF_BILLS"),
            Map.entry("wifi", "REF_BILLS"),
            Map.entry("indihome", "REF_BILLS"),
            Map.entry("pulsa", "REF_BILLS"),

            Map.entry("netflix", "REF_ENTERTAINMENT"),
            Map.entry("spotify", "REF_ENTERTAINMENT"),
            Map.entry("youtube premium", "REF_ENTERTAINMENT")
    );

    private static final Map<String, String> PAYMENT_METHOD_KEYWORDS = Map.ofEntries(
            Map.entry("QRIS", "REF_EWALLET"),
            Map.entry("OVO", "REF_EWALLET"),
            Map.entry("GOPAY", "REF_EWALLET"),
            Map.entry("DANA", "REF_EWALLET"),
            Map.entry("SHOPEEPAY", "REF_EWALLET")
    );

    public SavingsResponse getSavingsDetail(SavingsRequest request) {
        try {
            final String userId = RequestContext.get().getUserId();
            final String cif = RequestContext.get().getCif();

            log.info("DEBUG-SAVING-1 | userId={} cif={} requestAccount={}", userId, cif,
                    request != null ? request.getAccountNumber() : null);

            // Validasi user
            profileRepository.findByIdAndCif(userId, cif)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "User not found"));

            // Ambil akun SVG, tapi hanya status BUKA atau BARU
            List<Account> accounts = accountRepository
                    .findByUserIdAndCifAndAccountType(userId, cif, ProductType.SVG)
                    .stream()
                    .filter(a -> a.getAccountStatus() == AccountStatus.BUKA || a.getAccountStatus() == AccountStatus.BARU)
                    .toList();

            if (accounts.isEmpty()) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "No active savings account found");
            }

            Account selected = selectTargetAccountStrict(request, accounts);

            List<TrxHistory> trxList = trxHistoryRepository.findByAccountNumber(selected.getAccountNumber());
            log.info("DEBUG-SAVING-2 | trxCount={} account={}", trxList.size(), selected.getAccountNumber());

            if (trxList.isEmpty()) {
                return buildEmptyResponse(selected);
            }

            // Apply category classification before summary processing
            trxList.forEach(t -> t.setRefId(resolveCategory(t)));

            Summary summary = calculateSummary(trxList);
            Insights insights = calculateInsights(trxList);
            List<CategoryBreakdown> breakdown = calculateCategoryBreakdown(trxList);
            Meta meta = buildMeta(trxList, selected);

            return new SavingsResponse(meta, summary, insights, breakdown.isEmpty() ? null : breakdown);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("🔥 REAL ERROR in getSavingsDetail: ", e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR",
                    "Something went wrong while retrieving savings detail data");
        }
    }

    private String resolveCategory(TrxHistory trx) {
        String desc = Optional.ofNullable(trx.getTransactionDescription()).orElse("").toLowerCase();
        String party = Optional.ofNullable(trx.getPartyName()).orElse("").toLowerCase();
        String method = Optional.ofNullable(trx.getPaymentMethod()).orElse("").toUpperCase();

        for (var e : DESCRIPTION_KEYWORDS.entrySet()) {
            if (desc.contains(e.getKey().toLowerCase())) return e.getValue();
        }

        for (var e : PAYMENT_METHOD_KEYWORDS.entrySet()) {
            if (party.contains(e.getKey().toLowerCase()) || method.contains(e.getKey())) return e.getValue();
        }

        return Optional.ofNullable(trx.getRefId()).orElse("REF_UNCATEGORIZED");
    }

    private Account selectTargetAccountStrict(SavingsRequest request, List<Account> accounts) {
        if (request != null && request.getAccountNumber() != null && !request.getAccountNumber().isBlank()) {
            return accounts.stream()
                    .filter(a -> a.getAccountNumber().equals(request.getAccountNumber()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND",
                            "Requested account does not belong to the user"));
        }
        return accounts.stream()
                .filter(a -> a.getIsMainAccount() == 1)
                .findFirst()
                .orElse(accounts.get(0));
    }

    private Summary calculateSummary(List<TrxHistory> trxList) {
        BigDecimal debit = sum(trxList, "DEBIT");
        BigDecimal credit = sum(trxList, "CREDIT");
        return new Summary(debit, credit, credit.subtract(debit));
    }

    private BigDecimal sum(List<TrxHistory> list, String type) {
        return list.stream()
                .filter(t -> type.equalsIgnoreCase(t.getTransactionType()))
                .map(TrxHistory::getTransactionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Insights calculateInsights(List<TrxHistory> trxList) {
        // Top category based on spending (debit)
        Map<String, BigDecimal> spent = trxList.stream()
                .filter(t -> "DEBIT".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.groupingBy(
                        t -> t.getRefId(),
                        Collectors.reducing(BigDecimal.ZERO, TrxHistory::getTransactionAmount, BigDecimal::add)
                ));

        TopCategory topCategory = spent.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> new TopCategory(e.getKey(), e.getValue()))
                .orElse(null);

        // Biggest incoming
        var maxIncoming = trxList.stream()
                .filter(t -> "CREDIT".equalsIgnoreCase(t.getTransactionType()))
                .max(Comparator.comparing(TrxHistory::getTransactionAmount))
                .map(t -> new BiggestIncoming(
                        t.getTransactionType(),
                        t.getTransactionAmount(),
                        Optional.ofNullable(t.getTransactionDescription()).orElse("Unknown"),
                        t.getTransactionDate().atZone(ZoneOffset.systemDefault())
                                .withZoneSameInstant(ZoneOffset.UTC)
                                .format(ISO_DATETIME_WITH_ZONE) + "Z"
                ))
                .orElse(null);

        return new Insights(topCategory, maxIncoming);
    }

    private List<CategoryBreakdown> calculateCategoryBreakdown(List<TrxHistory> trxList) {
        Map<String, BigDecimal> spent = trxList.stream()
                .filter(t -> "DEBIT".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.groupingBy(
                        t -> t.getRefId(),
                        Collectors.reducing(BigDecimal.ZERO, TrxHistory::getTransactionAmount, BigDecimal::add)
                ));

        BigDecimal total = spent.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return spent.entrySet().stream()
                .map(e -> new CategoryBreakdown(
                        e.getKey(),
                        e.getValue(),
                        total.compareTo(BigDecimal.ZERO) > 0
                                ? e.getValue().multiply(BigDecimal.valueOf(100))
                                .divide(total, 0, RoundingMode.HALF_UP).intValue()
                                : 0
                ))
                .sorted(Comparator.comparing(CategoryBreakdown::getTotal_amount).reversed())
                .collect(Collectors.toList());
    }

    private Meta buildMeta(List<TrxHistory> trxList, Account account) {
        String month = trxList.get(0).getTransactionDate().format(MONTH_FORMATTER);
        return new Meta(month, account.getCurrencyCode());
    }

    private SavingsResponse buildEmptyResponse(Account selectedAccount) {
        Meta meta = new Meta(java.time.LocalDate.now().format(MONTH_FORMATTER), selectedAccount.getCurrencyCode());
        return new SavingsResponse(meta, new Summary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), null, null);
    }
}
