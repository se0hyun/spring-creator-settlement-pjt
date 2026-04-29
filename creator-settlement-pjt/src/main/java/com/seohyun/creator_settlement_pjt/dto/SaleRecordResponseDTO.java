package com.seohyun.creator_settlement_pjt.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.seohyun.creator_settlement_pjt.entity.SaleRecord;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonPropertyOrder({
        "id", "courseId", "courseTitle", "studentId", "studentName",
        "paidAmount", "paidAt",
        "feeRateAtSale",
        "cancelled", "cancelAmount", "canceledAt", "feeRateAtCancel"
})
public class SaleRecordResponseDTO {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long studentId;
    private String studentName;
    private long paidAmount;
    private LocalDateTime paidAt;
    /** 구매 결제 일시 기준 스냅샷 수수료율(%) */
    private BigDecimal feeRateAtSale;
    private boolean cancelled;
    private Long cancelAmount;
    private LocalDateTime canceledAt;
    /** 취소가 있으면 취소 일시 기준 스냅샷 수수료율(%) */
    private BigDecimal feeRateAtCancel;

    public static SaleRecordResponseDTO from(SaleRecord saleRecord) {
        boolean cancelled = saleRecord.getCancelRecord() != null;
        return SaleRecordResponseDTO.builder()
                .id(saleRecord.getId())
                .courseId(saleRecord.getCourse().getId())
                .courseTitle(saleRecord.getCourse().getTitle())
                .studentId(saleRecord.getStudent().getId())
                .studentName(saleRecord.getStudent().getName())
                .paidAmount(saleRecord.getPaidAmount())
                .paidAt(saleRecord.getPaidAt())
                .feeRateAtSale(saleRecord.getFeeRate())
                .cancelled(cancelled)
                .cancelAmount(cancelled ? saleRecord.getCancelRecord().getCancelAmount() : null)
                .canceledAt(cancelled ? saleRecord.getCancelRecord().getCanceledAt() : null)
                .feeRateAtCancel(cancelled ? saleRecord.getCancelRecord().getFeeRate() : null)
                .build();
    }
}
