package com.seohyun.creator_settlement_pjt.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonPropertyOrder({"id", "saleRecordId", "courseTitle", "studentName", "paidAmount", "cancelAmount", "canceledAt"})
public class CancelResponseDTO {

    private Long id;
    private Long saleRecordId;
    private String courseTitle;
    private String studentName;
    private long paidAmount;
    private long cancelAmount;
    private LocalDateTime canceledAt;

}
