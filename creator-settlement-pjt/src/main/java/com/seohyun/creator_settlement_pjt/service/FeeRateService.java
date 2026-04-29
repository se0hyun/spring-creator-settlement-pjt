package com.seohyun.creator_settlement_pjt.service;

import com.seohyun.creator_settlement_pjt.dto.FeeRateCreateRequestDTO;
import com.seohyun.creator_settlement_pjt.dto.FeeRateResponseDTO;
import com.seohyun.creator_settlement_pjt.entity.FeeRecord;
import com.seohyun.creator_settlement_pjt.exception.BusinessException;
import com.seohyun.creator_settlement_pjt.exception.ErrorCode;
import com.seohyun.creator_settlement_pjt.repository.FeeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeeRateService {

    /** 무기한 종료 구간 끝 (JPQL 유효 조회용 — 설계 문서와 동일 개념) */
    private static final LocalDateTime OPEN_END =
            LocalDateTime.of(2999, 12, 31, 23, 59, 59);

    private final FeeRecordRepository feeRecordRepository;

    public FeeRateResponseDTO getCurrent() {
        FeeRecord feeRecord = feeRecordRepository.findActiveFeeRate(LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(ErrorCode.FEE_RATE_NOT_FOUND));
        return FeeRateResponseDTO.from(feeRecord);
    }

    /**
     * 새 수수료 구간을 등록한다. 동일 시각을 시작으로 하는 구간이 이미 있으면 거부하고,
     * 그 외에는 기존 구간 중 {@code effectiveFrom}과 겹치는 행의 종료 시각을 자동으로 앞당긴다.
     */
    @Transactional
    public FeeRateResponseDTO register(FeeRateCreateRequestDTO dto) {
        LocalDateTime from = dto.getEffectiveFrom();
        BigDecimal rate = dto.getFeeRate();

        if (feeRecordRepository.existsByStartAt(from)) {
            throw new BusinessException(ErrorCode.FEE_RATE_SCHEDULE_CONFLICT);
        }

        feeRecordRepository.findActiveFeeRate(from.minusNanos(1)).ifPresent(prev -> {
            if (!prev.getEndAt().isBefore(from)) {
                try {
                    prev.truncateEndBeforeNextPeriodStarts(from);
                } catch (IllegalArgumentException e) {
                    throw new BusinessException(ErrorCode.INVALID_FEE_EFFECTIVE_FROM);
                }
            }
        });

        FeeRecord saved = feeRecordRepository.save(FeeRecord.builder()
                .feeRate(rate)
                .startAt(from)
                .endAt(OPEN_END)
                .build());

        return FeeRateResponseDTO.from(saved);
    }
}
