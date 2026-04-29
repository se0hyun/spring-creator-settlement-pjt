package com.seohyun.creator_settlement_pjt.dto;

import com.seohyun.creator_settlement_pjt.entity.FeeRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class FeeRateResponseDTO {

    private Long id;

    @Schema(description = "수수료율 (%)")
    private BigDecimal feeRate;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public static FeeRateResponseDTO from(FeeRecord entity) {
        return FeeRateResponseDTO.builder()
                .id(entity.getId())
                .feeRate(entity.getFeeRate())
                .startAt(entity.getStartAt())
                .endAt(entity.getEndAt())
                .build();
    }
}
