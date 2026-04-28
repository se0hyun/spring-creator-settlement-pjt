package com.seohyun.creator_settlement_pjt.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancel_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA용
public class CancelRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_record_id", nullable = false)
    private SaleRecord saleRecord;

    @Column(name="cancel_amount", nullable = false)
    private long cancelAmount;

    @Column(name="canceled_at", nullable = false)
    private LocalDateTime canceledAt;

    @Builder
    private CancelRecord(SaleRecord saleRecord, long cancelAmount, LocalDateTime canceledAt) {
        this.saleRecord = saleRecord;
        this.cancelAmount = cancelAmount;
        this.canceledAt = canceledAt;
    }
}