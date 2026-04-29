package com.seohyun.creator_settlement_pjt.dto;

import com.seohyun.creator_settlement_pjt.entity.SaleRecord;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class SaleRecordResponseDTO {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long studentId;
    private String studentName;
    private long paidAmount;
    private LocalDateTime paidAt;
    private boolean cancelled;
    private Long cancelAmount;
    private LocalDateTime canceledAt;

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
                .cancelled(cancelled)
                .cancelAmount(cancelled ? saleRecord.getCancelRecord().getCancelAmount() : null)
                .canceledAt(cancelled ? saleRecord.getCancelRecord().getCanceledAt() : null)
                .build();
    }
}
