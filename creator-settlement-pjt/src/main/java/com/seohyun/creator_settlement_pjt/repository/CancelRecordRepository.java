package com.seohyun.creator_settlement_pjt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.seohyun.creator_settlement_pjt.entity.CancelRecord;
import com.seohyun.creator_settlement_pjt.entity.SaleRecord;
import com.seohyun.creator_settlement_pjt.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CancelRecordRepository extends JpaRepository<CancelRecord, Long> {

    boolean existsBySaleRecord(SaleRecord saleRecord); // 해당 판매 기록에 대한 취소 기록이 존재하는지 확인
    List<CancelRecord> findBySaleRecordCourseCreatorAndCanceledAtBetween(User creator, LocalDateTime startAt, LocalDateTime endAt); // cancelAmount, cancelCount

    @Query("SELECT COALESCE(SUM(c.cancelAmount), 0) FROM CancelRecord c " +
       "WHERE c.saleRecord.course.creator = :creator AND c.canceledAt BETWEEN :start AND :end")
    long sumCancelAmountByCreatorAndRange(
        @Param("creator") User creator,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(c) FROM CancelRecord c " +
        "WHERE c.saleRecord.course.creator = :creator AND c.canceledAt BETWEEN :start AND :end")
    int countCancelByCreatorAndRange(
        @Param("creator") User creator,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
}
