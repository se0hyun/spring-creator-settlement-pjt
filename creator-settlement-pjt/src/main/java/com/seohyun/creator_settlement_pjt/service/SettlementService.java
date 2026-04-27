package com.seohyun.creator_settlement_pjt.service;

import com.seohyun.creator_settlement_pjt.dto.SettlementResponseDTO;
import com.seohyun.creator_settlement_pjt.entity.CancelRecord;
import com.seohyun.creator_settlement_pjt.entity.FeeRecord;
import com.seohyun.creator_settlement_pjt.entity.Role;
import com.seohyun.creator_settlement_pjt.entity.SaleRecord;
import com.seohyun.creator_settlement_pjt.entity.Settlement;
import com.seohyun.creator_settlement_pjt.entity.User;
import com.seohyun.creator_settlement_pjt.exception.BusinessException;
import com.seohyun.creator_settlement_pjt.exception.ErrorCode;
import com.seohyun.creator_settlement_pjt.repository.CancelRecordRepository;
import com.seohyun.creator_settlement_pjt.repository.FeeRecordRepository;
import com.seohyun.creator_settlement_pjt.repository.SaleRecordRepository;
import com.seohyun.creator_settlement_pjt.repository.SettlementRepository;
import com.seohyun.creator_settlement_pjt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final SaleRecordRepository saleRecordRepository;
    private final CancelRecordRepository cancelRecordRepository;
    private final FeeRecordRepository feeRecordRepository;

    // 매달 1일 00:00 KST 에 전월 정산 자동 생성
    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul")
    public void scheduleMonthlySettlement() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        log.info("월별 정산 자동 실행: {}년 {}월", lastMonth.getYear(), lastMonth.getMonthValue());
        generateSettlement(lastMonth.getYear(), lastMonth.getMonthValue());
    }

    @Transactional
    public void generateSettlement(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        FeeRecord feeRecord = feeRecordRepository.findActiveFeeRate(LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(ErrorCode.FEE_RATE_NOT_FOUND));

        List<User> creators = userRepository.findAllByRole(Role.CREATOR);

        for (User creator : creators) {
            if (settlementRepository.existsByCreatorAndYearAndMonth(creator, year, month)) {
                log.info("정산 이미 존재 - creatorId: {}, {}년 {}월", creator.getId(), year, month);
                continue;
            }

            List<SaleRecord> sales = saleRecordRepository
                    .findByCourseCreatorAndPaidAtBetween(creator, start, end);
            List<CancelRecord> cancels = cancelRecordRepository
                    .findBySaleRecordCourseCreatorAndCanceledAtBetween(creator, start, end);

            if (sales.isEmpty() && cancels.isEmpty()) {
                continue;
            }

            long totalSales = sales.stream().mapToLong(SaleRecord::getPaidAmount).sum();
            long totalRefunds = cancels.stream().mapToLong(CancelRecord::getCancelAmount).sum();
            long netSales = totalSales - totalRefunds;

            long feeAmount = BigDecimal.valueOf(netSales)
                    .multiply(feeRecord.getFeeRate())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                    .longValue();
            long settlementAmount = netSales - feeAmount;

            settlementRepository.save(Settlement.builder()
                    .creator(creator)
                    .year(year)
                    .month(month)
                    .totalSales(totalSales)
                    .totalRefunds(totalRefunds)
                    .netSales(netSales)
                    .feeAmount(feeAmount)
                    .settlementAmount(settlementAmount)
                    .salesCount(sales.size())
                    .cancelCount(cancels.size())
                    .build());
        }
    }

    public List<SettlementResponseDTO> getSettlementsByCreator(Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return settlementRepository.findAllByCreatorOrderByYearDescMonthDesc(creator)
                .stream()
                .map(SettlementResponseDTO::from)
                .toList();
    }

    @Transactional
    public SettlementResponseDTO confirm(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));

        settlement.confirm();
        return SettlementResponseDTO.from(settlement);
    }

    @Transactional
    public SettlementResponseDTO pay(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));

        settlement.pay();
        return SettlementResponseDTO.from(settlement);
    }
}
