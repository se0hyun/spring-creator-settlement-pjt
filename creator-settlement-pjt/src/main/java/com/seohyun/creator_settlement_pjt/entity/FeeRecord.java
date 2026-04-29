package com.seohyun.creator_settlement_pjt.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA용
public class FeeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="fee_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal feeRate;

    @Column(name="start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name="end_at", nullable = false)
    private LocalDateTime endAt;

    @Builder
    private FeeRecord(BigDecimal feeRate, LocalDateTime startAt, LocalDateTime endAt) {
        this.feeRate = feeRate;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    /**
     * 새 수수료 구간이 {@code effectiveFromInclusive}부터 시작할 때, 기존 구간의 종료 시각을 그 직전 순간으로 맞춘다.
     */
    public void truncateEndBeforeNextPeriodStarts(LocalDateTime effectiveFromInclusive) {
        LocalDateTime boundary = effectiveFromInclusive.minusNanos(1);
        if (boundary.isBefore(this.startAt)) {
            throw new IllegalArgumentException("effectiveFrom이 기존 구간 시작보다 앞입니다.");
        }
        this.endAt = boundary;
    }
}