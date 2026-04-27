package com.seohyun.creator_settlement_pjt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CancelRequestDTO {

    @Positive
    @Max(value = 10_000_000, message = "환불 금액은 1,000만 원을 초과할 수 없습니다.")
    @Schema(description = "환불 금액 (원, 부분 환불 가능)", example = "50000")
    private long cancelAmount;

    @NotNull
    @Schema(description = "취소 일시 (KST)", example = "2025-02-01T10:00:00")
    private LocalDateTime canceledAt;

}
