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
import lombok.Builder;
import java.math.BigDecimal;
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

    /** 취소 일시 기준 유효했던 플랫폼 수수료율(%) 스냅샷 — 정산 집계는 월별 FeeRecord를 쓰지만, 감사·표시용으로 저장 */
    @Column(name = "fee_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal feeRate;

    @Builder
    private CancelRecord(SaleRecord saleRecord, long cancelAmount, LocalDateTime canceledAt, BigDecimal feeRate) {
        this.saleRecord = saleRecord;
        this.cancelAmount = cancelAmount;
        this.canceledAt = canceledAt;
        this.feeRate = feeRate;
    }
}