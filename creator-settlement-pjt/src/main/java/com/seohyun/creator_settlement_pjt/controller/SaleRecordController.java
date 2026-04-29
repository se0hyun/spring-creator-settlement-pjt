package com.seohyun.creator_settlement_pjt.controller;

import com.seohyun.creator_settlement_pjt.dto.SaleRecordResponseDTO;
import com.seohyun.creator_settlement_pjt.service.SaleRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sale-records")
@RequiredArgsConstructor
@Tag(name = "3. SaleRecord", description = "판매 내역 조회 API")
public class SaleRecordController {

    private final SaleRecordService saleRecordService;

    @GetMapping
    @Operation(
            summary = "판매 내역 목록 조회 (관리자)",
            description = "크리에이터 별 판매 내역을 결제일 최신순으로 조회합니다. from/to를 모두 입력하면 기간 필터가 적용됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "크리에이터를 찾을 수 없음")
    })
    public ResponseEntity<List<SaleRecordResponseDTO>> getSaleRecords(
            @Parameter(description = "크리에이터 ID", example = "1", required = true)
            @RequestParam Long creatorId,
            @Parameter(description = "조회 시작일 (KST, 포함)", example = "2025-03-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "조회 종료일 (KST, 포함)", example = "2025-03-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(saleRecordService.getSaleRecords(creatorId, from, to));
    }
}
