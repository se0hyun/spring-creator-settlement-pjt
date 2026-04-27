package com.seohyun.creator_settlement_pjt.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.seohyun.creator_settlement_pjt.entity.CancelRecord;
import com.seohyun.creator_settlement_pjt.entity.Course;
import com.seohyun.creator_settlement_pjt.entity.FeeRecord;
import com.seohyun.creator_settlement_pjt.entity.Role;
import com.seohyun.creator_settlement_pjt.entity.SaleRecord;
import com.seohyun.creator_settlement_pjt.entity.User;

@Component
public class DataInitializer implements ApplicationRunner {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User creator1 = User.builder()
                .name("홍길동")
                .role(Role.CREATOR)
                .build();

        User creator2 = User.builder()
                .name("김영희")
                .role(Role.CREATOR)
                .build();
        
        User creator3 = User.builder()
                .name("이철수")
                .role(Role.CREATOR)
                .build();

        User manager = User.builder()
                .name("관리자")
                .role(Role.MANAGER)
                .build();
        User student1 = User.builder()
                .name("학생1")
                .role(Role.STUDENT)
                .build();
        User student2 = User.builder()
                .name("학생2")
                .role(Role.STUDENT)
                .build();
        User student3 = User.builder()
                .name("학생3")
                .role(Role.STUDENT)
                .build();
        User student4 = User.builder()
                .name("학생4")
                .role(Role.STUDENT)
                .build();
                User student5 = User.builder()
                .name("학생5")
                .role(Role.STUDENT)
                .build();
        User student6 = User.builder()
                .name("학생6")
                .role(Role.STUDENT)
                .build();

        em.persist(creator1);
        em.persist(creator2);
        em.persist(creator3);
        em.persist(manager);
        em.persist(student1);
        em.persist(student2);
        em.persist(student3);
        em.persist(student4);
        em.persist(student5);
        em.persist(student6);
        
        Course course1 = Course.builder()
                .title("Spring Boot 입문")
                .price(50000)
                .creator(creator1)
                .build();

        Course course2 = Course.builder()
                .title("JPA 실전")
                .price(70000)
                .creator(creator1)
                .build();

        Course course3 = Course.builder()
                .title("React 기초")
                .price(45000)
                .creator(creator2)
                .build();

        Course course4 = Course.builder()
                .title("C# 기초")
                .price(20000)
                .creator(creator3)
                .build();

        em.persist(course1);
        em.persist(course2);
        em.persist(course3);
        em.persist(course4);
        
        FeeRecord feeRecord1 = FeeRecord.builder()
                .feeRate(BigDecimal.valueOf(20.0))
                .startAt(LocalDateTime.of(2026, 1, 1, 0, 0, 0))
                .endAt(LocalDateTime.of(2026, 12, 31, 23, 59, 59))
                .build();

        em.persist(feeRecord1);

        BigDecimal feeRate = BigDecimal.valueOf(20.0);

        // ── 2026년 2월 구매 내역 ──────────────────────────────────────
        SaleRecord sale1 = SaleRecord.builder()
                .course(course1).student(student1).paidAmount(50000)
                .paidAt(LocalDateTime.of(2026, 2, 5, 10, 0)).feeRate(feeRate).build();
        SaleRecord sale2 = SaleRecord.builder()
                .course(course1).student(student2).paidAmount(50000)
                .paidAt(LocalDateTime.of(2026, 2, 10, 14, 0)).feeRate(feeRate).build();
        SaleRecord sale3 = SaleRecord.builder()
                .course(course1).student(student3).paidAmount(50000)
                .paidAt(LocalDateTime.of(2026, 2, 15, 9, 0)).feeRate(feeRate).build();
        SaleRecord sale4 = SaleRecord.builder()
                .course(course2).student(student4).paidAmount(70000)
                .paidAt(LocalDateTime.of(2026, 2, 20, 11, 0)).feeRate(feeRate).build();
        SaleRecord sale5 = SaleRecord.builder()
                .course(course3).student(student5).paidAmount(45000)
                .paidAt(LocalDateTime.of(2026, 2, 8, 15, 0)).feeRate(feeRate).build();
        SaleRecord sale6 = SaleRecord.builder()
                .course(course3).student(student1).paidAmount(45000)
                .paidAt(LocalDateTime.of(2026, 2, 12, 16, 0)).feeRate(feeRate).build();
        SaleRecord sale7 = SaleRecord.builder()
                .course(course4).student(student6).paidAmount(20000)
                .paidAt(LocalDateTime.of(2026, 2, 18, 13, 0)).feeRate(feeRate).build();

