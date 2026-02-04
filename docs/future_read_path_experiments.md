# Future Read-Path Experiments (Later)

이 문서는 기존 로드맵(1~5단계)에서 이미 합의된 테스트 항목이 아니라, 최근 대화에서 “나중에 병목 관찰 후 개선 포인트로 검증하자”로 합의한 **추가 실험 후보**만 정리한다.

---

## 배경: 현재 쿼리의 병목 신호

현재 `performance_id`로 필터링하고 `seats.seat_no`로 정렬하는 조회는 EXPLAIN에서 `Using temporary; Using filesort`가 관측됐다.

- 원인 요약: 드라이빙 테이블이 `performance_seats`인데 정렬 키가 `seats.seat_no`라서, 조인 결과를 임시 테이블로 만든 뒤 정렬(filesort)하는 비용이 발생할 수 있다.

---

## 실험 1) “seats-driven” 플랜으로 정렬 비용 줄이기

### 가설
`seats(venue_id, seat_no)` 인덱스를 활용해서 `seats`를 먼저 정렬된 상태로 읽고, `performance_seats(performance_id, seat_id)`로 붙이면 `Using filesort`를 제거/완화할 수 있다.

### 방법
- `performance_id -> venue_id`를 확보한 뒤
- `seats where venue_id=? order by seat_no`로 먼저 읽고
- `performance_seats where performance_id=? and seat_id in (...)` 또는 조인 형태로 결합

### 검증 포인트
- `EXPLAIN FORMAT=JSON`, `EXPLAIN ANALYZE`에서 `Using filesort`/`Using temporary` 여부
- p95/p99 latency, DB CPU, Rows examined 변화

---

## 실험 2) 조회 범위 축소: section(구역) 단위 조회

### 가설
실제 서비스처럼 “회차(Performance) + 구역(section)” 단위로 조회하면, 5만석이라도 한 번에 500~1000석만 가져와 **정렬/네트워크/DB 부하**가 감소한다.

### 방법
- `GET /seats?section=...` 같은 인터페이스로 조회 범위를 제한
- (1차) 스키마 변경 없이도 필터링 가능하면(예: `seat_no` range 조건) 먼저 실험
- (2차) 아래 실험 3의 스키마 변경과 함께 최적화 실험 진행

### 검증 포인트
- 요청당 결과 row 수(페이지 크기) 기준으로 p95/p99, TPS 변화
- DB 커넥션 풀 점유율, slow query 증가/감소

---

## 실험 3) 반정규화로 조인 제거: performance_seats에 section/seat_no 보관

### 아이디어
- `seats`에 `section` 컬럼 추가
- `performance_seats`에 `section`, `seat_no`를 **반정규화**로 저장 (좌석 메타는 거의 불변이라는 전제)

### 가설
조회 쿼리를 `performance_seats` 단독으로 처리하면 조인 비용이 없어지고,
`(performance_id, section, seat_no)` 인덱스로 정렬까지 해결 가능해진다.

### 인덱스 후보
- `performance_seats(performance_id, section, seat_no)`
- 필요 시 커버링: `(..., status, reserved_by, reserved_at, price)` 등은 실측 후 결정

### 정합성 리스크(테스트 항목)
- `Seat` 메타(구역/번호)가 변경될 때 `performance_seats` 동기화가 필요한지/불변 가정이 맞는지
- 데이터 마이그레이션/백필(backfill) 절차

### 검증 포인트
- EXPLAIN에서 `Using filesort`/`Using temporary` 제거 여부
- 조인 제거 전/후 p95/p99, DB CPU/IO, Rows examined

---

## 실험 4) Cache-Aside로 GET 조회 부하 분산 (Phase 3)

### 원칙(합의)
락 전략 비교(1~4단계)의 베이스라인을 흐리지 않기 위해, 캐시는 **나중에** 적용한다.

### 가설
좌석 조회 트래픽이 DB를 잡아먹는 구간에서 Cache-Aside가 DB CPU/IO를 낮추고 p95/p99를 안정화한다.

### 캐시 설계 후보
- (A) “좌석 메타(grade/price/seatNo/section)”는 캐시, “상태(status/reservedBy/reservedAt)”는 DB/Redis로 분리
- (B) 공연별 좌석 뷰 전체를 캐시(단, 상태 변경 시 무효화 전략 필요)

### 검증 포인트
- DB QPS/CPU/IO 감소, 캐시 hit ratio, p95/p99 변화
- 상태 변경(hold) 직후 조회 일관성(허용 가능한 stale 범위 정의)

---

## 실험 5) 정렬 키 개선: seat_no(문자열) → seat_seq(숫자)

### 가설
문자열 정렬 비용 및 “A1/A10/A2” 문제를 줄이기 위해, 정렬 가능한 숫자 컬럼(`seat_seq`)로 정렬하면 DB 정렬 비용이 감소한다.

### 검증 포인트
- `order by seat_seq` 전환 전/후 `filesort` 여부 및 비용, p95/p99 변화

---

## 실험 6) 커버링 인덱스로 랜덤 I/O 줄이기 (현재 쿼리 유지 시)

### 가설
조인+정렬 구조를 유지하더라도, `performance_seats`에서 필요한 컬럼을 최대한 인덱스로 커버하면 테이블 접근을 줄여 읽기 비용을 감소시킬 수 있다.

### 후보
- `performance_seats(performance_id, seat_id, status, reserved_by, reserved_at)`

### 검증 포인트
- Handler reads/Rows examined 변화, p95/p99 변화
