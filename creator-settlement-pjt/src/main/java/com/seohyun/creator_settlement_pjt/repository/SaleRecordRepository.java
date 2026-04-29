package com.seohyun.creator_settlement_pjt.repository;  

import org.springframework.data.jpa.repository.JpaRepository;
import com.seohyun.creator_settlement_pjt.entity.SaleRecord;
import com.seohyun.creator_settlement_pjt.entity.User;
import com.seohyun.creator_settlement_pjt.entity.Course;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaleRecordRepository extends JpaRepository<SaleRecord, Long> {
    boolean existsByStudentAndCourse(User student, Course course);
    List<SaleRecord> findByCourseCreatorOrderByPaidAtDesc(User creator);
    List<SaleRecord> findByCourseCreatorAndPaidAtBetweenOrderByPaidAtDesc(User creator, LocalDateTime startAt, LocalDateTime endAt);
    List<SaleRecord> findByCourseCreatorAndPaidAtBetween(User creator, LocalDateTime startAt, LocalDateTime endAt);

    @Query("SELECT COALESCE(SUM(s.paidAmount), 0) FROM SaleRecord s " +
       "WHERE s.course.creator = :creator AND s.paidAt BETWEEN :start AND :end")
    long sumPaidAmountByCreatorAndRange(
        @Param("creator") User creator,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(s) FROM SaleRecord s " +
        "WHERE s.course.creator = :creator AND s.paidAt BETWEEN :start AND :end")
    int countByCreatorAndRange(
        @Param("creator") User creator,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
}
