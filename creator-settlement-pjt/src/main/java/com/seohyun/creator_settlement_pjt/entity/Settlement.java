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
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "settlements",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "year", "month"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User creator;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int totalSales;

    @Column(nullable = false)
    private int totalRefunds;

    @Column(nullable = false)
    private int netSales;

    @Column(nullable = false)
    private int feeAmount;

    @Column(nullable = false)
    private int settlementAmount;

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

    @Builder
    private Settlement(User creator, int year, int month,
                        int totalSales, int totalRefunds, int netSales,
                        int feeAmount, int settlementAmount,
                        int salesCount, int cancelCount) {
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
    }

    public void confirm() {
        this.status = SettlementStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void pay() {
        this.status = SettlementStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }
}