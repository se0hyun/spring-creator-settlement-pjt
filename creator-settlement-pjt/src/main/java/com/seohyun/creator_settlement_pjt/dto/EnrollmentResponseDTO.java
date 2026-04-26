package com.seohyun.creator_settlement_pjt.dto;

import lombok.Getter;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@Builder
public class EnrollmentResponseDTO {

    private Long id;
    private String courseTitle;
    private String studentName;
    private int paidAmount;
    private LocalDateTime paidAt;

}
