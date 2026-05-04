# AGENTS.md

이 파일은 `digital-twin-data-generator` 저장소의 개발 작업 가이드입니다.

## 프로젝트 요약

이 프로젝트는 Spring Boot 기반 ActiveMQ Artemis 데이터 생성/전송 시뮬레이터입니다.

현재 주요 기능:

- 포맷 기반 데이터 생성
- 랜덤 데이터 대량 생성
- `format.txt` + `data.txt` 기반 메시지 생성
- header 기반 data 매핑
- quoted CSV 처리
- ActiveMQ Topic 전송
- dry-run 생성 결과 확인
- task 상태 추적
- JmsTemplate 기반 부하 테스트
- Swagger UI 제공

## 실행

```bash
mvn spring-boot:run
```

Swagger:

```text
http://localhost:8080/swagger
http://localhost:8080/swagger-ui/index.html
```

## 테스트

```bash
mvn test
```

CI도 Java 21 기준으로 `mvn -B test`를 실행합니다.

## 주요 API

전송 API:

```text
POST /activemq/request/{taskId}
POST /activemq/request/file/{taskId}
POST /activemq/request/file-data/{taskId}
```

Dry-run API:

```text
POST /activemq/request/dry-run?limit=10
POST /activemq/request/file/dry-run?limit=10
POST /activemq/request/file-data/dry-run?limit=10
```

JmsTemplate 부하 테스트 API:

```text
POST /activemq/request/jms-template-load/{taskId}
```

Task API:

```text
GET /activemq/task/running-tasks
GET /activemq/task/statuses
GET /activemq/task/statuses/running
DELETE /activemq/task/statuses/finished
POST /activemq/task/cancel-tasks
POST /activemq/task/cancel-task/{taskId}
```

## 파일 기반 데이터 규칙

format 예시:

```json
"temperature": "{{temperature}}",
"pressure": "{{pressure}}",
"status": "{{status}}"
```

순서 기반 data:

```csv
25.1,1001,OK
25.2,1002,WARN
```

header 기반 data:

```csv
status,temperature,pressure
OK,25.1,1001
WARN,25.2,1002
```

quoted CSV:

```csv
status,message,temperature
OK,"hello, world",25.1
WARN,"quoted ""text"" sample",25.2
```

## 부하 테스트 메모

기존 `concurrentTasks` 방식은 동시 task/producer 부하 테스트에 가깝습니다.
JmsTemplate 방식은 제한된 worker와 session cache를 사용하므로 메시지 처리량 비교에 더 적합합니다.

총 메시지 수:

```text
총 메시지 수 = concurrentTasks × repeatCount × data row 수
repeatCount = messageCount > 0 ? messageCount : ceil(repeatTime / delayTime)
```

## 로그 보안

운영 로그에 민감 정보가 그대로 남지 않도록 합니다.
메시지나 요청 내용을 로그로 남길 때는 `SensitiveLogMasker.mask(...)` 적용 여부를 확인합니다.

## 작업 완료 후 문서 갱신 규칙

기능, API, 설정, 테스트 방식, 사용법을 변경했다면 작업 완료 전에 아래 세 파일을 반드시 확인하고 최신 상태로 맞춥니다.

1. `AGENTS.md`
2. `README.md`
3. `src/main/부하테스트.http`

다음 항목이 바뀌면 세 파일을 특히 주의해서 갱신합니다.

- REST API path
- request body / response body
- application properties
- messageCount / repeatTime / concurrentTasks / workerCount 동작
- dry-run 응답 구조
- task 상태 관리 방식
- CSV 파싱 규칙
- placeholder 매핑 규칙
- Swagger 접근 경로
- 테스트 실행 방법

변경이 필요 없다고 판단한 경우에도 최종 응답에 세 파일 확인 여부를 명시합니다.

## 추가 개선 후보

- 목표 TPS 기반 전송 모드
- JmsTemplate 부하 테스트 결과에 elapsed time, TPS, 실패 건수 집계 추가
- dry-run messages를 object 형태로 반환하는 옵션 추가
- task 상태 이력을 DB에 저장하는 옵션 추가
- request/response logging 정책을 profile별로 분리
