package com.seohyun.creator_settlement_pjt.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@JsonPropertyOrder({"courseId", "studentId", "paidAmount", "paidAt"})
public class EnrollmentRequestDTO {

    @NotNull
    @Schema(description = "강의 ID", example = "1")
    private Long courseId;

    @NotNull
    @Schema(description = "수강생 사용자 ID", example = "5")
    private Long studentId;

    @Positive
    @Schema(description = "결제 금액 (원)", example = "50000")
    private long paidAmount;    // 샘플 데이터에 따른 변경 -> 실제 서비스라면 삭제

    @NotNull
    @Schema(description = "결제 일시 (KST)", example = "2025-03-05T10:00:00")
    private LocalDateTime paidAt;

}
