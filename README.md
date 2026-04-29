# 퓨쳐스콜레-라이브클래스 프로덕트 엔지니어 과제 - BE-B. 크리에이터 정산 API

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

- **인증/인가**: Spring Security 미사용. 과제 지침에 따라 `userId`를 파라미터/바디로 전달하는 방식으로 단순화.
- **결제 금액**: `paidAmount`는 강의 정가와 일치해야 하며, 불일치 시 `INVALID_PAYMENT_AMOUNT`.
- **`paidAt` / `canceledAt`**: 채점·엣지 케이스 테스트를 위해 클라이언트에서 설정 가능 (프로덕션에서는 서버 생성이 적절).
- **정산 기준**: 판매는 `paidAt`, 취소는 `canceledAt` 기준 월별 집계 (KST 월 경계). 수수료는 `effectiveNet`에 대해 백분율 저장 후 버림.

전체 서술은 [docs/requirements-and-assumptions.md](docs/requirements-and-assumptions.md) 참고.

---

## 설계 결정과 이유

- 월별 **carryOver 이월**(음수 순매출을 다음 달에 반영), **기간 집계 API**는 월 단위 분해 후 크리에이터별 합산.
- H2 **`YEAR`/`MONTH` 예약어** 회피, **`Settlement` UniqueConstraint**(크리에이터·연월), **DB `SUM`/`COUNT` 집계**, **KST 타임존·스케줄**, 수수료율 **스냅샷·`FeeRecord.endAt`** 처리 등.

항목별 상세·코드 예시는 [docs/design-decisions.md](docs/design-decisions.md) 참고.

---

## 미구현 / 제약사항

- 인증/인가 미적용 (`userId` 파라미터 방식).
- 기간 집계 시 직전 달 `Settlement`가 없으면 carryOver 초기값 반영이 불완전할 수 있음.
- 생성된 월 정산의 수정·재생성 불가, 취소 1회·재수강 제약 등.

전체 목록은 [docs/limitations.md](docs/limitations.md)의 「미구현 / 제약사항」 참고.

---

## AI 활용 범위

표 형태의 세부 내용은 [docs/ai.md](docs/ai.md)의 「AI 활용 범위」 참고.

---

## API 목록 및 예시

> 상세 요청/응답 스펙은 Swagger UI에서 확인하세요.
> **Swagger UI**: http://localhost:8080/api/v1/swagger-ui/index.html

### Enrollment (수강 및 취소)

| Method | URL | 설명 | 역할 |
|--------|-----|------|------|
| POST | `/api/v1/enrollments` | 강의 수강 등록 | STUDENT |
| POST | `/api/v1/enrollments/{saleRecordId}/cancel` | 수강 취소 및 환불 | STUDENT |

### FeeRate (플랫폼 수수료)

| Method | URL | 설명 | 역할 |
|--------|-----|------|------|
| GET | `/api/v1/fee-rates/current` | 현재 유효 수수료 조회 | MANAGER |
| POST | `/api/v1/fee-rates` | 새 수수료 구간 등록 | MANAGER |

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

Swagger 표시 정리, `sale-records`의 `from`/`to` 동작, **주요 에러 코드 표**는 [docs/api-reference.md](docs/api-reference.md) 참고.

---

## 데이터 모델 설명

엔티티 관계도·테이블 요약·`Settlement` 핵심 필드 설명은 [docs/data-model.md](docs/data-model.md) 참고.

---

## 테스트 실행 방법

1. 애플리케이션 실행 후 Swagger UI 접속: http://localhost:8080/api/v1/swagger-ui/index.html  
2. 샘플 데이터로 수동 테스트 — 이강사 carryOver 검증 시 **`POST /settlements/generate`를 2025년 1월→5월 순**으로 호출할 것.

시나리오 표·추가 검증 항목은 [docs/testing-guide.md](docs/testing-guide.md) 참고.

커밋 메시지 규칙·브랜치 구조는 [docs/git-workflow.md](docs/git-workflow.md) 참고.
