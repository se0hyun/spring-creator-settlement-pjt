package com.seohyun.creator_settlement_pjt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.seohyun.creator_settlement_pjt.entity.Settlement;
import com.seohyun.creator_settlement_pjt.entity.User;
import com.seohyun.creator_settlement_pjt.entity.SettlementStatus;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    // 중복 정산 체크
    boolean existsByCreatorAndYearAndMonth(User creator, int year, int month);

    // 크리에이터의 정산 목록 조회 (최신순)
    List<Settlement> findAllByCreatorOrderByYearDescMonthDesc(User creator); // year, month 최신순

    // 관리자용 전체 정산 목록 (상태 필터 가능)
    List<Settlement> findAllByStatusOrderByYearDescMonthDesc(SettlementStatus status); // year, month 최신순

}
