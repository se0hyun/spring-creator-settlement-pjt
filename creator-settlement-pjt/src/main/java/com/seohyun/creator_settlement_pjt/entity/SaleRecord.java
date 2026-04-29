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
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sale_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA용
public class SaleRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User student;

    @Column(name="paid_amount", nullable = false)
    private long paidAmount;

    @Column(name="paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(name="fee_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal feeRate;

    @OneToOne(mappedBy = "saleRecord", fetch = FetchType.LAZY)
    private CancelRecord cancelRecord;

    @Builder
    private SaleRecord(Course course, User student, long paidAmount, 
                        LocalDateTime paidAt, BigDecimal feeRate) {
        this.course = course;
        this.student = student;
        this.paidAmount = paidAmount;
        this.paidAt = paidAt;
        this.feeRate = feeRate;
    }
}