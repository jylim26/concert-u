# DB 낙관적 락 실험 결과

## 1) 테스트 조건
- 전략: DB 낙관적 락 (`@Version`)
- 테스트 대상: 동일 좌석 1개 (`performanceId=1`, `seatId=1`)
- 동시 요청: 100명
- 테스트 코드: `SeatHoldServiceTest.givenSameSeat_whenOptimisticHold_thenOneSuccess`

## 2) JUnit 결과 (로컬, 5회)
- 측정값(ms): `50, 45, 42, 47, 42`
- 평균(ms): `45.2`
- 최소~최대(ms): `42~50`
- 정합성: 매 실행마다 성공 1건, 실패 99건

## 3) k6 결과
- 실행 커맨드:
```bash
k6 run scripts/k6/hold-same-seat-optimistic.js
```
- p95 latency: `82.76ms`
- p99 latency: `측정값 미표기 (추가 실행 필요)`
- 성공/실패 건수: `200=1건, 409=99건`
- RPS/TPS: `http_reqs=1158.43/s`
- http_req_duration(avg/med/max): `71.09ms / 71.13ms / 83.94ms`

## 4) 충돌 관측
- Optimistic lock 충돌 횟수: `99건 (100건 중 1건 성공, 99건 실패)`
- 충돌 시 응답 코드(409) 비율: `99%`
- 예외 로그/메트릭 근거:
  - 서비스 레벨: `SeatHoldServiceTest`에서 매 실행 `성공 1건 / 실패 99건`
  - API 레벨: k6 결과 `seat_hold_status_200=1`, `seat_hold_status_409=99`
  - 예외 매핑: `ObjectOptimisticLockingFailureException -> 409 Conflict`

## 5) 결론
- 한 줄 결론: `낙관적 락은 정합성을 보장했지만(성공 1건), 충돌 구간에서 실패(409)가 집중적으로 발생하는 특성을 확인했다.`
