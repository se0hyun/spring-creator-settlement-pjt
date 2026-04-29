# API 목록 · Swagger · 에러 코드

> 상세 요청/응답 스키마는 **Swagger UI**에서 확인하세요.  
> http://localhost:8080/api/v1/swagger-ui/index.html

## Swagger UI 표시 정리

- **태그 순서**: `SwaggerConfig`에서 `OpenAPI.tags()`로 Enrollment → FeeRate → SaleRecord → Settlement 순을 명시했습니다. (`application.properties`의 `springdoc.swagger-ui.tags-sorter` 등으로 탭 정렬 방식 조정 가능)
- **스키마 필드 순서**: 주요 DTO에 `@JsonPropertyOrder`를 두어 Swagger의 예시 응답·스키마 탭에서 필드가 일정한 순서로 보이도록 했습니다.

## 판매 내역 조회 `GET /sale-records` — `from` / `to`

| 조건 | 동작 |
|------|------|
| `from`, `to` **모두** 지정 | `paidAt` 기준 기간 필터 (시작일 00:00 ~ 종료일 23:59:59) |
| **한쪽만** 지정 또는 **둘 다** 생략 | 크리에이터 **전체** 판매 목록 (결제일 최신순) |

---

## 엔드포인트 목록

### Enrollment (수강 및 취소)

| Method | URL | 설명 | 역할 |
|--------|-----|------|------|
| POST | `/api/v1/enrollments` | 강의 수강 등록 | STUDENT |
| POST | `/api/v1/enrollments/{saleRecordId}/cancel` | 수강 취소 및 환불 | STUDENT |

### FeeRate (플랫폼 수수료 구간)

| Method | URL | 설명 | 역할 |
|--------|-----|------|------|
| GET | `/api/v1/fee-rates/current` | 현재 시각 기준 유효 수수료 한 건 조회 | MANAGER |
| POST | `/api/v1/fee-rates` | 새 구간 등록 (`feeRate`, `effectiveFrom`). 기존 구간 종료 자동 조정 | MANAGER |

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

## 정산 상태 흐름

```
PENDING → CONFIRMED → PAID
```

- `PENDING`: 정산 생성 직후 초기 상태
- `CONFIRMED`: 관리자 확정 완료
- `PAID`: 지급 완료

---

## 주요 에러 코드

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
| `INVALID_DATE_RANGE` | 400 | 집계 시작일이 종료일보다 늦음 (`from > to`) |
| `INVALID_FEE_EFFECTIVE_FROM` | 400 | 수수료 적용 시작 시각이 유효하지 않음 |
| `FEE_RATE_SCHEDULE_CONFLICT` | 409 | 동일 적용 시작 시각의 수수료 구간이 이미 존재 |
