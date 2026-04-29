## AI 활용 범위

### 설계

Claude를 활용해 설계 및 지침으로써 활용을 위한 process.md를 만들었습니다.
이를 활용해 길어지는 컨텍스트에도 개발 방향을 놓치지 않을 수 있었습니다.

<details>
<summary>토글 접기/펼치기</summary>
<div markdown="1">
# 크리에이터 정산 API — 과제 요구사항 참조 문서

---

## 기술 스택 (확정)

- Spring Boot 3.5.14
- Java 21
- JPA
- H2 (인메모리, MODE=MySQL)
- Swagger (springdoc-openapi)

---

## 사용자 역할

| Role | 설명 |
|------|------|
| CREATOR | 강의 개설, 월별 정산 조회, 엑셀 다운로드 |
| STUDENT | 강의 구매, 강의 취소 |
| MANAGER | 전체 관리 (판매 조회, 정산 집계, 정산 확정/지급, 수수료 관리, 엑셀 다운로드) |

- 인증/인가는 HTTP Header `X-User-Id`로 userId를 전달하는 방식으로 처리
- 회원 가입(STUDENT)은 자유 가입, 크리에이터 등록은 MANAGER가 처리 (설계 가정)

---

## 도메인 및 테이블 정의

### users
| 컬럼 | 타입 | 설명 |
|------|------|------|
| userId | int PK AI | |
| userName | varchar(50) NOT NULL | |
| role | varchar(10) NOT NULL | CREATOR / STUDENT / MANAGER |

### courses
| 컬럼 | 타입 | 설명 |
|------|------|------|
| courseId | int PK AI | |
| courseTitle | varchar(200) NOT NULL | |
| coursePrice | int NOT NULL | 변경 가능 |
| userId | int FK → users | 강의 소유 크리에이터 |

### salesRecords
| 컬럼 | 타입 | 설명 |
|------|------|------|
| salesId | int PK AI | |
| courseId | int FK → courses | |
| userId | int FK → users | 구매한 수강생 |
| paidAmount | int NOT NULL | 구매 시점 가격 스냅샷 |
| paidAt | datetime NOT NULL | KST 기준 |
| feeRate | decimal(5,2) NOT NULL | 구매 시점 수수료율 스냅샷 |

### cancelRecords
| 컬럼 | 타입 | 설명 |
|------|------|------|
| cancelId | int PK AI | |
| salesId | int FK → salesRecords | 1:1 관계 |
| cancelAmount | int NOT NULL | 부분환불 가능 (원결제액보다 작을 수 있음) |
| canceledAt | datetime NOT NULL | KST 기준 |

- salesRecords : cancelRecords = 1:1
- 강의 특성상 취소는 1회만 발생 (진도율 기반 환불 정책 도입 시에도 취소 자체는 1회)

### feeRecords
| 컬럼 | 타입 | 설명 |
|------|------|------|
| feeId | int PK AI | |
| feeRate | decimal(5,2) NOT NULL | |
| startAt | datetime NOT NULL | 수수료 적용 시작 시점 |
| endAt | datetime NULL | NULL이면 현재 적용 중 |

- 다른 테이블과 직접 FK 관계 없음
- 판매 시점(paidAt)에 해당하는 feeRate를 조회해서 salesRecords.feeRate에 스냅샷 저장
- 현재는 20% 고정, 변경 이력 관리 가능한 구조

### settlements
| 컬럼 | 타입 | 설명 |
|------|------|------|
| settlementId | int PK AI | |
| userId | int FK → users | 크리에이터 |
| year | int NOT NULL | |
| month | int NOT NULL | |
| totalSales | int NOT NULL | 해당 월 총 판매 금액 |
| totalRefunds | int NOT NULL | 해당 월 총 환불 금액 |
| netSales | int NOT NULL | totalSales - totalRefunds |
| feeAmount | int NOT NULL | netSales × feeRate |
| settlementAmount | int NOT NULL | netSales - feeAmount |
| salesCount | int NOT NULL | 판매 건수 |
| cancelCount | int NOT NULL | 취소 건수 |
| status | varchar(10) NOT NULL | PENDING / CONFIRMED / PAID (기본값 PENDING) |
| createdAt | datetime NOT NULL | 정산 생성 시각 (기본값 now()) |
| confirmedAt | datetime NULL | 운영자 확정 시각 |
| paidAt | datetime NULL | 지급 완료 시각 |

- UNIQUE 제약: (userId, year, month) → 동일 기간 중복 정산 방지

---

## 핵심 비즈니스 규칙

### 정산 계산 공식
```
총 판매액    = 해당 월 paidAt 기준 salesRecords.paidAmount 합산
총 환불액    = 해당 월 canceledAt 기준 cancelRecords.cancelAmount 합산
순 판매액    = 총 판매액 - 총 환불액
수수료       = 순 판매액 × feeRate (salesRecords별 feeRate 적용)
정산 예정액  = 순 판매액 - 수수료
```

### 정산 기간 기준 (중요)
- 판매는 **paidAt** 기준 월에 반영
- 취소는 **canceledAt** 기준 월에 반영
- 월 경계: 해당 월 1일 00:00:00 ~ 말일 23:59:59 **KST 기준**
- 예: 1월 31일 결제 → 1월 정산 반영 / 2월 3일 취소 → 2월 정산 반영

### 수수료율
- 현재 고정값 20%
- salesRecords.feeRate에 판매 시점 수수료율 스냅샷 저장
- 월 중간에 수수료율이 바뀌어도 각 판매건의 feeRate 기준으로 계산
- settlements에는 feeRate 컬럼 없음 (feeAmount로 충분)

### 취소/환불
- 부분환불 가능: cancelAmount ≤ paidAmount
- 취소 시 feeRate는 원본 salesRecords.feeRate 참조
- cancelRecords에 feeRate 컬럼 불필요

### 정산 상태 흐름
```
PENDING → CONFIRMED → PAID
```
- PENDING: 정산 생성 시 기본값
- CONFIRMED: 운영자가 금액 확정
- PAID: 크리에이터에게 지급 완료
- 동일 creatorId + year + month 조합은 1개만 존재 가능

### 빈 월 조회
- 판매 내역 없는 크리에이터/월 조회 시 null 반환 금지
- 모든 금액 0, 건수 0으로 응답

---

## 필수 구현
- 판매 내역 등록 및 조회
- 취소 내역 등록
- 크리에이터별 월별 정산 조회
- 기간별 전체 크리에이터 정산 집계
- 정산 상태 관리 (PENDING → CONFIRMED → PAID)
- 동일 기간 중복 정산 방지
- 수수료율 변경 이력 관리
- KST 일관된 처리

</div>
</details>

또, 과제 가이드라인 상 개인적으로 설정할 설계 요소를 구체화할 때, 라이브클래스 서비스를 개발하는 기획자임을 선언하여 기획을 진행했습니다.

### 개발

1. Cursor AI를 활용해 코드를 작성했습니다.
기본적으로 엔티티 구조, 서비스 레이어 로직, DTO를 개발자가 설계한 뒤, 기본적인 코드를 작성했습니다.

2. 버그 탐지
- H2 예약어 충돌 감지
- 중복 조건 체크 발견

3. 코드 리뷰
- 생성자 패턴
- 예외 처리 일관성 검토

4. Docs 작성
- README 초안 작성 및 내용 간략화
- PR 메시지 초안 작성