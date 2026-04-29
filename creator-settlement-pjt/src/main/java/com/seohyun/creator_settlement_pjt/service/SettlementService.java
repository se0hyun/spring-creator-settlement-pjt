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
        if (year > LocalDateTime.now().getYear() || month > 12 || month < 1) {       // 날짜 유효성 검증    ex) 2099년 13월
            throw new BusinessException(ErrorCode.INVALID_YEAR_MONTH_VALUE);
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        FeeRecord feeRecord = feeRecordRepository.findActiveFeeRate(start)
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

            // 이번 달 거래가 없고 이월할 금액도 없으면 정산 레코드 불필요
            // (이월 여부는 이전 달 조회 전까지 알 수 없으므로, 아래에서 판단)

            long totalSales = sales.stream().mapToLong(SaleRecord::getPaidAmount).sum();
            long totalRefunds = cancels.stream().mapToLong(CancelRecord::getCancelAmount).sum();
            long netSales = totalSales - totalRefunds;

            // 이전 달 정산에서 미정산 손실(carryOver)을 가져옴
            // effectiveNet = 이번 달 순매출 + 이전 달에서 이월된 미정산 손실(0 이하)
            YearMonth prevMonth = yearMonth.minusMonths(1);
            long prevCarryOver = settlementRepository
                    .findByCreatorAndYearAndMonth(creator, prevMonth.getYear(), prevMonth.getMonthValue())
                    .map(prev -> Math.min(0L, prev.getNetSales() + prev.getCarryOverAmount()))
                    .orElse(0L);

            long effectiveNet = netSales + prevCarryOver;

            // 거래도 없고 이전 달 이월도 없으면 정산 생략
            if (sales.isEmpty() && cancels.isEmpty() && prevCarryOver == 0) {
                continue;
            }

            long feeAmount = 0;
            long settlementAmount = 0;
            if (effectiveNet > 0) {
                // effectiveNet 기준으로 수수료 계산 (버림 처리)
                feeAmount = BigDecimal.valueOf(effectiveNet)
                    .multiply(feeRecord.getFeeRate())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                    .longValue();
                settlementAmount = effectiveNet - feeAmount;
            }
            // effectiveNet <= 0 이면 feeAmount=0, settlementAmount=0 유지
            // 음수 effectiveNet은 다음 달에 prevCarryOver로 전달됨

            settlementRepository.save(Settlement.builder()
                    .creator(creator)
                    .year(year)
                    .month(month)
                    .totalSales(totalSales)
                    .totalRefunds(totalRefunds)
                    .netSales(netSales)
                    .feeAmount(feeAmount)
                    .settlementAmount(settlementAmount)
                    .carryOverAmount(prevCarryOver)   // 이번 달에 적용된 이월액 (0 이하)
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

        long totalSales = 0, totalRefunds = 0, totalFee = 0, totalSettlement = 0;
        int salesCount = 0, cancelCount = 0;

        // 월 루프 간 미정산 손실을 연속으로 이월하기 위한 누적 변수
        // effectiveNet이 음수였던 달은 그 값을 다음 달로 전달
        long runningCarryOver = 0;

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

            // effectiveNet: 이번 달 순매출 + 이전 달에서 이월된 미정산 손실
            long effectiveNet = monthNet + runningCarryOver;

            long monthFee = 0;
            long monthSettlement = 0;
            if (effectiveNet > 0) {
                // effectiveNet 기준으로 수수료 계산 (버림 처리)
                FeeRecord feeRecord = feeRecordRepository
                        .findActiveFeeRate(ym.atDay(1).atStartOfDay())
                        .orElseThrow(() -> new BusinessException(ErrorCode.FEE_RATE_NOT_FOUND));
                monthFee = BigDecimal.valueOf(effectiveNet)
                        .multiply(feeRecord.getFeeRate())
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                        .longValue();
                monthSettlement = effectiveNet - monthFee;
                runningCarryOver = 0;       // 손실 흡수 완료, 이월 리셋
            } else {
                runningCarryOver = effectiveNet;    // 음수 effectiveNet을 다음 달로 이월
            }

            totalSales += monthSales;
            totalRefunds += monthRefunds;
            totalFee += monthFee;
            totalSettlement += monthSettlement;
            salesCount += sales.size();
            cancelCount += cancels.size();
        }

        long netSales = totalSales - totalRefunds;
        long settlementAmount = totalSettlement;

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
