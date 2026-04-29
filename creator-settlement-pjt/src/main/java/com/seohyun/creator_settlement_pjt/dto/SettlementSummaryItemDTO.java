package com.seohyun.creator_settlement_pjt.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
        "creatorId", "creatorName",
        "totalSales", "totalRefunds", "netSales",
        "feeAmount", "settlementAmount",
        "salesCount", "cancelCount"
})
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
