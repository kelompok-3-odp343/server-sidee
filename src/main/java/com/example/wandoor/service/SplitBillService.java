package com.example.wandoor.service;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.SplitBill;
import com.example.wandoor.model.entity.SplitBillMember;
import com.example.wandoor.model.request.EditSplitBillRequest;
import com.example.wandoor.model.request.SplitBillDetailRequest;
import com.example.wandoor.model.response.AddNewSplitBillResponse;
import com.example.wandoor.model.response.EditSplitBillResponse;
import com.example.wandoor.model.response.SplitBillDetailResponse;
import com.example.wandoor.model.request.AddNewSplitBillRequest;
import com.example.wandoor.model.response.SplitBillsListResponse;
import com.example.wandoor.repository.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
// import lombok.extern.log4j.Log4j2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
//@AllArgsConstructor
@RequiredArgsConstructor
// @Log4j2
public class SplitBillService {
    private final SplitBillRepository splitBillRepository;
    private final SplitBillMemberRepository splitBillMemberRepository;
    private final ProfileRepository profileRepository;
    private final TrxHistoryRepository trxHistoryRepository;
    private final AccountRepository accountRepository;
    private static final Logger log = LoggerFactory.getLogger(SplitBillService.class);

    public SplitBillsListResponse getAllSplitBill(){
        long start = System.currentTimeMillis();
        String traceId = org.slf4j.MDC.get("traceId");

        try {
                var userId = RequestContext.get().getUserId();
                var cif = RequestContext.get().getCif();
        
                log.info("Fetching split bills for userId={} and cif={}", userId, cif);
        
                var userExists = profileRepository.findByIdAndCif(userId, cif)
                        .orElseThrow(() -> {
                                log.warn("User not found for userId={} and cif={}", userId, cif);
                                return new BusinessException(HttpStatus.NOT_FOUND, "INVALID_USER", "User not found");
                        });
        
                List<SplitBill> userSplitBills = splitBillRepository.findByUserIdAndCif(userId, cif);

                if (userSplitBills.isEmpty()) {
                        long duration = System.currentTimeMillis() - start;
                        log.info("No split bills found for userId={} cif={} (traceId={}) in {} ms", 
                        userId, cif, traceId, duration);
                        return new SplitBillsListResponse(Collections.emptyList());
                }
        
                List<SplitBillsListResponse.SplitBillData> responseList = new ArrayList<>();
        
                for (SplitBill bill: userSplitBills) {
                    List<SplitBillMember> members = splitBillMemberRepository.findAllBySplitBillId(bill.getId());
        
                    BigDecimal paidAmount = BigDecimal.ZERO;
                    BigDecimal remainingAmount = BigDecimal.ZERO;
                    int countPaid = 0;
                    int countUnpaid = 0;
        
                    List<SplitBillsListResponse.SplitBillData.SplitBillMemberDetail> memberDetails = new ArrayList<>();
        
                    for (SplitBillMember member: members){
                        if (member.getHasPaid() != null && member.getHasPaid() == 1){
                            paidAmount = paidAmount.add(member.getAmountShare());
                            countPaid++;
                        } else {
                            remainingAmount = remainingAmount.add(member.getAmountShare());
                            countUnpaid++;
                        }
        
                        // Tambahkan ke member detail
                        SplitBillsListResponse.SplitBillData.SplitBillMemberDetail memberDetail =
                                new SplitBillsListResponse.SplitBillData.SplitBillMemberDetail(
                                        member.getId(),
                                        member.getMemberName(),
                                        member.getAmountShare(),
                                        member.getHasPaid() != null && member.getHasPaid() == 1
                                );
                        memberDetails.add(memberDetail);
                    }
                    // Buat Object SplitBillData
                    SplitBillsListResponse.SplitBillData splitBillData =
                            new SplitBillsListResponse.SplitBillData(
                                    bill.getId(),
                                    bill.getSplitBillTitle(),
                                    bill.getTransactionId(),
                                    bill.getCurrency(),
                                    bill.getTotalAmount(),
                                    remainingAmount,
                                    paidAmount,
                                    countPaid,
                                    countUnpaid,
                                    memberDetails
                            );
                    responseList.add(splitBillData);
                }

                long duration = System.currentTimeMillis() - start;
                log.info("Successfully fetched {} split bills in {} ms (traceId={})",
                        responseList.size(), duration, traceId);
                return new SplitBillsListResponse(responseList);         
        } catch (ResponseStatusException e) {
                log.warn("Business error while fetching split bills: {}", e.getMessage());
                throw e;
                
        } catch (Exception e) {
                log.error("Unexpected error fetching split bills", e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch split bills");
        }
}

    public SplitBillDetailResponse getAllSplitBillMember(SplitBillDetailRequest request) {
            var userId = RequestContext.get().getUserId();
            var cif = RequestContext.get().getCif();

            try {
                    var splitBIllData = splitBillRepository.findByUserIdAndCifAndId(userId, cif, request.splitBillId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT, "DATA_NOT_FOUND","Split Bill Not Found"));
                    //        if (splitBIllData.isEmpty()) {
                            //            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
                            //        }

                            var transactionData = trxHistoryRepository.findById(splitBIllData.getTransactionId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No Transaction Related to Split BIll"));

                            var members = splitBillMemberRepository.findAllBySplitBillId(request.splitBillId());
                            if (members.isEmpty()) {
                                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
                            }

                            // Buat list member response
                            List<SplitBillDetailResponse.Data.Member> memberList = new ArrayList<>();
                            for (SplitBillMember m : members) {
                                    Boolean hasPaid = m.getHasPaid() != null && m.getHasPaid() == 1;
                                    BigDecimal amountShare = m.getAmountShare();

                                    // Jika hasPaid >= amountShare → Paid, else Unpaid
                                    //String status = (hasPaid.compareTo(amountShare) >= 0) ? "Paid" : "Unpaid";

                                    // Konversi tanggal ke String agar cocok dengan record
                                    String paymentDate = (m.getPaymentDate() != null)
                                    ? m.getPaymentDate().toString()
                                    : "-";

                                    memberList.add(new SplitBillDetailResponse.Data.Member(
                                            m.getId(),
                                            m.getMemberName(),
                                            amountShare,
                                            paymentDate,
                                            hasPaid
                                            ));
                                    }




                                    // Ambil data utama dari transaksi split bill

                                    SplitBillDetailResponse.Data data = new SplitBillDetailResponse.Data(
                                            splitBIllData.getId(),
                                            splitBIllData.getSplitBillTitle(),
                                            splitBIllData.getCurrency(),
                                            transactionData.getRefId(),
                                            splitBIllData.getCreatedTime().toString(),
                                            splitBIllData.getTotalAmount(),
                                            splitBIllData.getCreatedTime().toString(),
                            transactionData.getTransactionDate().toString(),
                            memberList
                            );

                            // Return response akhir
                            return new SplitBillDetailResponse(
                                    data
                                    );

                            } catch (ResponseStatusException e) {
                                    log.warn("Business error while fetching split bills: {}", e.getMessage());
                                    throw e;
                                    // TODO: handle exception
                            } catch (Exception e) {
                                    log.error("Unexpected error fetching split bills", e);
                                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch split bills");
                                    // TODO: handle exception
                            } finally {
                                    MDC.clear();
                            }

                    }

    @Transactional
    public AddNewSplitBillResponse createSplitBill(AddNewSplitBillRequest request) {
        //        System.out.println("Hai Aku dari Service");
        log.info("Receive create split bill request dari service: {}", request.splitBillTitle());
        var userId = RequestContext.get().getUserId();
        var cif = RequestContext.get().getCif();

        var userData = profileRepository.findByIdAndCif(userId, cif)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "User not found"));

