package com.seohyun.creator_settlement_pjt.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementSummaryItemDTO {

    private Long creatorId;
    private String creatorName;
    private long totalSales;
    private long totalRefunds;
    private long netSales;
    private long feeAmount;
    private long settlementAmount;
    private int salesCount;
    private int cancelCount;
}
