package com.seohyun.creator_settlement_pjt.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class SettlementSummaryResponseDTO {

    private LocalDate from;
    private LocalDate to;
    private List<SettlementSummaryItemDTO> items;
    private long totalSettlementAmount;
}
