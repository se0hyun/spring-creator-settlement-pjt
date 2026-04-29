# 커밋 메시지 규칙 및 브랜치 구조

## 커밋 메시지

| type | 사용 시점 |
|------|-----------|
| `feat` | 새 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변화 없는 코드 구조 변경 |
| `test` | 테스트 데이터 추가/수정 |
| `chore` | 설정, 의존성, 빌드 관련 |
| `docs` | README 등 문서 작업 |

## 브랜치 구조

```
main
├── feat/data-init       # 샘플 데이터 초기화 (DataInitializer)
├── feat/enrollment      # 수강생: 강의 구매 + 취소 API
├── feat/sale-record     # 크리에이터: 판매 내역 조회 (기간 필터)
├── feat/settlement      # 정산 생성/조회/확정/지급 + carryOver 이월 로직
└── feat/common          # 공통 정리: Swagger 개선, API 순서 정비, 버그 수정, README.md 작성
```

각 피처 브랜치는 독립적으로 개발 후 PR을 통해 `main`에 병합했습니다.
