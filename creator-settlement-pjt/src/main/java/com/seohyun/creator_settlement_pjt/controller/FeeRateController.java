package com.seohyun.creator_settlement_pjt.controller;

import com.seohyun.creator_settlement_pjt.dto.FeeRateCreateRequestDTO;
import com.seohyun.creator_settlement_pjt.dto.FeeRateResponseDTO;
import com.seohyun.creator_settlement_pjt.service.FeeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fee-rates")
@RequiredArgsConstructor
@Tag(name = "FeeRate", description = "(관리자) 플랫폼 수수료율 구간 등록·조회")
public class FeeRateController {

    private final FeeRateService feeRateService;

    @GetMapping("/current")
    @Operation(summary = "현재 유효 수수료 조회", description = "현재 시각 기준 활성 FeeRecord 한 건을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "유효한 수수료 없음")
    })
    public ResponseEntity<FeeRateResponseDTO> getCurrent() {
        return ResponseEntity.ok(feeRateService.getCurrent());
    }

    @PostMapping
    @Operation(
            summary = "수수료 구간 신규 등록",
            description = """
                    새 구간을 추가합니다. 동일한 effectiveFrom 시작 구간이 이미 있으면 409입니다.
                    기존 구간과 날짜가 겹치면 직전 구간의 end_at을 새 구간 시작 직전으로 줄입니다.
                    월별 정산·기간 집계는 해당 월 시작 시점의 유효 FeeRecord를 사용합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            @ApiResponse(responseCode = "409", description = "동일 시각 시작 구간 중복")
    })
    public ResponseEntity<FeeRateResponseDTO> register(@Valid @RequestBody FeeRateCreateRequestDTO dto) {
        FeeRateResponseDTO body = feeRateService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
