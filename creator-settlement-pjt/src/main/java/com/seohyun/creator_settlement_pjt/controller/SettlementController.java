package com.seohyun.creator_settlement_pjt.controller;

import com.seohyun.creator_settlement_pjt.dto.SettlementResponseDTO;
import com.seohyun.creator_settlement_pjt.dto.SettlementSummaryResponseDTO;
import com.seohyun.creator_settlement_pjt.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
@Tag(name = "2. Settlement", description = "(관리자, 크리에이터) 정산 API")
public class SettlementController {

    private final SettlementService settlementService;
    
        @PostMapping("/generate")
        @Operation(summary = "정산 수동 생성 (관리자)", description = "특정 연월에 대해 모든 크리에이터의 정산을 생성합니다.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "정산 생성 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 연월 값")
        })
        public ResponseEntity<Void> generate(
                @Parameter(description = "정산 연도 (예: 2026)", example = "2026") @RequestParam int year,
                @Parameter(description = "정산 월 (1~12)", example = "4") @RequestParam int month) {
            settlementService.generateSettlement(year, month);
            return ResponseEntity.ok().build();
        }

        @PostMapping("/{settlementId}/confirm")
        @Operation(summary = "정산 확정 (관리자)", description = "PENDING 상태의 정산을 CONFIRMED로 변경합니다.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "정산 확정 성공"),
                @ApiResponse(responseCode = "404", description = "정산을 찾을 수 없음"),
                @ApiResponse(responseCode = "400", description = "PENDING 상태가 아님")
        })
        public ResponseEntity<SettlementResponseDTO> confirm(
                @Parameter(description = "정산 ID") @PathVariable Long settlementId) {
            return ResponseEntity.ok(settlementService.confirm(settlementId));
        }
    
        @PostMapping("/{settlementId}/pay")
        @Operation(summary = "정산 지급 (관리자)", description = "CONFIRMED 상태의 정산을 PAID로 변경합니다.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "정산 지급 성공"),
                @ApiResponse(responseCode = "404", description = "정산을 찾을 수 없음"),
                @ApiResponse(responseCode = "400", description = "CONFIRMED 상태가 아님")
        })
        public ResponseEntity<SettlementResponseDTO> pay(
                @Parameter(description = "정산 ID") @PathVariable Long settlementId) {
            return ResponseEntity.ok(settlementService.pay(settlementId));
        }
        
    @GetMapping("/summary")
    @Operation(
            summary = "기간별 정산 집계", 
            description = "시작일~종료일 기간의 크리에이터별 정산 금액을 집계합니다. 월별로 독립 계산 후 합산하며, 정산금액 내림차순으로 정렬됩니다.(관리자)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "집계 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식")
    })
    public ResponseEntity<SettlementSummaryResponseDTO> getSummary(
            @Parameter(description = "조회 시작일", example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "조회 종료일", example = "2025-03-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(settlementService.getSummary(from, to));
    }

    @GetMapping("/creators/{creatorId}/monthly")
    @Operation(summary = "크리에이터별 특정 월 정산 조회 (크리에이터)", description = "크리에이터 본인의 특정 월 정산 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "크리에이터 또는 해당 월 정산을 찾을 수 없음")
    })
    public ResponseEntity<SettlementResponseDTO> getSettlementByMonth(
            @Parameter(description = "크리에이터 ID", example = "1") @PathVariable Long creatorId,
            @Parameter(description = "정산 연도", example = "2025") @RequestParam int year,
            @Parameter(description = "정산 월 (1~12)", example = "3") @RequestParam int month) {
        return ResponseEntity.ok(settlementService.getSettlementByMonth(creatorId, year, month));
    }

//     @GetMapping("/creators/{creatorId}")
//     @Operation(summary = "크리에이터별 특정 기간 정산 목록 조회", description = "크리에이터의 특정 기간 정산 내역을 최신순으로 조회합니다.")
//     @ApiResponses({
//             @ApiResponse(responseCode = "200", description = "조회 성공"),
//             @ApiResponse(responseCode = "404", description = "크리에이터를 찾을 수 없음")
//     })
//     public ResponseEntity<List<SettlementResponseDTO>> getSettlementsByCreator(
//             @Parameter(description = "크리에이터 ID") @PathVariable Long creatorId) {
//         return ResponseEntity.ok(settlementService.getSettlementsByCreator(creatorId));
//     }


}
