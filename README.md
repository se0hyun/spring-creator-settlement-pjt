# spring-creator-settlement-pjt

## 프로젝트 개요

크리에이터(강사)의 강의 판매 및 환불 내역을 기반으로 월별 정산을 자동/수동 생성하고 관리하는 백엔드 서비스입니다.

수강생의 강의 구매·취소부터 크리에이터별 월 정산 생성, 상태 관리(확정/지급), 운영자용 기간별 집계까지의 흐름을 구현합니다.

---

## 기술 스택

| 항목 | 내용 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.14 |
| ORM | Spring Data JPA (Hibernate) |
| Database | H2 (In-Memory, MySQL 모드) |
| API 문서 | Springdoc OpenAPI (Swagger UI) 2.8.0 |
| 빌드 도구 | Gradle |
| 기타 | Lombok, Spring Validation |

---

## 실행 방법

```bash
git clone https://github.com/se0hyun/spring-creator-settlement-pjt.git
cd spring-creator-settlement-pjt/creator-settlement-pjt
./gradlew bootRun
```

애플리케이션 시작 시 `DataInitializer`가 실행되어 샘플 데이터(크리에이터 3명, 강의 4개, 수강생 12명, 판매/취소 내역)가 자동으로 삽입됩니다.

### 접속 정보

| 서비스 | URL |
|--------|-----|
| Swagger UI | http://localhost:8080/api/v1/swagger-ui/index.html |
| H2 Console | http://localhost:8080/api/v1/h2-console |

**H2 Console 접속 정보**
- JDBC URL: `jdbc:h2:mem:settlement`
- Username: `sa`
- Password: (없음)

---

## 요구사항 해석 및 가정

### 인증/인가
- 과제 지침에 따라 Spring Security / JWT 없이 구현합니다.
- `userId`를 요청 파라미터 또는 바디로 전달하는 방식으로 대체합니다.
- 실서비스에서는 JWT 기반 인증 미들웨어를 통해 요청자 식별 및 권한 검증이 이루어져야 합니다.

### 결제 금액 (`paidAmount`)
- 수강 등록 요청 시 `paidAmount`를 클라이언트가 명시적으로 전달합니다.
- 서버에서 해당 강의의 실제 가격(`course.price`)과 일치하는지 검증합니다.
- 불일치 시 `INVALID_PAYMENT_AMOUNT (400)` 에러를 반환합니다.

### `paidAt` / `canceledAt` 직접 입력 허용
실제 프로덕션에서는 `paidAt`, `canceledAt`을 서버에서 생성해야 하나, 정산 집계 로직의 월별 엣지 케이스(월 경계, 과거 데이터 등) 테스트를 위해 의도적으로 클라이언트에서 설정 가능하도록 노출했습니다.

추후 프로덕션 전환 시에는 두 필드를 요청 DTO에서 제거하고 서버에서 `LocalDateTime.now()`로 자동 생성해야 합니다.

### 정산 기준 (KST 기준 월 경계)
- **판매**: `paidAt` 기준으로 해당 월 정산에 포함
- **취소**: `canceledAt` 기준으로 해당 월 정산에 포함
- 월 경계: `1일 00:00:00 ~ 말일 23:59:59 (KST)`
- 예) 1월 31일 결제 → 1월 정산 포함 / 2월 3일 취소 → 2월 정산에서 환불 차감

### 수수료 계산
- 수수료율은 `FeeRecord` 테이블에서 유효 기간 기반으로 조회합니다 (플랫폼 단일 수수료율).
- 수수료 = `effectiveNet × feeRate(%) / 100`, **버림(RoundingMode.DOWN)** 처리
- 수수료율은 백분율 저장 (예: `20.00` = 20%)

---

## 설계 결정과 이유

### 1. 월간 음수 순매출 carryOver 이월
환불 초과로 순매출이 음수인 달이 발생하면, 해당 손실을 다음 달 정산에 이월하여 차감합니다.

```
netSales     = 이번 달 판매 합계 - 이번 달 환불 합계
carryOver    = 이전 달 effectiveNet이 음수였던 경우 그 값 (0 이하)
effectiveNet = netSales + carryOver  →  실제 지급 기준 금액
```

- `effectiveNet > 0`: 수수료 계산, 정산금액 = effectiveNet - 수수료
- `effectiveNet ≤ 0`: 수수료 = 0, 정산금액 = 0, 다음 달로 이월 발생
- `Settlement.carryOverAmount`: 이번 달에 적용된 이전 달 이월액 (투명하게 응답에 노출)

