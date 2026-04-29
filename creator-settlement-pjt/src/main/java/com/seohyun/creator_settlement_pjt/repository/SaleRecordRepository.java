package com.seohyun.creator_settlement_pjt.repository;  

import org.springframework.data.jpa.repository.JpaRepository;
import com.seohyun.creator_settlement_pjt.entity.SaleRecord;
import com.seohyun.creator_settlement_pjt.entity.User;
import com.seohyun.creator_settlement_pjt.entity.Course;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRecordRepository extends JpaRepository<SaleRecord, Long> {
    boolean existsByStudentAndCourse(User student, Course course);
    List<SaleRecord> findByCourseCreatorAndPaidAtBetween(User creator, LocalDateTime startAt, LocalDateTime endAt); // paidAmount, salesCount

}
