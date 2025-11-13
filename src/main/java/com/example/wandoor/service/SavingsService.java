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

import com.example.wandoor.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.model.entity.Account;
import com.example.wandoor.model.entity.TrxHistory;
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

    /**
     * Endpoint utama untuk mendapatkan detail tabungan
     */
    public SavingsResponse getSavingsDetail(SavingsRequest request) {

        try {
            String userId = RequestContext.get().getUserId();
            String cif = RequestContext.get().getCif();

            // 2️⃣ Validasi user
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

            // 6️⃣ Hitung total debit, kredit, dan pertumbuhan bersih
            Summary summary = calculateSummary(trxList);

            // 7️⃣ Temukan kategori pengeluaran dan transaksi masuk terbesar
            Insights insights = calculateInsights(trxList);

            // 8️⃣ Hitung breakdown kategori (persentase pengeluaran per kategori)
            List<CategoryBreakdown> breakdown = calculateCategoryBreakdown(trxList);

            // 9️⃣ Ambil meta (bulan transaksi + mata uang)
            Meta meta = buildMeta(trxList, selectedAccount);

            // 🚀 10️⃣ Kembalikan response lengkap
            return new SavingsResponse(meta, summary, insights, breakdown.isEmpty() ? null : breakdown);
        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while retrieving savings detail data", e);
        }

    }

    /**
     * Pilih rekening target berdasarkan input user atau akun utama
     */
    private Account selectTargetAccount(SavingsRequest request, List<Account> accounts) {
        return Optional.ofNullable(request)
                .map(SavingsRequest::getAccountNumber)
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
        BigDecimal totalDebit = sumByType(trxList, "DEBIT");
        BigDecimal totalCredit = sumByType(trxList, "CREDIT");
        BigDecimal netGrowth = totalCredit.subtract(totalDebit);
        return new Summary(totalDebit, totalCredit, netGrowth);
    }

    /**
     * Helper untuk menjumlahkan transaksi berdasarkan tipe
     */
    private BigDecimal sumByType(List<TrxHistory> trxList, String type) {
        return trxList.stream()
                .filter(t -> type.equalsIgnoreCase(t.getTransactionType()))
                .map(TrxHistory::getTransactionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Temukan kategori utama dan transaksi masuk terbesar
     */
    private Insights calculateInsights(List<TrxHistory> trxList) {
        // Ambil kategori pengeluaran terbesar
        Map<String, BigDecimal> debitByCategory = trxList.stream()
                .filter(t -> "DEBIT".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.groupingBy(
                        t -> Optional.ofNullable(t.getPaymentMethod()).orElse("Other"),
                        Collectors.reducing(BigDecimal.ZERO, TrxHistory::getTransactionAmount, BigDecimal::add)
                ));

        TopCategory topCategory = debitByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> new TopCategory(e.getKey(), e.getValue()))
                .orElse(null);

        // Ambil transaksi masuk (CREDIT) terbesar
        Optional<TrxHistory> biggestIncomingTrx = trxList.stream()
                .filter(t -> "CREDIT".equalsIgnoreCase(t.getTransactionType()))
                .max(Comparator.comparing(TrxHistory::getTransactionAmount));

        BiggestIncoming biggestIncoming = biggestIncomingTrx.map(t -> {
            String formattedDate = t.getTransactionDate() != null
                    ? t.getTransactionDate().atZone(ZoneOffset.systemDefault())
                            .withZoneSameInstant(ZoneOffset.UTC)
                            .format(ISO_DATETIME_WITH_ZONE) + "Z"
                    : null;

            return new BiggestIncoming(
                    Optional.ofNullable(t.getTransactionType()).orElse("TRANSFER"),
                    t.getTransactionAmount(),
                    Optional.ofNullable(t.getTransactionDescription()).orElse("Unknown"),
                    formattedDate
            );
        }).orElse(null);

        return new Insights(topCategory, biggestIncoming);
    }

    /**
     * Buat breakdown kategori (pengeluaran per kategori + persen)
     */
    private List<CategoryBreakdown> calculateCategoryBreakdown(List<TrxHistory> trxList) {
        Map<String, BigDecimal> debitByCategory = trxList.stream()
                .filter(t -> "DEBIT".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.groupingBy(
                        t -> Optional.ofNullable(t.getTransactionDescription()).orElse("Other"),
                        Collectors.reducing(BigDecimal.ZERO, TrxHistory::getTransactionAmount, BigDecimal::add)
                ));

        BigDecimal totalSpent = debitByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return debitByCategory.entrySet().stream()
                .map(e -> {
                    int percent = totalSpent.compareTo(BigDecimal.ZERO) > 0
                            ? e.getValue().multiply(BigDecimal.valueOf(100))
                                    .divide(totalSpent, 0, RoundingMode.HALF_UP)
                                    .intValue()
                            : 0;
                    return new CategoryBreakdown(e.getKey(), e.getValue(), percent);
                })
                .sorted(Comparator.comparing(CategoryBreakdown::getTotal_amount).reversed())
                .collect(Collectors.toList());
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
    private SavingsResponse buildEmptyResponse(Account selectedAccount) {
        Meta meta = new Meta(java.time.LocalDate.now().format(MONTH_FORMATTER),
                Optional.ofNullable(selectedAccount.getCurrencyCode()).orElse("IDR"));
        Summary summary = new Summary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        return new SavingsResponse(meta, summary, null, null);
    }
}