### 2. 운영자용 기간별 집계 API 설계
정산 상태(PENDING/CONFIRMED/PAID)와 무관하게 `SaleRecord` / `CancelRecord`를 실시간으로 집계합니다.

- 지정 기간을 월 단위로 분리 → 각 월 독립 계산 → 크리에이터별 합산
- carryOver 이월도 월 루프 내 `runningCarryOver`로 반영
- 정산금액 내림차순 → 동일 금액 시 이름 오름차순 정렬

> 추후 뎁스가 한 단계 깊어진다면, 이미 확정된 정산(`CONFIRMED`, `PAID`)은 `Settlement` 테이블에서 가져오고, 미확정 기간만 실시간 계산하는 방식으로 전환할 수 있습니다.

### 3. H2 예약어 충돌 대응
H2에서 `YEAR`, `MONTH`는 SQL 예약어로 컬럼명으로 사용 시 오류가 발생합니다.
`Settlement` 엔티티에서 `@Column(name = "settlement_year")` / `@Column(name = "settlement_month")`로 명시하여 해결했습니다.

### 4. 금액 필드 `long` 타입 통일
`int` 최대값(약 21억)을 초과하는 금액 처리를 위해 모든 금액 필드를 `long`으로 통일했습니다.
API 요청값에 `@Max(10_000_000)` 제한과 Swagger `@Schema(example = "...")` 기본값을 함께 적용하였습니다.

### 5. Entity Builder 명시적 private 생성자
`@AllArgsConstructor(PRIVATE) + @Builder` 조합 대신 명시적 `@Builder private 생성자`를 사용하여 `id` 필드가 Builder에 노출되는 문제를 방지했습니다.

### 6. 중복 정산 방지 — UniqueConstraint
`Settlement` 테이블에 `(user_id, settlement_year, settlement_month)` 복합 UniqueConstraint를 적용하여 동일 크리에이터의 동월 정산이 중복 생성되는 것을 DB 레벨에서 방지합니다.

```java
@Table(name = "settlements",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"user_id", "settlement_year", "settlement_month"}))
```

수동 생성 API(`POST /settlements/generate`)는 이미 정산이 존재하는 크리에이터를 건너뛰고, 아직 생성되지 않은 크리에이터만 처리합니다. 따라서 동일 연월로 여러 번 호출해도 중복 정산이 발생하지 않습니다.

### 7. KST 타임존 처리 방식
정산 기준이 KST 월 경계이므로, 타임존 일관성이 필수입니다. JVM 전역 타임존 변경 대신 아래 두 가지 방식을 조합했습니다.

| 설정 | 위치 | 효과 |
|------|------|------|
| `spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Seoul` | application.properties | DB 읽기/쓰기 시 KST 기준으로 변환 |
| `@Scheduled(cron = "...", zone = "Asia/Seoul")` | SettlementService | 월별 자동 정산 스케줄러 KST 기준 실행 |

JVM 전역 타임존(`TimeZone.setDefault()`)을 변경하지 않은 이유는 다른 라이브러리나 컴포넌트에 예기치 않은 영향을 줄 수 있기 때문입니다. 필요한 지점만 명시적으로 KST를 지정하는 방식을 선택했습니다.

### 8. 수수료율 스냅샷 저장 및 `endAt` 원거리 날짜 사용
**수수료율 스냅샷**: `SaleRecord.feeRate`에 구매 시점의 수수료율을 스냅샷으로 저장합니다. 이후 수수료율이 변경되어도 과거 판매 내역의 수수료 기준이 바뀌지 않도록 보장합니다.

**`endAt`에 `null` 대신 원거리 날짜(`2099-12-31`) 사용**: 현재 유효한 수수료율 조회 쿼리가 `f.endAt >= :now` 조건을 사용합니다. JPQL에서 `null` 비교는 `UNKNOWN`을 반환하여 해당 레코드가 조회되지 않습니다. "종료일 없음(무기한)"을 표현하기 위해 null 대신 충분히 먼 미래 날짜를 사용하여 null 처리 로직 없이 단순하게 유지했습니다.

---

## 미구현 / 제약사항

