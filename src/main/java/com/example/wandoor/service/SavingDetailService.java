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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.Account;
import com.example.wandoor.model.entity.TrxHistory;
import com.example.wandoor.model.enums.ProductType;
import com.example.wandoor.model.enums.DebitCredit;
import com.example.wandoor.model.request.SavingDetailRequest;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class SavingDetailService {

    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    private final TrxHistoryRepository trxHistoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter ISO_DATETIME_WITH_ZONE = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Endpoint utama untuk mendapatkan detail tabungan
     */
    public SavingDetailResponse getSavingsDetail(SavingDetailRequest request) {

        try {
            String userId = RequestContext.get().getUserId();
            String cif = RequestContext.get().getCif();
            if (cif == null || cif.isBlank()) {
                cif = profileRepository.findById(userId).map(p -> p.getCif()).orElse(null);
            }
            profileRepository.findByIdAndCif(userId, cif)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND" ,  "User not found"));

            // 3️⃣ Ambil semua rekening tabungan (SAV)
            List<Account> accounts = accountRepository.findByUserIdAndCifAndAccountType(userId, cif, ProductType.SVG);
            if (accounts.isEmpty()) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND" ,"No savings account found");
            }

            // 4️⃣ Tentukan rekening target (dari request atau rekening utama)
            Account selectedAccount = selectTargetAccount(request, accounts);

            // 5️⃣ Ambil semua transaksi dari rekening tersebut
            List<TrxHistory> trxList = trxHistoryRepository.findByAccountNumber(selectedAccount.getAccountNumber());
            if (trxList.isEmpty()) {
                return buildEmptyResponse(selectedAccount);
            }

            Summary summary = calculateSummary(trxList);

            // 7️⃣ Temukan kategori pengeluaran dan transaksi masuk terbesar
            Insights insights = calculateInsights(selectedAccount.getAccountNumber(), trxList);

            // 8️⃣ Hitung breakdown kategori (persentase pengeluaran per kategori)
            List<CategoryBreakdown> breakdown = calculateCategoryBreakdown(selectedAccount.getAccountNumber(), trxList);

            // 9️⃣ Ambil meta (bulan transaksi + mata uang)
            Meta meta = buildMeta(trxList, selectedAccount);

            // 🚀 10️⃣ Kembalikan response lengkap
            return new SavingDetailResponse(meta, summary, insights, breakdown);
        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while retrieving savings detail data", e);
        }

    }

    /**
     * Pilih rekening target berdasarkan input user atau akun utama
     */
    private Account selectTargetAccount(SavingDetailRequest request, List<Account> accounts) {
        return Optional.ofNullable(request)
                .map(SavingDetailRequest::getAccountNumber)
                .flatMap(accNum -> accounts.stream()
                        .filter(a -> a.getAccountNumber().equals(accNum))
                        .findFirst())
                .orElseGet(() -> accounts.stream()
                        .filter(a -> a.getIsMainAccount() != null && a.getIsMainAccount() == 1)
                        .findFirst()
                        .orElse(accounts.get(0)));
    }

    /**
     * Hitung total debit, kredit, dan net growth
     */
    private Summary calculateSummary(List<TrxHistory> trxList) {
        BigDecimal totalDebit = sumByDebitCredit(trxList, DebitCredit.D);
        BigDecimal totalCredit = sumByDebitCredit(trxList, DebitCredit.C);
        BigDecimal netGrowth = totalCredit.subtract(totalDebit);
        return new Summary(totalDebit, totalCredit, netGrowth);
    }

    /**
     * Helper untuk menjumlahkan transaksi berdasarkan tipe
     */
    private BigDecimal sumByDebitCredit(List<TrxHistory> trxList, DebitCredit dc) {
        return trxList.stream()
                .filter(t -> t.getDebitCredit() == dc)
                .map(TrxHistory::getTransactionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Temukan kategori utama dan transaksi masuk terbesar
     */
    private Insights calculateInsights(String accountNumber, List<TrxHistory> trxList) {
        List<Object[]> freqRows = nativeCountDebitByCategory(accountNumber);
        TopCategory topCategory = freqRows.stream()
                .findFirst()
                .map(r -> new TopCategory(r[0] == null ? "" : r[0].toString(), new BigDecimal(r[2].toString())))
                .orElseGet(() -> {
                    Map<String, Long> freq = new HashMap<>();
                    Map<String, BigDecimal> amountByCat = new HashMap<>();
                    trxList.stream()
                            .filter(t -> t.getDebitCredit() == DebitCredit.D)
                            .forEach(t -> {
                                String cat = Optional.ofNullable(t.getPaymentMethod()).orElse("Other");
                                freq.merge(cat, 1L, Long::sum);
                                amountByCat.merge(cat, t.getTransactionAmount(), BigDecimal::add);
                            });
                    return freq.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(e -> new TopCategory(e.getKey(), amountByCat.getOrDefault(e.getKey(), BigDecimal.ZERO)))
                            .orElse(null);
                });

        // Ambil transaksi masuk (CREDIT) terbesar
        Optional<TrxHistory> biggestIncomingTrx = trxList.stream()
                .filter(t -> t.getDebitCredit() == DebitCredit.C)
                .max(Comparator.comparing(TrxHistory::getTransactionAmount));

        BiggestIncoming biggestIncoming = biggestIncomingTrx.map(t -> {
            String formattedDate = t.getTransactionDate() != null
                    ? t.getTransactionDate().atZone(ZoneOffset.systemDefault())
                            .withZoneSameInstant(ZoneOffset.UTC)
                            .format(ISO_DATETIME_WITH_ZONE) + "Z"
                    : null;

        String pm = Optional.ofNullable(t.getPaymentMethod()).orElse("Other");
        String source = pm;

            return new BiggestIncoming(
                    pm,
                    t.getTransactionAmount(),
                    source,
                    formattedDate
            );
        }).orElse(null);

        return new Insights(topCategory, biggestIncoming);
    }

    /**
     * Buat breakdown kategori (pengeluaran per kategori + persen)
     */
    private List<CategoryBreakdown> calculateCategoryBreakdown(String accountNumber, List<TrxHistory> trxList) {
        List<Object[]> rows = nativeSumDebitByCategory(accountNumber);
        if (!rows.isEmpty()) {
            BigDecimal totalSpent = rows.stream()
                    .map(r -> new BigDecimal(r[1].toString()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return rows.stream()
                    .map(r -> {
                        BigDecimal val = new BigDecimal(r[1].toString());
                        int percent = totalSpent.compareTo(BigDecimal.ZERO) > 0
                                ? val.multiply(BigDecimal.valueOf(100)).divide(totalSpent, 0, RoundingMode.HALF_UP).intValue()
                                : 0;
                        return new CategoryBreakdown(r[0] == null ? "" : r[0].toString(), val, percent);
                    })
                    .sorted(Comparator.comparing(CategoryBreakdown::getTotal_amount).reversed())
                    .collect(Collectors.toList());
        }

        Map<String, BigDecimal> debitByCategory = new HashMap<>();
        trxList.stream()
                .filter(t -> t.getDebitCredit() == DebitCredit.D)
                .forEach(t -> {
                    String cat = Optional.ofNullable(t.getPaymentMethod()).orElse("Other");
                    debitByCategory.merge(cat, t.getTransactionAmount(), BigDecimal::add);
                });

        BigDecimal totalSpent = debitByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return debitByCategory.entrySet().stream()
                .map(e -> {
                    int percent = totalSpent.compareTo(BigDecimal.ZERO) > 0
                            ? e.getValue().multiply(BigDecimal.valueOf(100)).divide(totalSpent, 0, RoundingMode.HALF_UP).intValue()
                            : 0;
                    return new CategoryBreakdown(e.getKey(), e.getValue(), percent);
                })
                .sorted(Comparator.comparing(CategoryBreakdown::getTotal_amount).reversed())
                .collect(Collectors.toList());

    }

    private List<Object[]> nativeSumDebitByCategory(String accountNumber) {
        String sql = "SELECT c.CATEGORY_NAME AS categoryName, COALESCE(SUM(t.TRANSACTION_AMOUNT), 0) AS total "
                + "FROM WANDOOR.TRX_HISTORY t "
                + "JOIN WANDOOR.TRX_CATEGORY c ON c.ID = t.CATEGORY_ID "
                + "WHERE t.ACCOUNT_NUMBER = :accountNumber AND t.DEBIT_CREDIT = 'D' "
                + "GROUP BY c.CATEGORY_NAME ORDER BY total DESC";
        return entityManager.createNativeQuery(sql)
                .setParameter("accountNumber", accountNumber)
                .getResultList();
    }

    private List<Object[]> nativeCountDebitByCategory(String accountNumber) {
        String sql = "SELECT c.CATEGORY_NAME AS categoryName, COUNT(*) AS cnt, COALESCE(SUM(t.TRANSACTION_AMOUNT), 0) AS total "
                + "FROM WANDOOR.TRX_HISTORY t "
                + "JOIN WANDOOR.TRX_CATEGORY c ON c.ID = t.CATEGORY_ID "
                + "WHERE t.ACCOUNT_NUMBER = :accountNumber AND t.DEBIT_CREDIT = 'D' "
                + "GROUP BY c.CATEGORY_NAME ORDER BY cnt DESC";
        return entityManager.createNativeQuery(sql)
                .setParameter("accountNumber", accountNumber)
                .getResultList();
    }

    /**
     * Buat metadata response (bulan & mata uang)
     */
    private Meta buildMeta(List<TrxHistory> trxList, Account account) {
        String month = trxList.isEmpty()
                ? java.time.LocalDate.now().format(MONTH_FORMATTER)
                : trxList.get(0).getTransactionDate().format(MONTH_FORMATTER);
        return new Meta(month, Optional.ofNullable(account.getCurrencyCode()).orElse("IDR"));
    }

    /**
     * Jika tidak ada transaksi, kembalikan response default kosong
     */
    private SavingDetailResponse buildEmptyResponse(Account selectedAccount) {
        Meta meta = new Meta(java.time.LocalDate.now().format(MONTH_FORMATTER),
                Optional.ofNullable(selectedAccount.getCurrencyCode()).orElse("IDR"));
        Summary summary = new Summary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        return new SavingDetailResponse(meta, summary, null, null);
    }
}