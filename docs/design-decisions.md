# 설계 결정과 이유

## 1. 월간 음수 순매출 carryOver 이월

환불 초과로 순매출이 음수인 달이 발생하면, 해당 손실을 다음 달 정산에 이월하여 차감합니다.

```
netSales     = 이번 달 판매 합계 - 이번 달 환불 합계
carryOver    = 이전 달 effectiveNet이 음수였던 경우 그 값 (0 이하)
effectiveNet = netSales + carryOver  →  실제 지급 기준 금액
```

- `effectiveNet > 0`: 수수료 계산, 정산금액 = effectiveNet - 수수료
- `effectiveNet ≤ 0`: 수수료 = 0, 정산금액 = 0, 다음 달로 이월 발생
- `Settlement.carryOverAmount`: 이번 달에 적용된 이전 달 이월액 (투명하게 응답에 노출)

## 2. 운영자용 기간별 집계 API 설계

정산 상태(PENDING/CONFIRMED/PAID)와 무관하게 `SaleRecord` / `CancelRecord`를 실시간으로 집계합니다.

- 지정 기간을 월 단위로 분리 → 각 월 독립 계산 → 크리에이터별 합산
- carryOver 이월도 월 루프 내 `runningCarryOver`로 반영
- 정산금액 내림차순 → 동일 금액 시 이름 오름차순 정렬

> 추후 뎁스가 한 단계 깊어진다면, 이미 확정된 정산(`CONFIRMED`, `PAID`)은 `Settlement` 테이블에서 가져오고, 미확정 기간만 실시간 계산하는 방식으로 전환할 수 있습니다.

## 3. H2 예약어 충돌 대응

H2에서 `YEAR`, `MONTH`는 SQL 예약어로 컬럼명으로 사용 시 오류가 발생합니다.
`Settlement` 엔티티에서 `@Column(name = "settlement_year")` / `@Column(name = "settlement_month")`로 명시하여 해결했습니다.

## 4. 금액 필드 `long` 타입 통일

`int` 최대값(약 21억)을 초과하는 금액 처리를 위해 모든 금액 필드를 `long`으로 통일했습니다.
API 요청값에 `@Max(10_000_000)` 제한과 Swagger `@Schema(example = "...")` 기본값을 함께 적용하였습니다.

## 5. Entity Builder 명시적 private 생성자

`@AllArgsConstructor(PRIVATE) + @Builder` 조합 대신 명시적 `@Builder private 생성자`를 사용하여 `id` 필드가 Builder에 노출되는 문제를 방지했습니다.

## 6. 중복 정산 방지 — UniqueConstraint

`Settlement` 테이블에 `(user_id, settlement_year, settlement_month)` 복합 UniqueConstraint를 적용하여 동일 크리에이터의 동월 정산이 중복 생성되는 것을 DB 레벨에서 방지합니다.

```java
@Table(name = "settlements",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"user_id", "settlement_year", "settlement_month"}))
```

수동 생성 API(`POST /settlements/generate`)는 이미 정산이 존재하는 크리에이터를 건너뛰고, 아직 생성되지 않은 크리에이터만 처리합니다. 따라서 동일 연월로 여러 번 호출해도 중복 정산이 발생하지 않습니다.

## 7. KST 타임존 처리 방식

정산 기준이 KST 월 경계이므로, 타임존 일관성이 필수입니다. JVM 전역 타임존 변경 대신 아래 두 가지 방식을 조합했습니다.

| 설정 | 위치 | 효과 |
|------|------|------|
| `spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Seoul` | application.properties | DB 읽기/쓰기 시 KST 기준으로 변환 |
| `@Scheduled(cron = "...", zone = "Asia/Seoul")` | SettlementService | 월별 자동 정산 스케줄러 KST 기준 실행 |

JVM 전역 타임존(`TimeZone.setDefault()`)을 변경하지 않은 이유는 다른 라이브러리나 컴포넌트에 예기치 않은 영향을 줄 수 있기 때문입니다. 필요한 지점만 명시적으로 KST를 지정하는 방식을 선택했습니다.

## 8. 수수료율 스냅샷 저장 및 `endAt` 원거리 날짜 사용

**수수료율 스냅샷**: `SaleRecord.feeRate`에 구매 시점의 수수료율을 스냅샷으로 저장합니다. 이후 수수료율이 변경되어도 과거 판매 내역의 수수료 기준이 바뀌지 않도록 보장합니다.

**`endAt`에 `null` 대신 원거리 날짜 사용**: 현재 유효한 수수료율 조회 쿼리가 `f.endAt >= :now` 조건을 사용합니다. JPQL에서 `null` 비교는 `UNKNOWN`을 반환하여 해당 레코드가 조회되지 않습니다. "종료일 없음(무기한)"을 표현하기 위해 null 대신 충분히 먼 미래 날짜를 사용합니다. 샘플 데이터에서는 `2026-12-31 23:59:59` 등으로 설정했습니다.

## 9. 정산 집계를 DB `SUM` / `COUNT`로 처리

정산 생성(`generateSettlement`)과 기간 집계(`getSummary`)에서 판매·취소 금액 및 건수는 엔티티 목록을 메모리에 올린 뒤 Java 스트림으로 합산하지 않고, `@Query`로 작성한 JPQL 집계(`COALESCE(SUM(...), 0)`, `COUNT`)를 사용합니다.

데이터량이 커져도 네트워크·메모리 부담이 줄고, 집계 의도가 Repository에 명시적으로 드러납니다.
