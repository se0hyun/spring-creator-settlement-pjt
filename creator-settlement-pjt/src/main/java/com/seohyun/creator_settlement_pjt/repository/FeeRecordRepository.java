package com.seohyun.creator_settlement_pjt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.seohyun.creator_settlement_pjt.entity.FeeRecord;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeeRecordRepository extends JpaRepository<FeeRecord, Long> {

    @Query("SELECT f FROM FeeRecord f WHERE f.startAt <= :now AND f.endAt >= :now ORDER BY id DESC LIMIT 1")
    Optional<FeeRecord> findActiveFeeRate(@Param("now") LocalDateTime now); // 현재 유효한 수수료 레코드 조회
}
