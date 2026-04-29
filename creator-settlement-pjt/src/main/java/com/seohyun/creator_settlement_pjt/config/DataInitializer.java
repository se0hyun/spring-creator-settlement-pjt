package com.seohyun.creator_settlement_pjt.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

        // ── 크리에이터 ────────────────────────────────────────────────
        User creator1 = User.builder().name("김강사").role(Role.CREATOR).build();
        User creator2 = User.builder().name("이강사").role(Role.CREATOR).build();
        User creator3 = User.builder().name("박강사").role(Role.CREATOR).build();
        User manager  = User.builder().name("관리자").role(Role.MANAGER).build();

        em.persist(creator1); em.persist(creator2);
        em.persist(creator3); em.persist(manager);

        // ── 수강생 ────────────────────────────────────────────────────
        User student1  = User.builder().name("student-1").role(Role.STUDENT).build();
        User student2  = User.builder().name("student-2").role(Role.STUDENT).build();
        User student3  = User.builder().name("student-3").role(Role.STUDENT).build();
        User student4  = User.builder().name("student-4").role(Role.STUDENT).build();
        User student5  = User.builder().name("student-5").role(Role.STUDENT).build();
        User student6  = User.builder().name("student-6").role(Role.STUDENT).build();
        User student7  = User.builder().name("student-7").role(Role.STUDENT).build();
        User student8  = User.builder().name("student-8").role(Role.STUDENT).build();
        User student9  = User.builder().name("student-9").role(Role.STUDENT).build();
        User student10 = User.builder().name("student-10").role(Role.STUDENT).build();
        User student11 = User.builder().name("student-11").role(Role.STUDENT).build();
        User student12 = User.builder().name("student-12").role(Role.STUDENT).build();

        em.persist(student1);  em.persist(student2);  em.persist(student3);
        em.persist(student4);  em.persist(student5);  em.persist(student6);
        em.persist(student7);  em.persist(student8);  em.persist(student9);
        em.persist(student10); em.persist(student11); em.persist(student12);

        // ── 강의 ──────────────────────────────────────────────────────
        Course course1 = Course.builder().title("Spring Boot 입문").price(50000).creator(creator1).build();
        Course course2 = Course.builder().title("JPA 실전").price(80000).creator(creator1).build();
        Course course3 = Course.builder().title("Kotlin 기초").price(60000).creator(creator2).build();
        Course course4 = Course.builder().title("MSA 설계").price(120000).creator(creator3).build();

        em.persist(course1); em.persist(course2);
        em.persist(course3); em.persist(course4);

        // ── 수수료율: 2025 ~ 2026 전체 커버 ──────────────────────────
        FeeRecord feeRecord = FeeRecord.builder()
                .feeRate(BigDecimal.valueOf(20.00))
                .startAt(LocalDateTime.of(2025, 1, 1, 0, 0, 0))
                .endAt(LocalDateTime.of(2026, 12, 31, 23, 59, 59))
                .build();
        em.persist(feeRecord);
        BigDecimal feeRate = BigDecimal.valueOf(20.00);

        // ══════════════════════════════════════════════════════════════
        // 가이드라인 기본 7개 케이스
        // ══════════════════════════════════════════════════════════════

        // 케이스 1 — 김강사 3월 정상 판매 (취소 없음)
        // 3월 매출 기여: +50,000
        em.persist(SaleRecord.builder()
                .course(course1).student(student1).paidAmount(50000)
                .paidAt(LocalDateTime.of(2025, 3, 5, 10, 0, 0)).feeRate(feeRate).build());

        // 케이스 2 — 김강사 3월 정상 판매 (취소 없음)
        // 3월 매출 기여: +50,000
        em.persist(SaleRecord.builder()
                .course(course1).student(student2).paidAmount(50000)
                .paidAt(LocalDateTime.of(2025, 3, 15, 14, 30, 0)).feeRate(feeRate).build());

        // 케이스 3 — 김강사 3월 판매 → 동월 전액 환불
        // 3월 매출: +80,000 / 3월 환불: -80,000 → 3월 net 기여 0
        SaleRecord sale3 = SaleRecord.builder()
                .course(course2).student(student3).paidAmount(80000)
                .paidAt(LocalDateTime.of(2025, 3, 20, 9, 0, 0)).feeRate(feeRate).build();
        em.persist(sale3);
        em.persist(CancelRecord.builder()
                .saleRecord(sale3).cancelAmount(80000)
                .canceledAt(LocalDateTime.of(2025, 3, 25, 11, 0, 0))
                .feeRate(feeRate).build());

        // 케이스 4 — 김강사 3월 판매 → 익월 부분 환불
        // 3월 매출: +80,000 / 4월 환불: -40,000
        SaleRecord sale4 = SaleRecord.builder()
                .course(course2).student(student4).paidAmount(80000)
                .paidAt(LocalDateTime.of(2025, 3, 22, 11, 0, 0)).feeRate(feeRate).build();
        em.persist(sale4);
        em.persist(CancelRecord.builder()
                .saleRecord(sale4).cancelAmount(40000)
                .canceledAt(LocalDateTime.of(2025, 4, 5, 9, 0, 0))
                .feeRate(feeRate).build());

        // 케이스 5 — 이강사 월 경계: 1월 말 판매 → 2월 초 전액 취소
        // 1월 매출: +60,000 / 2월 환불: -60,000
        // → 2월 단독 조회 시 net = -60,000 (carryOver 이월 테스트용)
        SaleRecord sale5 = SaleRecord.builder()
                .course(course3).student(student5).paidAmount(60000)
                .paidAt(LocalDateTime.of(2025, 1, 31, 23, 30, 0)).feeRate(feeRate).build();
        em.persist(sale5);
        em.persist(CancelRecord.builder()
                .saleRecord(sale5).cancelAmount(60000)
                .canceledAt(LocalDateTime.of(2025, 2, 3, 10, 0, 0))
                .feeRate(feeRate).build());

        // 케이스 6 — 이강사 3월 정상 판매 (취소 없음)
        // 3월 매출 기여: +60,000
        SaleRecord sale6 = SaleRecord.builder()
                .course(course3).student(student6).paidAmount(60000)
                .paidAt(LocalDateTime.of(2025, 3, 10, 16, 0, 0)).feeRate(feeRate).build();
        em.persist(sale6);

        // 케이스 7 — 박강사 2월 판매 (빈 월 조회 검증용)
        // 박강사는 2월에만 데이터 있음 → 1·3월 조회 시 비어있어야 함
        em.persist(SaleRecord.builder()
                .course(course4).student(student7).paidAmount(120000)
                .paidAt(LocalDateTime.of(2025, 2, 14, 10, 0, 0)).feeRate(feeRate).build());

        // ══════════════════════════════════════════════════════════════
        // 추가 케이스: 다월 집계 · carryOver 이월 · 정렬 검증
        // ══════════════════════════════════════════════════════════════

        // 추가-1 — 김강사 4월 정상 판매
        // 4월 net: +50,000(sale8) - 40,000(케이스4 익월 환불) = +10,000
        // → fee = 2,000(DOWN) / settlement = 8,000
        em.persist(SaleRecord.builder()
                .course(course1).student(student8).paidAmount(50000)
                .paidAt(LocalDateTime.of(2025, 4, 8, 10, 0, 0)).feeRate(feeRate).build());

        // 추가-2 — 이강사 3월 두 번째 판매 → 동월 전액 취소
        // 3월 net: 60,000(sale6) + 60,000(sale9) - 60,000(cancel9) = 60,000
        SaleRecord sale9 = SaleRecord.builder()
                .course(course3).student(student9).paidAmount(60000)
                .paidAt(LocalDateTime.of(2025, 3, 18, 14, 0, 0)).feeRate(feeRate).build();
        em.persist(sale9);
        em.persist(CancelRecord.builder()
                .saleRecord(sale9).cancelAmount(60000)
                .canceledAt(LocalDateTime.of(2025, 3, 28, 9, 0, 0))
                .feeRate(feeRate).build());

        // 추가-3 — 이강사 음수 netSales 시나리오 (carryOver 발생)
        // 4월 판매: 없음 / 4월 취소: sale6(3월 판매) 취소 60,000
        // 4월 net = -60,000 → feeAmount=0, settlement=0, 5월로 -60,000 이월
        em.persist(CancelRecord.builder()
                .saleRecord(sale6).cancelAmount(60000)
                .canceledAt(LocalDateTime.of(2025, 4, 10, 11, 0, 0))
                .feeRate(feeRate).build());

        // 추가-4 — 이강사 5월 판매 2건 (carryOver 흡수 후 잔여 정산 검증)
        // 5월 매출: 120,000 / effectiveNet = 120,000 - 60,000(carryOver) = 60,000
        // → fee = 12,000 / settlement = 48,000
        em.persist(SaleRecord.builder()
                .course(course3).student(student10).paidAmount(60000)
                .paidAt(LocalDateTime.of(2025, 5, 7, 10, 0, 0)).feeRate(feeRate).build());
        em.persist(SaleRecord.builder()
                .course(course3).student(student11).paidAmount(60000)
                .paidAt(LocalDateTime.of(2025, 5, 20, 15, 0, 0)).feeRate(feeRate).build());

        // 추가-5 — 박강사 3월 · 4월 판매 (다월 집계 및 정렬 검증)
        // 3월 net: +120,000 / 4월 net: +120,000
        em.persist(SaleRecord.builder()
                .course(course4).student(student8).paidAmount(120000)
                .paidAt(LocalDateTime.of(2025, 3, 12, 13, 0, 0)).feeRate(feeRate).build());
        SaleRecord sale12 = SaleRecord.builder()
                .course(course4).student(student12).paidAmount(120000)
                .paidAt(LocalDateTime.of(2025, 4, 20, 10, 0, 0)).feeRate(feeRate).build();
        em.persist(sale12);

        // 추가-6 — 박강사 5월 취소 초과 시나리오 (carryOver 발생)
        // 5월 판매: 없음 / 5월 취소: sale12(4월 판매) 부분 환불 60,000
        // 5월 net = -60,000 → settlement=0, 6월로 -60,000 이월
        em.persist(CancelRecord.builder()
                .saleRecord(sale12).cancelAmount(60000)
                .canceledAt(LocalDateTime.of(2025, 5, 2, 9, 0, 0))
                .feeRate(feeRate).build());
    }
}
