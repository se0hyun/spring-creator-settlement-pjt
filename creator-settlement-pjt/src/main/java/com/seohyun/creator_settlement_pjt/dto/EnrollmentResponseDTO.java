package com.seohyun.creator_settlement_pjt.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonPropertyOrder({"id", "courseTitle", "studentName", "paidAmount", "paidAt"})
public class EnrollmentResponseDTO {

    private Long id;
    private String courseTitle;
    private String studentName;
    private long paidAmount;
    private LocalDateTime paidAt;

}
