package com.seohyun.creator_settlement_pjt.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.seohyun.creator_settlement_pjt.dto.CancelRequestDTO;
import com.seohyun.creator_settlement_pjt.dto.CancelResponseDTO;
import com.seohyun.creator_settlement_pjt.dto.EnrollmentRequestDTO;
import com.seohyun.creator_settlement_pjt.dto.EnrollmentResponseDTO;
import com.seohyun.creator_settlement_pjt.service.EnrollmentService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Enrollment", description = "수강 및 취소 API")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/enrollments")
    @Operation(summary = "강의 수강", description = "학생이 강의를 구매합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "수강 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 (역할 불일치, 결제 금액 불일치, 중복 수강, 수수료율 없음 등)")
    })
    public ResponseEntity<EnrollmentResponseDTO> enroll(@Valid @RequestBody EnrollmentRequestDTO enrollmentRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentService.enroll(enrollmentRequestDTO));
    }

    @PostMapping("/sale-records/{saleRecordId}/cancel")
    @Operation(summary = "수강 취소", description = "판매 내역을 취소하고 환불 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 (중복 취소, 환불 금액 초과 등)")
    })
    public ResponseEntity<CancelResponseDTO> cancel(@PathVariable Long saleRecordId,
                                                    @Valid @RequestBody CancelRequestDTO cancelRequestDTO) {
        return ResponseEntity.ok(enrollmentService.cancel(saleRecordId, cancelRequestDTO));
    }
}
