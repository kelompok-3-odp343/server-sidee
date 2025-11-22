package com.example.wandoor.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.sql.Clob;
import java.sql.NClob;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class TrActivity {
    @Id
    @ToString.Include
    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private String id;

    @Lob
    private Clob metaData;

    @Column(nullable = false)
    private String makerId;
    private String checkerId;
    private String approverId;
    @Column(nullable = false)
    private String menuId;
    @Column(nullable = false)
    private String menuName;
    @Column(nullable = false)
    private String identifier;
    @Column(nullable = false)
    private String actionFlow;
    @Column(nullable = false)
    private String actionMenu;
    private String status;
    private String reason;
    private LocalDateTime updatedTimeChecker;
    private LocalDateTime updatedTimeApprover;
    private LocalDateTime createdTime;
    private String createdBy;
    private LocalDateTime updatedTime;
    private String updatedBy;




}
