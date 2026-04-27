package com.seohyun.creator_settlement_pjt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.seohyun.creator_settlement_pjt.entity.CancelRecord;
import com.seohyun.creator_settlement_pjt.entity.SaleRecord;
import com.seohyun.creator_settlement_pjt.entity.User;
import java.time.LocalDateTime;
import java.util.List;

public interface CancelRecordRepository extends JpaRepository<CancelRecord, Long> {

    boolean existsBySaleRecord(SaleRecord saleRecord); // 해당 판매 기록에 대한 취소 기록이 존재하는지 확인
    List<CancelRecord> findBySaleRecordCourseCreatorAndCanceledAtBetween(User creator, LocalDateTime startAt, LocalDateTime endAt); // cancelAmount, cancelCount

}
