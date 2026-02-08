# DB 비관적 락 실험 결과

## 1) 테스트 조건
- 전략: DB 비관적 락 (`SELECT ... FOR UPDATE`)
- 테스트 대상: 동일 좌석 1개 (`performanceId=1`, `seatId=1`)
- 동시 요청: 100명
- 테스트 코드: `SeatHoldServiceTest.givenSameSeat_whenPessimisticHold_thenOneSuccess`

## 2) JUnit 결과 (로컬, 5회)
- 측정값(ms): `126, 108, 113, 116, 105`
- 평균(ms): `113.6`
- 최소~최대(ms): `105~126`
- 정합성: 매 실행마다 성공 1건, 실패 99건

## 3) k6 결과
- 실행 커맨드:
```bash
k6 run scripts/k6/hold-same-seat-pessimistic.js
```
- p95 latency: `108.04ms`
- p99 latency: `측정값 미표기 (추가 실행 필요)`
- 성공/실패 건수: `200=1건, 409=99건`
- RPS/TPS: `http_reqs=865.03/s`
- http_req_duration(avg/med/max): `86.54ms / 88.16ms / 110.40ms`

## 4) DB 락 관측
- lock wait 발생 여부: `발생`
- deadlock 발생 여부: `미발생 (관측 중 데드락 없음)`
- 관측 근거: `performance_schema.data_lock_waits`, `performance_schema.data_locks`
- 관측 요약:
  - 선행 트랜잭션(`trx_id=12909`, thread=800): `uk_perf_seat` 인덱스 레코드에 `X,REC_NOT_GAP` 락 `GRANTED`
  - 후행 트랜잭션(`trx_id=12910`, thread=801): 동일 레코드에 `X,REC_NOT_GAP` 락 `WAITING`
  - lock data: `1, 1, 1` (동일 좌석 키 경합)
  - 해석: `FOR UPDATE`가 동일 좌석 요청을 DB 레벨에서 직렬화함을 확인

## 5) 결론
- 한 줄 결론: `비관적 락은 동일 좌석 요청을 row-level(`X,REC_NOT_GAP`)로 직렬화해 정합성을 보장했지만, 동일 시나리오에서 낙관적 락 대비 응답 시간이 더 느렸다.`
