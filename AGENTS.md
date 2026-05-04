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

## 테스트 방법

```bash
mvn test
```

PR 또는 feature 브랜치 push 시 GitHub Actions CI에서 Java 21 기준으로 `mvn -B test`가 실행됩니다.

## 주요 API

### 데이터 생성/전송

- `POST /activemq/request/{taskId}`
  - format 정의 기반 일반 메시지 전송
  - `value`가 비어 있으면 랜덤 데이터 생성
  - `value`가 있으면 value row 기반 전송

- `POST /activemq/request/file/{taskId}`
  - 서버 로컬 파일의 JSON fragment를 메시지에 포함해서 전송

- `POST /activemq/request/file-data/{taskId}`
  - 서버 로컬의 `formatFileName`과 `dataFileName`을 읽어서 전송
  - 핵심 기능입니다.
  - `data.txt` 첫 줄에 header가 있으면 placeholder 이름 기준으로 매핑합니다.
  - header가 없으면 format placeholder 순서와 data column 순서로 매핑합니다.

### Task 관리

- `GET /activemq/task/running-tasks`
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

매핑:

```text
temperature = 25.1
pressure    = 1001
status      = OK
```

### 헤더 기반 data 파일

```csv
status,temperature,pressure
OK,25.1,1001
WARN,25.2,1002
```

매핑:

```text
temperature = 25.1
pressure    = 1001
status      = OK
```

컬럼 순서가 달라도 header 이름과 placeholder 이름이 같으면 정상 매핑됩니다.

## 부하 테스트 주의사항

`concurrentTasks`는 동시에 실행할 비동기 task 수입니다.

예를 들어 다음 요청은 매우 큰 부하를 만들 수 있습니다.

```json
{
  "concurrentTasks": 1000,
  "repeatTime": 35000,
  "delayTime": 1000
}
```

현재 반복 횟수 계산은 다음과 같습니다.

```text
repeatCount = ceil(repeatTime / delayTime)
```

위 예시는 task 하나당 data 파일 전체를 35번 반복합니다.

총 메시지 수는 다음과 같습니다.

```text
총 메시지 수 = concurrentTasks × repeatCount × data row 수
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

## 현재 남은 개선 후보

- quoted CSV 완전 지원
- 생성 결과만 확인하는 dry-run API
- task별 상태 저장: RUNNING / SUCCESS / FAILED / CANCELLED
- ActiveMQ 접속 정보를 request body가 아니라 서버 설정으로 분리
- 개발용 `/test` API 제거 또는 local/dev profile 전용화
