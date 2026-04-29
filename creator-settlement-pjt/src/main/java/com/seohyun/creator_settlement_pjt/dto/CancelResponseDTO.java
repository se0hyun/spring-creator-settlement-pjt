package com.seohyun.creator_settlement_pjt.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonPropertyOrder({"id", "saleRecordId", "courseTitle", "studentName", "paidAmount", "cancelAmount", "canceledAt", "feeRateAtCancel"})
public class CancelResponseDTO {

    private Long id;
    private Long saleRecordId;
    private String courseTitle;
    private String studentName;
    private long paidAmount;
    private long cancelAmount;
    private LocalDateTime canceledAt;
    /** 취소 일시 기준 유효했던 플랫폼 수수료율(%) 스냅샷 */
    private BigDecimal feeRateAtCancel;

}