- **인증/인가**: Spring Security 미적용. `userId`를 요청 파라미터로 전달하는 방식으로 대체.
- **carryOver 기간 집계 정합성**: 집계 API(`/settlements/summary`)는 조회 범위의 첫 달 이전에 이월된 손실을 반영하지 않습니다. (예: 2월부터 조회 시 1월의 음수 carryOver 미반영)
- **정산 재생성 불가**: 이미 생성된 월 정산은 수정/재생성이 불가합니다. 취소·수정이 필요하면 데이터를 직접 조작해야 합니다.
- **부분 환불 후 재취소 불가**: 취소는 건당 1회만 가능하며, 부분 환불 후 잔여분 추가 환불은 지원하지 않습니다.
- **취소 후 재수강 불가**: `existsByStudentAndCourse` 조건이 취소 여부를 구분하지 않아, 취소된 수강 건도 중복으로 간주됩니다.

---

## AI 활용 범위

본 프로젝트는 Cursor AI (Claude Sonnet 4.5)를 활용하여 개발되었습니다.

| 활용 영역 | 내용 |
|-----------|------|
| 코드 설계 | 엔티티 구조, 서비스 레이어 로직, DTO 설계에 대한 피드백 및 초안 작성 |
| 버그 탐지 | `int` 오버플로우, H2 예약어 충돌, 중복 조건 체크 등 발견 |
| 비즈니스 로직 | carryOver 이월 로직 설계 및 구현 |
| 코드 리뷰 | 접근 제어, Builder 패턴, 예외 처리 일관성 등 검토 |
| 문서화 | README, PR 메시지 초안 작성 |

설계 결정(paidAt 직접 입력 허용, 정산 기준 월 경계, 수수료 버림 처리 등)은 개발자가 직접 판단하였으며, AI는 구현 보조 및 검증 역할을 수행했습니다.

---

## API 목록 및 예시

> 상세 요청/응답 스펙은 Swagger UI에서 확인하세요.
> **Swagger UI**: http://localhost:8080/api/v1/swagger-ui/index.html

### Enrollment (수강 및 취소)

| Method | URL | 설명 | 역할 |
|--------|-----|------|------|
| POST | `/api/v1/enrollments` | 강의 수강 등록 | STUDENT |
| POST | `/api/v1/enrollments/{saleRecordId}/cancel` | 수강 취소 및 환불 | STUDENT |

### SaleRecord (판매 내역)

| Method | URL | 설명 | 역할 |
|--------|-----|------|------|
| GET | `/api/v1/sale-records` | 크리에이터 판매 내역 목록 (날짜 필터) | CREATOR |

### Settlement (정산)

| Method | URL | 설명 | 역할 |
|--------|-----|------|------|
| POST | `/api/v1/settlements/generate` | 특정 연월 정산 수동 생성 | MANAGER |
| POST | `/api/v1/settlements/{settlementId}/confirm` | 정산 확정 (PENDING → CONFIRMED) | MANAGER |
| POST | `/api/v1/settlements/{settlementId}/pay` | 정산 지급 (CONFIRMED → PAID) | MANAGER |
| GET | `/api/v1/settlements/creators/{creatorId}/monthly` | 특정 월 정산 단건 조회 | CREATOR |
| GET | `/api/v1/settlements/summary` | 기간별 크리에이터 정산 집계 | MANAGER |

### 정산 상태 흐름

```
PENDING → CONFIRMED → PAID
```

- `PENDING`: 정산 생성 직후 초기 상태
- `CONFIRMED`: 관리자 확정 완료
- `PAID`: 지급 완료

### 주요 에러 코드
`exception/ErrorCode.java` 참고
| 코드 | HTTP | 설명 |
|------|------|------|
| `INVALID_ENROLL_ROLE` | 400 | 수강생(STUDENT)만 구매 가능 |
| `INVALID_PAYMENT_AMOUNT` | 400 | 결제 금액이 강의 정가와 불일치 |
| `DUPLICATE_ENROLLMENT` | 409 | 이미 수강 중인 강의 |
| `ALREADY_CANCELLED` | 409 | 이미 취소된 판매 내역 |
| `CANCEL_AMOUNT_EXCEEDED` | 400 | 취소 금액이 원결제 금액 초과 |
| `INVALID_SETTLEMENT_STATUS` | 400 | 상태 전환 불가 (순서 위반) |
| `SETTLEMENT_NOT_FOUND` | 404 | 해당 월 정산 없음 |
| `FEE_RATE_NOT_FOUND` | 404 | 유효한 수수료율 없음 |

