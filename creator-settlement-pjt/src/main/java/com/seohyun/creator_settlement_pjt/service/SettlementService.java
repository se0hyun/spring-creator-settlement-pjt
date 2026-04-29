package com.seohyun.creator_settlement_pjt.service;

import com.seohyun.creator_settlement_pjt.dto.SettlementResponseDTO;
import com.seohyun.creator_settlement_pjt.dto.SettlementSummaryItemDTO;
import com.seohyun.creator_settlement_pjt.dto.SettlementSummaryResponseDTO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
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

    public SettlementResponseDTO getSettlementByMonth(Long creatorId, int year, int month) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Settlement settlement = settlementRepository.findByCreatorAndYearAndMonth(creator, year, month)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));

        return SettlementResponseDTO.from(settlement);
    }

    public SettlementSummaryResponseDTO getSummary(LocalDate from, LocalDate to) {
        List<User> creators = userRepository.findAllByRole(Role.CREATOR);

        // from ~ to 를 월 구간 리스트로 분리
        List<YearMonth> months = new ArrayList<>();
        YearMonth current = YearMonth.from(from);
        YearMonth last = YearMonth.from(to);
        // current < last인지 검증
        if (current.isAfter(last)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
        while (!current.isAfter(last)) {
            months.add(current);
            current = current.plusMonths(1);
        }

        List<SettlementSummaryItemDTO> items = creators.stream()
                .map(creator -> calculateCreatorSummary(creator, from, to, months))
                .sorted(Comparator.comparingLong(SettlementSummaryItemDTO::getSettlementAmount).reversed()
                        .thenComparing(SettlementSummaryItemDTO::getCreatorName))
                .toList();

        long totalSettlementAmount = items.stream()
                .mapToLong(SettlementSummaryItemDTO::getSettlementAmount)
                .sum();

        return SettlementSummaryResponseDTO.builder()
                .from(from)
                .to(to)
                .items(items)
                .totalSettlementAmount(totalSettlementAmount)
                .build();
    }

    private SettlementSummaryItemDTO calculateCreatorSummary(
            User creator, LocalDate from, LocalDate to, List<YearMonth> months) {

        long totalSales = 0, totalRefunds = 0, totalFee = 0;
        int salesCount = 0, cancelCount = 0;

        for (YearMonth ym : months) {
            // 월 구간의 실제 시작/종료 (기간 경계 고려)
            LocalDateTime start = from.isAfter(ym.atDay(1)) ? from.atStartOfDay()
                    : ym.atDay(1).atStartOfDay();
            LocalDateTime end = to.isBefore(ym.atEndOfMonth()) ? to.atTime(23, 59, 59)
                    : ym.atEndOfMonth().atTime(23, 59, 59);

            List<SaleRecord> sales = saleRecordRepository
                    .findByCourseCreatorAndPaidAtBetween(creator, start, end);
            List<CancelRecord> cancels = cancelRecordRepository
                    .findBySaleRecordCourseCreatorAndCanceledAtBetween(creator, start, end);

            long monthSales = sales.stream().mapToLong(SaleRecord::getPaidAmount).sum();
            long monthRefunds = cancels.stream().mapToLong(CancelRecord::getCancelAmount).sum();
            long monthNet = monthSales - monthRefunds;

            // netSales < 0인 경우 feeAmount = 0, settlementAmount = 0 처리
            // TODO: 음수 netSales(환불 초과)는 다음 달 정산에서 차감하는 이월 로직 필요.
            //       현재는 0으로 처리하여 해당 월 손실분이 반영되지 않음.
            long monthFee = 0;
            if (monthNet > 0) {
                FeeRecord feeRecord = feeRecordRepository
                        .findActiveFeeRate(ym.atDay(1).atStartOfDay())
                        .orElseThrow(() -> new BusinessException(ErrorCode.FEE_RATE_NOT_FOUND));
                monthFee = BigDecimal.valueOf(monthNet)
                        .multiply(feeRecord.getFeeRate())
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                        .longValue();
            }

            totalSales += monthSales;
            totalRefunds += monthRefunds;
            totalFee += monthFee;
            salesCount += sales.size();
            cancelCount += cancels.size();
        }

        long netSales = totalSales - totalRefunds;
        long settlementAmount = Math.max(0, netSales - totalFee);

        return SettlementSummaryItemDTO.builder()
                .creatorId(creator.getId())
                .creatorName(creator.getName())
                .totalSales(totalSales)
                .totalRefunds(totalRefunds)
                .netSales(netSales)
                .feeAmount(totalFee)
                .settlementAmount(settlementAmount)
                .salesCount(salesCount)
                .cancelCount(cancelCount)
                .build();
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
