package com.seohyun.creator_settlement_pjt.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.math.BigDecimal;
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

        // ── 크리에이터 (creator-1 ~ creator-3) ───────────────────────
        User creator1 = User.builder().name("김강사").role(Role.CREATOR).build();
        User creator2 = User.builder().name("이강사").role(Role.CREATOR).build();
        User creator3 = User.builder().name("박강사").role(Role.CREATOR).build();

        // ── 관리자 ────────────────────────────────────────────────────
        User manager = User.builder().name("관리자").role(Role.MANAGER).build();

        // ── 수강생 (student-1 ~ student-7) ───────────────────────────
        User student1 = User.builder().name("student-1").role(Role.STUDENT).build();
        User student2 = User.builder().name("student-2").role(Role.STUDENT).build();
        User student3 = User.builder().name("student-3").role(Role.STUDENT).build();
        User student4 = User.builder().name("student-4").role(Role.STUDENT).build();
        User student5 = User.builder().name("student-5").role(Role.STUDENT).build();
        User student6 = User.builder().name("student-6").role(Role.STUDENT).build();
        User student7 = User.builder().name("student-7").role(Role.STUDENT).build();

        em.persist(creator1); em.persist(creator2); em.persist(creator3);
        em.persist(manager);
        em.persist(student1); em.persist(student2); em.persist(student3);
        em.persist(student4); em.persist(student5); em.persist(student6);
        em.persist(student7);

        // ── 강의 (course-1 ~ course-4) ───────────────────────────────
        Course course1 = Course.builder().title("Spring Boot 입문").price(50000).creator(creator1).build();
        Course course2 = Course.builder().title("JPA 실전").price(80000).creator(creator1).build();
        Course course3 = Course.builder().title("Kotlin 기초").price(60000).creator(creator2).build();
        Course course4 = Course.builder().title("MSA 설계").price(120000).creator(creator3).build();

        em.persist(course1); em.persist(course2);
        em.persist(course3); em.persist(course4);

        // ── 수수료율: 2025 ~ 2026 커버 ───────────────────────────────
        FeeRecord feeRecord = FeeRecord.builder()
                .feeRate(BigDecimal.valueOf(20.00))
                .startAt(LocalDateTime.of(2025, 1, 1, 0, 0, 0))
                .endAt(LocalDateTime.of(2026, 12, 31, 23, 59, 59))
                .build();
        em.persist(feeRecord);

        BigDecimal feeRate = BigDecimal.valueOf(20.00);

        // ── 판매 내역 (가이드라인 7개 케이스) ────────────────────────
        // 케이스 1 — 정상 판매 (취소 없음)
        em.persist(SaleRecord.builder()
                .course(course1).student(student1).paidAmount(50000)
                .paidAt(LocalDateTime.of(2025, 3, 5, 10, 0, 0)).feeRate(feeRate).build());

        // 케이스 2 — 정상 판매 (취소 없음)
        em.persist(SaleRecord.builder()
                .course(course1).student(student2).paidAmount(50000)
                .paidAt(LocalDateTime.of(2025, 3, 15, 14, 30, 0)).feeRate(feeRate).build());

        // 케이스 3 — 전액 환불 대상 판매
        em.persist(SaleRecord.builder()
                .course(course2).student(student3).paidAmount(80000)
                .paidAt(LocalDateTime.of(2025, 3, 20, 9, 0, 0)).feeRate(feeRate).build());

        // 케이스 4 — 부분 환불 대상 판매 (환불 금액 ≠ 원결제 금액)
        em.persist(SaleRecord.builder()
                .course(course2).student(student4).paidAmount(80000)
                .paidAt(LocalDateTime.of(2025, 3, 22, 11, 0, 0)).feeRate(feeRate).build());

        // 케이스 5 — 월 경계: 1월 말 결제 → 2월 초 취소 예정
        em.persist(SaleRecord.builder()
                .course(course3).student(student5).paidAmount(60000)
                .paidAt(LocalDateTime.of(2025, 1, 31, 23, 30, 0)).feeRate(feeRate).build());

        // 케이스 6 — creator-2 정상 판매 (취소 없음)
        em.persist(SaleRecord.builder()
                .course(course3).student(student6).paidAmount(60000)
                .paidAt(LocalDateTime.of(2025, 3, 10, 16, 0, 0)).feeRate(feeRate).build());

        // 케이스 7 — creator-3 판매 (2월, 빈 월 조회 검증용)
        em.persist(SaleRecord.builder()
                .course(course4).student(student7).paidAmount(120000)
                .paidAt(LocalDateTime.of(2025, 2, 14, 10, 0, 0)).feeRate(feeRate).build());
    }
}
