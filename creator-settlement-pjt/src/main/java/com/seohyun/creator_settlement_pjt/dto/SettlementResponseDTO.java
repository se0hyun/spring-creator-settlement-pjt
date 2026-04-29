package com.seohyun.creator_settlement_pjt.dto;

import com.seohyun.creator_settlement_pjt.entity.Settlement;
import com.seohyun.creator_settlement_pjt.entity.SettlementStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class SettlementResponseDTO {

    private Long id;
    private Long creatorId;
    private String creatorName;
    private int year;
    private int month;
    private long totalSales;
    private long totalRefunds;
    private long netSales;
    private long feeAmount;
    private long settlementAmount;
    private long carryOverAmount;
    private int salesCount;
    private int cancelCount;
    private SettlementStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime paidAt;

    public static SettlementResponseDTO from(Settlement settlement) {
        return SettlementResponseDTO.builder()
                .id(settlement.getId())
                .creatorId(settlement.getCreator().getId())
                .creatorName(settlement.getCreator().getName())
                .year(settlement.getYear())
                .month(settlement.getMonth())
                .totalSales(settlement.getTotalSales())
                .totalRefunds(settlement.getTotalRefunds())
                .netSales(settlement.getNetSales())
                .feeAmount(settlement.getFeeAmount())
                .settlementAmount(settlement.getSettlementAmount())
                .carryOverAmount(settlement.getCarryOverAmount())
                .salesCount(settlement.getSalesCount())
                .cancelCount(settlement.getCancelCount())
                .status(settlement.getStatus())
                .createdAt(settlement.getCreatedAt())
                .confirmedAt(settlement.getConfirmedAt())
                .paidAt(settlement.getPaidAt())
                .build();
    }
}