        em.persist(sale1); em.persist(sale2); em.persist(sale3);
        em.persist(sale4); em.persist(sale5); em.persist(sale6); em.persist(sale7);

        // 2월 취소: student3의 course1 환불
        CancelRecord cancel1 = CancelRecord.builder()
                .saleRecord(sale3).cancelAmount(50000)
                .canceledAt(LocalDateTime.of(2026, 2, 20, 10, 0)).build();
        em.persist(cancel1);

        // ── 2026년 3월 구매 내역 ──────────────────────────────────────
        SaleRecord sale8 = SaleRecord.builder()
                .course(course1).student(student5).paidAmount(50000)
                .paidAt(LocalDateTime.of(2026, 3, 5, 10, 0)).feeRate(feeRate).build();
        SaleRecord sale9 = SaleRecord.builder()
                .course(course1).student(student6).paidAmount(50000)
                .paidAt(LocalDateTime.of(2026, 3, 10, 12, 0)).feeRate(feeRate).build();
        SaleRecord sale10 = SaleRecord.builder()
                .course(course2).student(student1).paidAmount(70000)
                .paidAt(LocalDateTime.of(2026, 3, 22, 9, 0)).feeRate(feeRate).build();
        SaleRecord sale11 = SaleRecord.builder()
                .course(course3).student(student2).paidAmount(45000)
                .paidAt(LocalDateTime.of(2026, 3, 15, 17, 0)).feeRate(feeRate).build();
        SaleRecord sale12 = SaleRecord.builder()
                .course(course4).student(student3).paidAmount(20000)
                .paidAt(LocalDateTime.of(2026, 3, 8, 11, 0)).feeRate(feeRate).build();

        em.persist(sale8); em.persist(sale9); em.persist(sale10);
        em.persist(sale11); em.persist(sale12);

        // 3월 취소: student6의 course1 환불
        CancelRecord cancel2 = CancelRecord.builder()
                .saleRecord(sale9).cancelAmount(50000)
                .canceledAt(LocalDateTime.of(2026, 3, 18, 14, 0)).build();
        em.persist(cancel2);

        // ── 2026년 4월 구매 내역 ──────────────────────────────────────
        SaleRecord sale13 = SaleRecord.builder()
                .course(course1).student(student4).paidAmount(50000)
                .paidAt(LocalDateTime.of(2026, 4, 5, 10, 0)).feeRate(feeRate).build();
        SaleRecord sale14 = SaleRecord.builder()
                .course(course2).student(student2).paidAmount(70000)
                .paidAt(LocalDateTime.of(2026, 4, 15, 13, 0)).feeRate(feeRate).build();
        SaleRecord sale15 = SaleRecord.builder()
                .course(course3).student(student6).paidAmount(45000)
                .paidAt(LocalDateTime.of(2026, 4, 10, 16, 0)).feeRate(feeRate).build();

        em.persist(sale13); em.persist(sale14); em.persist(sale15);

        // 4월 취소: student2의 course2 환불
        CancelRecord cancel3 = CancelRecord.builder()
                .saleRecord(sale14).cancelAmount(70000)
                .canceledAt(LocalDateTime.of(2026, 4, 20, 11, 0)).build();
        em.persist(cancel3);
    }
}
