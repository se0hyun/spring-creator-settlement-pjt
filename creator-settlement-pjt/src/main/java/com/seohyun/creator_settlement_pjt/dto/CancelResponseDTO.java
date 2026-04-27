package com.seohyun.creator_settlement_pjt.dto;

import lombok.Getter;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@Builder
public class CancelResponseDTO {

    private Long id;
    private Long saleRecordId;
    private String courseTitle;
    private String studentName;
    private long paidAmount;
    private long cancelAmount;
    private LocalDateTime canceledAt;

}
