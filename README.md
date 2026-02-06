# 🎟️ Concert-U (대규모 트래픽 티켓팅 서비스)
> "데이터 정합성과 대규모 트래픽을 처리할 수 있는 아키텍처 설계" <br>
> (현재 진행 중이며 README.md 업데이트 예정)

# 프로젝트 소개
단순한 티켓팅 서비스 기능 구현을 넘어, 대용량 트래픽 환경에서 발생하는 동시성 이슈와 성능 병목을 해결하는데 주력한 티켓팅 서비스입니다. 다양한 동시성 제어 기법을 적용해 데이터 정합성을 보장하고, 부하 테스트를 통해 시스템의 한계점을 분석했습니다. 이를 통해 트레이드오프를 고려한 아키텍처 최적화 과정을 거치며 안정적인 대규모 트랜잭션 처리 시스템을 구축했습니다.
대용량 트래픽 상황에서도 데이터 정합성을 보장하고 안정적인 티켓팅을 처리하기 위한 서비스입니다.
---

# 🛠️ 기술 스택
<img src="https://img.shields.io/badge/java-%234479A1.svg?style=for-the-badge&logo=openjdk&logoColor=white"> <img src="https://img.shields.io/badge/spring%20boot-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/jpa-%236DB33F.svg?style=for-the-badge&logo=hibernate&logoColor=white">
<br>
<img src="https://img.shields.io/badge/AWS-%23000000.svg?style=for-the-badge&logo=amazon-aws&logoColor=white"> <img src="https://img.shields.io/badge/EC2-%23FF9900.svg?style=for-the-badge&logo=ec2&logoColor=white"> <img src="https://img.shields.io/badge/docker-%232496ED.svg?style=for-the-badge&logo=docker&logoColor=white">
<br>
<img src="https://img.shields.io/badge/mysql-%234479A1.svg?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/redis-%23DC382D.svg?style=for-the-badge&logo=redis&logoColor=white">
<br>
<img src="https://img.shields.io/badge/k6-%237D64FF.svg?style=for-the-badge&logo=k6&logoColor=white">

---

# 🏗️ 인프라 구성
서버의 역할에 따라 물리적 인프라를 분리하여 확장성과 안정성을 확보했습니다.

![architecture](./images/architecture.png)

### 서버 역할 분리 및 확장 전략
트래픽의 성격에 따라 서버 그룹을 분리하여 확장성있게 설계했습니다. 
- 대기열 서버
  - 역할: 수만 명의 대기열 진입 및 연결 유지
  - 특성: Network I/O Bound. CPU 연산보다 메모리와 동시 연결 유지 능력이 중요
  - 전략: 메모리 최적화 인스턴스 위주로 수평 확장
- 티켓팅 서버
  - 역할: 결제, 좌석 선점 등 비즈니스 로직 수행
  - 특성: CPU Bound. 처리 속도가 늦어지면 DB 커넥션 병목 발생
  - 전략: 연산 속도가 빠른 CPU 최적화 인스턴스를 배치

### 대기열 시스템
API 서버를 늘려 무작정 트래픽을 받게되면, DB가 병목이 되어 전체 시스템이 병목이 될수있어 DB가 처리 가능한 범위 내에서 유량을 제어하도록 설계했습니다.
- 자료구조: Redis `Sorted Set`
- 유량 제어
  - DB 인스턴스의 최대 TPS를 벤치마킹
  - 스케줄러가 허용 가능한 TPS의 70~80% 수준으로 대기 토큰을 활성 토큰으로 전환
- 사용자 알림: 클라이언트 폴링 부하를 줄이기 위해 SSE 적용

### 좌석 선점과 데이터 정합성
동시다발저인 좌석 예약 요청에 대해 데이터 무결성을 보장하도록 설계했습니다.
- Redis Lua Script: `SETNX`와 `EXPIRE` 를 원자적으로 실행하여 동시성 제어
- DB Unique Index: 유니크 인덱스를 통해 최종 중복 저장 방지
- 보상 트랜잭션: Redis 선점 후 DB 저장 실패 시, 즉시 Redis 락을 해제하여 좀비 좌석 방지
---
# 테스트 과정 및 결과
개발 과정에서 발생한 동시성 이슈와 성능 개선 기록입니다.

- [01. 동시성 제어 없는 좌석 선점](results/01_race_condition_seat_hold.md)
  - 상황: 동시성 제어 없이 100명이 단일 좌석 선점 시도
  - 결과: 10명이 동시에 예약 성공하는 Lost Update 문제 발생

- [02. synchronized 적용과 분산 환경의 한계](results/02_synchronized_alb_multi_instance.md)
  - 시도: Java `Synchronized` 키워드 적용
  - 단일 서버: 정상 동작 (1건 성공, 99건 실패)
  - 멀티 서버: 정합성 깨짐 (2건 성공). 인스턴스 간 JVM 메모리 미공유로 인한 한계 측정
  - 결과: 분산 환경에서는 외부 저장소를 활용한 락 메커니즘 필수