---

## 데이터 모델 설명

### 엔티티 관계
<img width="1443" height="795" alt="스크린샷 2026-04-29 20 17 38" src="https://github.com/user-attachments/assets/1063056e-5ee6-4f88-a040-c8c8a0dda99e" />


### 주요 엔티티

| 엔티티 | 설명 |
|--------|------|
| `User` | 크리에이터 / 수강생 / 관리자 역할 구분 |
| `Course` | 강의 정보 (제목, 가격, 크리에이터) |
| `SaleRecord` | 수강 등록 내역 (결제 금액, 결제 일시, 수수료율 스냅샷) |
| `CancelRecord` | 취소 내역 (취소 금액, 취소 일시) — SaleRecord와 1:1 |
| `FeeRecord` | 수수료율 (유효 기간 기반, 현재 플랫폼 단일 수수료) |
| `Settlement` | 월별 크리에이터 정산 (순매출, 수수료, 정산금액, carryOver, 상태) |

### 정산 관련 핵심 필드 (`Settlement`)

| 필드 | 설명 |
|------|------|
| `netSales` | 이번 달 판매 합계 - 이번 달 환불 합계 (장부값) |
| `carryOverAmount` | 이번 달에 적용된 이전 달 미정산 손실 (0 이하) |
| `feeAmount` | `effectiveNet` 기준 수수료 (버림 처리) |
| `settlementAmount` | 실제 지급 금액 (`effectiveNet - feeAmount`) |

---

## 테스트 실행 방법

### Swagger를 통한 수동 테스트

1. 애플리케이션 실행 후 http://localhost:8080/api/v1/swagger-ui/index.html 접속
2. 앱 시작 시 샘플 데이터 자동 삽입 (크리에이터 3명, 강의 4개, 수강생 12명, 판매/취소 내역)

### carryOver 이월 검증 시나리오 (이강사 기준)

> `POST /settlements/generate`를 **1월 → 5월 순서대로** 호출해야 합니다. carryOver가 이전 달 정산 레코드를 참조하므로 순서가 중요합니다.

| 월 | netSales | carryOverAmount | effectiveNet | 예상 settlementAmount |
|----|----------|-----------------|--------------|----------------------|
| 1월 | +60,000 | 0 | +60,000 | **48,000** |
| 2월 | -60,000 | 0 | -60,000 | 0 |
| 3월 | +60,000 | -60,000 | 0 | 0 |
| 4월 | -60,000 | 0 | -60,000 | 0 |
| 5월 | +120,000 | -60,000 | +60,000 | **48,000** |

### 추가 검증 시나리오

| 시나리오 | 테스트 방법 |
|----------|------------|
| 월 경계 취소 | 1월 31일 판매(sale-5)가 1월 정산에 포함되고, 2월 3일 취소가 2월 정산에 반영되는지 확인 |
| 동월 전액 환불 | 3월 판매(sale-3) → 동월 취소 시 3월 순매출에서 차감되는지 확인 |
| 익월 부분 환불 | 3월 판매(sale-4) → 4월 취소 시 4월 정산 환불에 반영되는지 확인 |
| 빈 월 조회 | 박강사 1월 `GET /settlements/creators/{id}/monthly?year=2025&month=1` → 404 반환 확인 |
| 기간 집계 정렬 | `GET /settlements/summary?from=2025-01-01&to=2025-05-31` → 정산금액 내림차순 정렬 확인 |

## ETC

### 커밋 메시지 규칙

| type | 사용 시점 |
|------|-----------|
| `feat` | 새 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변화 없는 코드 구조 변경 |
| `test` | 테스트 데이터 추가/수정 |
| `chore` | 설정, 의존성, 빌드 관련 |
| `docs` | README 등 문서 작업 |

### 브랜치 구조

```
main
├── feat/data-init       # 샘플 데이터 초기화 (DataInitializer)
├── feat/enrollment      # 수강생: 강의 구매 + 취소 API
├── feat/sale-record     # 크리에이터: 판매 내역 조회 (기간 필터)
├── feat/settlement      # 정산 생성/조회/확정/지급 + carryOver 이월 로직
└── feat/common          # 공통 정리: Swagger 개선, API 순서 정비, 버그 수정, README.md 작성
```

각 피처 브랜치는 독립적으로 개발 후 PR을 통해 `main`에 병합했습니다.
