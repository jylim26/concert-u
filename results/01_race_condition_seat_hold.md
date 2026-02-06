# 01. 동시성 제어 없는 좌석 선점
티켓팅 서비스 이용 시 화가 나는 순간은 결제 단계까지 진입했음에도 불구하고,
마지막 순간에 "이미 예약된 좌석입디나"라는 메시지와 함께 실패하는 경우입니다.
이러한 현상이 발생하는 기술적 원인을 데이터를 통해 구체적으로 확인해보았습니다.

---

## 1. 테스트 목표
- 별도의 동기화 처리 없이 단일 좌석에 요청이 집중될 때 발생하는 현상을 재현합니다.
- 동시성 제어의 필요성을 막연한 이론보다는 실제 병목과 메트릭을 분석하여 확인해봅니다.

---

## 2. 테스트 환경 및 시나리오
- 엔드포인트: `POST /api/performances/1/seats/1/hold`
- 부하 테스트 도구: k6
- 트래픽 시나리오: 100명의 가상 사용자가 동시에 단 1회 좌석 선점을 시도하는 상황

---

## 3. 테스트 실행
```bash
k6 run scripts/k6/hold-same-seat.js
```

---

## 4. 결과 요약
- 선점 성공(200 OK): 10건 (비정상)
- 선점 실패(409 Conflict): 90건
- 응답 시간 (p95): 289.22ms

```bash
     execution: local
        script: hold-same-seat.js
        output: -

     scenarios: (100.00%) 1 scenario, 100 max VUs, 40s max duration (incl. graceful stop):
              * once: 1 iterations for each of 100 VUs (maxDuration: 10s, gracefulStop: 30s)



  █ TOTAL RESULTS

    checks_total.......: 100     321.683811/s
    checks_succeeded...: 100.00% 100 out of 100
    checks_failed......: 0.00%   0 out of 100

    ✓ 200 or 409

    CUSTOM
    seat_hold_status_200...........: 10     32.168381/s
    seat_hold_status_409...........: 90     289.51543/s

    HTTP
    http_req_duration..............: avg=199.54ms min=54.59ms med=207.51ms max=296.32ms p(90)=283.12ms p(95)=289.22ms
      { expected_response:true }...: avg=94.78ms  min=54.59ms med=95.61ms  max=137.8ms  p(90)=130.88ms p(95)=134.34ms
    http_req_failed................: 90.00% 90 out of 100
    http_reqs......................: 100    321.683811/s

    EXECUTION
    iteration_duration.............: avg=207.31ms min=57.39ms med=213.95ms max=306.29ms p(90)=289.31ms p(95)=298.12ms
    iterations.....................: 100    321.683811/s

    NETWORK
    data_received..................: 15 kB  48 kB/s
    data_sent......................: 17 kB  54 kB/s
```

> K6는 기본적으로 409 응답을 에러로 간주하여 `http_req_failed` 지표에 반영됩니다.
> 409는 이미 좌석 선점이 되어있어 실패한 정상적인 비즈니스 예외이기 때문에 집계는 별도로 설정한 커스텀 카운터를 사용하였습니다.

---

## 5. 결과 해석(데이터 정합성 이슈)
이번 테스트 결과는 읽고 쓰는 패턴의 Race Condition 현상을 보여줍니다.
1. User A가 좌석 상태 조회 (`AVAILABLE`)
2. User B도 동시에 좌석 상태 조회 (`AVAILABLE`)
3. User A가 선점 처리: `HELD`로 상태 변경
4. User B도 선점 처리: A의 변경 사항을 모른채 업데이트 (Lost Update)

단 하나의 좌석에 대해 정상적인 성공 응답은 1건이어야 합니다. 그러나 테스트 결과 10건의 성공 응답이 발생했습니다.
즉, 적절한 동시성 제어가 없을 경우 여러 사용자에게 동시에 좌석 선점에 성공하는 치명적인 정합성 문제가 발생함을 확인했습니다.

---

## 6. 향후 계획
다음 단계에서는 시스템에 적합한 동시성 제어 기법을 적용하여, 동일 좌석에 대해 1건의 요청만 성공하도록 개선하려고 합니다.
또한 비관적 락, 낙관적 락 등 각 방식에 따른 정확성 확보 여부와 성능 변화를 비교 분석해보겠습니다.