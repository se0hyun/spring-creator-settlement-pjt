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
import com.seohyun.creator_settlement_pjt.entity.Role;
import com.seohyun.creator_settlement_pjt.entity.User;
import com.seohyun.creator_settlement_pjt.entity.FeeRecord;

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
                .feeRate(BigDecimal.valueOf(0.20))
                .startAt(LocalDateTime.of(2026, 1, 1, 0, 0, 0))
                .endAt(LocalDateTime.of(2026, 12, 31, 23, 59, 59))
                .build();

        em.persist(feeRecord1);
    }
}
