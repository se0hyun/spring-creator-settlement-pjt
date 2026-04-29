# 데이터 모델 설명

## 엔티티 관계

<img width="1443" height="795" alt="엔티티 관계도" src="https://github.com/user-attachments/assets/1063056e-5ee6-4f88-a040-c8c8a0dda99e" />

## 주요 엔티티

| 엔티티 | 설명 |
|--------|------|
| `User` | 크리에이터 / 수강생 / 관리자 역할 구분 |
| `Course` | 강의 정보 (제목, 가격, 크리에이터) |
| `SaleRecord` | 수강 등록 내역 (결제 금액, 결제 일시, 수수료율 스냅샷) |
| `CancelRecord` | 취소 내역 (취소 금액, 취소 일시, **취소 시점 수수료율 스냅샷**) — SaleRecord와 1:1 |
| `FeeRecord` | 수수료율 (유효 기간 기반, 현재 플랫폼 단일 수수료) |
| `Settlement` | 월별 크리에이터 정산 (순매출, 수수료, 정산금액, carryOver, 상태) |

## 정산 관련 핵심 필드 (`Settlement`)

| 필드 | 설명 |
|------|------|
| `netSales` | 이번 달 판매 합계 - 이번 달 환불 합계 (장부값) |
| `carryOverAmount` | 이번 달에 적용된 이전 달 미정산 손실 (0 이하) |
| `feeAmount` | `effectiveNet` 기준 수수료 (버림 처리) |
| `settlementAmount` | 실제 지급 금액 (`effectiveNet - feeAmount`) |
