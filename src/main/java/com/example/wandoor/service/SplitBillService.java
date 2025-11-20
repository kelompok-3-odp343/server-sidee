package com.example.wandoor.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.SplitBill;
import com.example.wandoor.model.entity.SplitBillMember;
import com.example.wandoor.model.request.AddNewSplitBillRequest;
import com.example.wandoor.model.request.EditSplitBillRequest;
import com.example.wandoor.model.request.PatchSplitBillRequest;
import com.example.wandoor.model.request.SplitBillDetailRequest;
import com.example.wandoor.model.response.AddNewSplitBillResponse;
import com.example.wandoor.model.response.EditSplitBillResponse;
import com.example.wandoor.model.response.SplitBillDetailResponse;
import com.example.wandoor.model.response.SplitBillsListResponse;
import com.example.wandoor.repository.AccountRepository;
import com.example.wandoor.repository.ProfileRepository;
import com.example.wandoor.repository.SplitBillMemberRepository;
import com.example.wandoor.repository.SplitBillRepository;
import com.example.wandoor.repository.TrxHistoryRepository;

import lombok.RequiredArgsConstructor;

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
        } catch (BusinessException e) {
            throw e;
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
        try {
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

        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Add new split bill error: ", e);
            throw e;
        } catch (Exception e) {
            log.error("Add new split bill error: ", e);
            throw e;
        }
    }

    @Transactional
    public EditSplitBillResponse editSplitBill(EditSplitBillRequest request) {
        try {
            var cif = RequestContext.get().getCif();
            var userId = RequestContext.get().getUserId();

            var userData = profileRepository.findByIdAndCif(userId, cif)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "User Not Found"));

            var trxHistoryData = trxHistoryRepository.findById(request.transactionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Invalid Transaction Id"));

            var splitBill = splitBillRepository.findByIdAndUserIdAndCifAndTransactionId(
                            request.splitBillId(),
                            userData.getId(),
                            userData.getCif(),
                            trxHistoryData.getId()
                    )
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Split Bill Data Not Found"));

            splitBill.setSplitBillTitle(request.splitBillTitle());
            splitBill.setTotalAmount(request.totalAmount());
            splitBill.setUpdatedTime(LocalDateTime.now());
            splitBill.setUpdatedBy(userId);
            splitBillRepository.save(splitBill);

            var existingMembers = splitBillMemberRepository.findAllBySplitBillId(splitBill.getId());
            Map<String, SplitBillMember> existingMap = existingMembers.stream()
                    .collect(Collectors.toMap(SplitBillMember::getId, Function.identity()));

            Set<String> requestExistingIds = request.billMembers().stream()
                    .map(EditSplitBillRequest.BillMembers::memberId)
                    .filter(id -> id != null && !id.isBlank())
                    .collect(Collectors.toSet());

            for (var m : request.billMembers()) {

                    if (m.memberId() != null && existingMap.containsKey(m.memberId())) {
                    var existing = existingMap.get(m.memberId());

                    existing.setMemberName(m.memberName());
                    existing.setAmountShare(m.amountShare());
                    existing.setHasPaid(Boolean.TRUE.equals(m.hasPaid()) ? 1 : 0);
                    existing.setUpdatedTime(LocalDateTime.now());
                    existing.setUpdatedBy(userId);

                    splitBillMemberRepository.save(existing);
                    }
            }

            for (var e : existingMembers) {
                    if (!requestExistingIds.contains(e.getId())) {
                    splitBillMemberRepository.deleteById(e.getId());
                    }
            }

            for (var m : request.billMembers()) {

                    boolean exists = m.memberId() != null && existingMap.containsKey(m.memberId());

                    if (!exists) {
                    var entity = new SplitBillMember();
                    entity.setId(UUID.randomUUID().toString());
                    entity.setSplitBill(splitBill);
                    entity.setUserId(userId);
                    entity.setCreatedBy(userId);
                    entity.setUpdatedBy(userId);

                    entity.setMemberName(m.memberName());
                    entity.setAmountShare(m.amountShare());
                    entity.setHasPaid(Boolean.TRUE.equals(m.hasPaid()) ? 1 : 0);

                    entity.setCreatedTime(LocalDateTime.now());
                    entity.setUpdatedTime(LocalDateTime.now());

                    splitBillMemberRepository.save(entity);
                    }
            }

            return new EditSplitBillResponse(
                    "Split bill updated successfully",
                    splitBill.getId()
            );

        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Edit split bill error", e);
            throw e;
        } catch (Exception e) {
            log.error("Edit split bill error", e);
            throw e;
        }

    }

    @Transactional
    public void updateHaspaidSplitBill(PatchSplitBillRequest request) {
            
            try {
                var userId = RequestContext.get().getUserId();
                var cif = RequestContext.get().getCif();
                profileRepository.findByIdAndCif(userId, cif)
                        .orElseThrow(() -> {
                                log.warn("User not found for cif={} and userId={}", cif, userId);
                                return new BusinessException(HttpStatus.CONFLICT, "INVALID_USER", "User Not Found");    
                        });

                splitBillRepository.findByUserIdAndCifAndId(userId, cif, request.splitBillId())
                        .orElseThrow(() -> {
                                log.warn("Split Bill not found for splitBillId={} and memberId={}", request.splitBillId(), request.memberId());
                                return new BusinessException(HttpStatus.CONFLICT, "INVALID_SPLIT_BILL", "No Such Split Bill or Member");
                        });

                splitBillMemberRepository.findUnpaidSplitBillMemberById(request.splitBillId(), request.memberId())
                        .orElseThrow(() -> {
                            log.warn("Unpaid split bill member for memberId={} not found", request.memberId());
                            return new BusinessException(HttpStatus.CONFLICT, "UNPAID_MEMBER_NOT_FOUND", "Unpaid split bill member not found");
                        });

                int updated = splitBillMemberRepository.markAsPaid(request.splitBillId(), request.memberId());

                if (updated == 0) {
                        log.warn("Member update failed for splitBillId={} and memberId={}", request.splitBillId(), request.memberId());
                        throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UPDATE_FAILED", "Failed to mark as paid");
                        }        
                } catch (ResponseStatusException e) {
                        throw e;
                        
                } catch (Exception e) {
                        log.error("Unexpected error fetching split bills", e);
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch split bills");
                }
        }
}