        var account = accountRepository.findByUserIdAndCifAndAccountNumber(userId, cif, request.accountNumber())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.CONFLICT, "No Such Account"));


        var trx = trxHistoryRepository
                .findByIdAndAccountNumber(request.transactionId(), account.getAccountNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Invalid Transaction Id"));

        BigDecimal totalMemberAmount = request.billMembers().stream()
                .map(AddNewSplitBillRequest.BillMembers::amountShare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (trx.getTransactionAmount().compareTo(totalMemberAmount) != 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Invalid Total Amount");
        }

        //insert ke table split bill
        SplitBill splitBill = SplitBill.builder()
                .userId(userId)
                .cif(cif)
                .accountNumber(account.getAccountNumber())
                .transactionId(trx.getId())
                .splitBillTitle(request.splitBillTitle())
                .currency(request.currency())
                .totalAmount(trx.getTransactionAmount())
                .isDeleted(0)
                .createdBy("SYSTEM")
                .createdTime(LocalDateTime.now())
                .updatedBy("SYSTEM")
                .updatedTime(LocalDateTime.now())
                .build();

        var savedSplitBill = splitBillRepository.save(splitBill);

        List<SplitBillMember> members = request.billMembers().stream()
                .map(m -> SplitBillMember.builder()
                        .splitBill(savedSplitBill)
                        .userId(userData.getId())
                        .memberName(m.memberName())
                        .amountShare(m.amountShare())
                        .hasPaid(0)
                        .isDeleted(0)
                        .createdBy("SYSTEM")
                        .createdTime(LocalDateTime.now())
                        .updatedBy("SYSTEM")
                        .updatedTime(LocalDateTime.now())
                        .build())
                .toList();

        splitBillMemberRepository.saveAll(members);

        log.info("Split bill created successfully with ID={}", splitBill.getId());

        return new AddNewSplitBillResponse(
                "Split bill created successfully",
                splitBill.getId()
        );
    }

   @Transactional
    public EditSplitBillResponse editSplitBill(EditSplitBillRequest request) {
        // validate user data
        var cif = RequestContext.get().getCif();
        var userId = RequestContext.get().getUserId();
        var userData = profileRepository.findByIdAndCif(userId, cif)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "User Not Found"));

        // validate Split Bill Data
        var trxHistoryData = trxHistoryRepository.findById(request.transactionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Invalid Transaction Id"));

        var splitBillData = splitBillRepository.findByIdAndUserIdAndCifAndTransactionId(request.splitBillId(), userData.getId(), userData.getCif(), trxHistoryData.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Split Bill Data Not Found"));


        splitBillData.setSplitBillTitle(request.splitBillTitle());
        splitBillData.setTotalAmount(request.totalAmount());
        splitBillData.setUpdatedTime(LocalDateTime.now());
        splitBillRepository.save(splitBillData);


        var existingMembers = splitBillMemberRepository.findAllBySplitBillId(splitBillData.getId());
        Map<String, SplitBillMember> existingMap = existingMembers.stream()
                .collect(Collectors.toMap(SplitBillMember::getId, Function.identity()));

        Set<String> requestIds = request.billMembers().stream()
                .map(EditSplitBillRequest.BillMembers::memberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Update and Insert
        for (var newMember : request.billMembers()) {
            if (newMember.memberId() != null && existingMap.containsKey(newMember.memberId())) {
                var existing = existingMap.get(newMember.memberId());
                existing.setMemberName(newMember.memberName());
                existing.setAmountShare(newMember.amountShare());
                existing.setHasPaid(newMember.hasPaid() ? 1 : 0);
                existing.setUpdatedTime(LocalDateTime.now());
                splitBillMemberRepository.save(existing);
            } else {
                var entity = new SplitBillMember();
                entity.setId(UUID.randomUUID().toString());
                entity.setSplitBill(splitBillData);
                entity.setMemberName(newMember.memberName());
                entity.setAmountShare(newMember.amountShare());
                entity.setHasPaid(newMember.hasPaid() ? 1 : 0);
                entity.setCreatedTime(LocalDateTime.now());
                entity.setUpdatedTime(LocalDateTime.now());
                splitBillMemberRepository.save(entity);
            }

            // Delete member yang tidak ada di request
            for (var existing: existingMembers){
                if (!requestIds.contains(existing.getId())){
                    splitBillMemberRepository.delete(existing);
                }
            }

        }
            return new EditSplitBillResponse(
                    "Split bill updated successfully",
                    splitBillData.getId()
            );
    }
}
