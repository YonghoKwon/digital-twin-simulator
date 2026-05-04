# AGENTS.md

이 문서는 이 저장소에서 작업하는 AI 에이전트와 개발자가 따라야 할 기본 가이드입니다.

## 프로젝트 개요

`digital-twin-data-generator`는 Spring Boot 기반의 ActiveMQ Artemis 데이터 생성/전송 시뮬레이터입니다.

주요 목적은 다음과 같습니다.

1. 특정 포맷에 맞는 데이터를 생성한다.
2. 특정 포맷에 맞는 랜덤 데이터를 대량 생성한다.
3. `format.txt`와 `data.txt` 파일을 읽어서 메시지를 생성한다.
4. 생성한 메시지를 ActiveMQ Artemis Topic으로 전송한다.
5. 부하 테스트를 위해 여러 task를 동시에 실행한다.
6. ActiveMQ 전송 없이 dry-run으로 생성 결과만 먼저 확인한다.
7. task별 RUNNING / SUCCESS / FAILED / CANCELLED 상태와 전송 개수를 확인한다.

## 기술 스택

- Java 21
- Spring Boot 3.5.x
- Maven
- ActiveMQ Artemis Jakarta Client
- springdoc-openapi / Swagger UI
- JUnit 5
- Lombok

## 실행 방법

```bash
mvn spring-boot:run
```

기본 포트는 `8080`입니다.

Swagger UI:

```text
http://localhost:8080/swagger
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## ActiveMQ 접속 정보

요청 body에 `activeMQIp`, `id`, `pw`, `topic`을 넣으면 요청 body 값이 우선 사용됩니다.
요청 body에서 생략하면 `application.properties`의 기본값을 사용합니다.

```properties
simulator.activemq.broker-url=tcp://localhost:61616
simulator.activemq.username=artemis
simulator.activemq.password=artemis
simulator.activemq.topic=topic.cep.output.0
```

이 구조는 로컬/개발/운영 서버별 기본값을 둘 수 있고, 필요할 때 요청 body로 broker를 바꿔 테스트할 수도 있습니다.

## 테스트 방법

```bash
mvn test
```

PR 또는 feature 브랜치 push 시 GitHub Actions CI에서 Java 21 기준으로 `mvn -B test`가 실행됩니다.

## 주요 API

### 데이터 생성/전송

- `POST /activemq/request/{taskId}`
- `POST /activemq/request/file/{taskId}`
- `POST /activemq/request/file-data/{taskId}`

### Dry-run

- `POST /activemq/request/dry-run?limit=10`
- `POST /activemq/request/file/dry-run?limit=10`
- `POST /activemq/request/file-data/dry-run?limit=10`

Dry-run은 ActiveMQ로 보내지 않고 생성될 메시지 샘플만 반환합니다. `limit`은 최대 100개로 제한됩니다.

### Task 관리

- `GET /activemq/task/running-tasks`
- `GET /activemq/task/statuses`
- `GET /activemq/task/statuses/running`
- `DELETE /activemq/task/statuses/finished`
- `POST /activemq/task/cancel-tasks`
- `POST /activemq/task/cancel-task/{taskId}`

## 파일 기반 생성 규칙

예시 format 파일:

```json
"temperature": "{{temperature}}",
"pressure": "{{pressure}}",
"status": "{{status}}"
```

### 순서 기반 data 파일

```csv
25.1,1001,OK
25.2,1002,OK
```

### 헤더 기반 data 파일

```csv
status,temperature,pressure
OK,25.1,1001
WARN,25.2,1002
```

### quoted CSV

쉼표가 포함된 값은 큰따옴표로 감싸면 됩니다.

```csv
status,message,temperature
OK,"hello, world",25.1
WARN,"quoted ""text"" sample",25.2
```

## 부하 테스트 주의사항

`concurrentTasks`는 동시에 실행할 비동기 task 수입니다. DT 시스템에서 1000개 이상의 메시지 처리 부하를 확인하기 위한 용도로 사용할 수 있습니다.

다만 현재 구조는 task마다 ActiveMQ connection/session/producer를 만들 수 있으므로, `concurrentTasks=1000`은 메시지 처리 부하뿐 아니라 broker 연결 생성 부하도 함께 포함합니다.

총 메시지 수는 다음과 같습니다.

```text
총 메시지 수 = concurrentTasks × repeatCount × data row 수
repeatCount = messageCount > 0 ? messageCount : ceil(repeatTime / delayTime)
```

처음 테스트할 때는 다음처럼 작게 시작하세요.

```json
{
  "concurrentTasks": 1,
  "messageCount": 1,
  "delayTime": 1000
}
```

정상 동작 확인 후 점진적으로 늘리는 것을 권장합니다.

## 코드 작업 원칙

1. 기존 API 호환성을 가능한 유지합니다.
2. 메시지 생성 로직은 테스트 가능한 순수 로직으로 분리하는 방향을 선호합니다.
3. 파일 경로는 반드시 normalize 후 base path escape 여부를 확인해야 합니다.
4. ActiveMQ 전송 로직 변경 시 브로커 연결 수와 부하를 고려합니다.
5. DTO에는 가능한 validation annotation을 추가합니다.
6. README와 `src/main/부하테스트.http`의 예시는 실제 동작하는 API 경로와 일치해야 합니다.
7. 변경 후 `mvn test` 또는 GitHub Actions CI 결과를 확인합니다.

## 추가 개선 후보

- connection/session/producer pool 또는 JmsTemplate 기반 부하 테스트 모드
- task 상태 이력 보관 개수 제한
- dry-run 응답을 JSON string 목록이 아니라 구조화된 DTO로 개선
- 운영 환경에서 민감한 계정 정보 로그 마스킹
