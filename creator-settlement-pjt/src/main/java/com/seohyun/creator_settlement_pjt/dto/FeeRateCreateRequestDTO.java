package com.seohyun.creator_settlement_pjt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class FeeRateCreateRequestDTO {

    @NotNull
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "100.00")
    @Schema(description = "플랫폼 수수료율 (%)", example = "25.00")
    private BigDecimal feeRate;

    @NotNull
    @Schema(description = "새 수수료가 적용되기 시작하는 일시 (KST)", example = "2026-06-01T00:00:00")
    private LocalDateTime effectiveFrom;
}
