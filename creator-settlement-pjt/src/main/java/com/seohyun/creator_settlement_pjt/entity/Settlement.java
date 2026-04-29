package com.seohyun.creator_settlement_pjt.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import com.seohyun.creator_settlement_pjt.exception.BusinessException;
import com.seohyun.creator_settlement_pjt.exception.ErrorCode;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "settlements",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "settlement_year", "settlement_month"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User creator;

    @Column(name = "settlement_year", nullable = false)
    private int year;

    @Column(name = "settlement_month", nullable = false)
    private int month;

    @Column(nullable = false)
    private long totalSales;

    @Column(nullable = false)
    private long totalRefunds;

    @Column(nullable = false)
    private long netSales;

    @Column(nullable = false)
    private long feeAmount;

    @Column(nullable = false)
    private long settlementAmount;

    @Column(nullable = false)
    private int salesCount;

    @Column(nullable = false)
    private int cancelCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime confirmedAt;

    @Column
    private LocalDateTime paidAt;

    @Column(nullable = false)
    private long carryOverAmount;   // 이월 금액 (정산 금액이 음수인 경우, 다음 달 정산에서 차감)

    @Builder
    private Settlement(User creator, int year, int month,   
                        long totalSales, long totalRefunds, long netSales,
                        long feeAmount, long settlementAmount,
                        int salesCount, int cancelCount, long carryOverAmount) {
        this.creator = creator;
        this.year = year;
        this.month = month;
        this.totalSales = totalSales;
        this.totalRefunds = totalRefunds;
        this.netSales = netSales;
        this.feeAmount = feeAmount;
        this.settlementAmount = settlementAmount;
        this.salesCount = salesCount;
        this.cancelCount = cancelCount;
        this.status = SettlementStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.carryOverAmount = carryOverAmount;
    }

    public void confirm() {
        if (this.status != SettlementStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_SETTLEMENT_STATUS);
        }
        this.status = SettlementStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void pay() {
        if (this.status != SettlementStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_SETTLEMENT_STATUS);
        }
        this.status = SettlementStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }
}