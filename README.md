# Digital Twin Data Generator

REST API 요청으로 특정 포맷의 데이터를 대량 생성한 뒤 ActiveMQ Artemis Topic으로 전송하는 시뮬레이터입니다.

## Swagger UI

- http://localhost:8080/swagger
- http://localhost:8080/swagger-ui/index.html

## 실행

```bash
mvn spring-boot:run
```

## ActiveMQ 기본 설정

요청 body에 `activeMQIp`, `id`, `pw`, `topic`을 넣으면 해당 값이 우선 사용됩니다.
요청 body에서 생략하면 아래 서버 기본값을 사용합니다.

```properties
simulator.activemq.broker-url=tcp://localhost:61616
simulator.activemq.username=artemis
simulator.activemq.password=artemis
simulator.activemq.topic=topic.cep.output.0
simulator.activemq.session-cache-size=100
simulator.task.max-history=10000
```

로컬/개발/운영 서버별 기본값은 properties/profile로 관리하고, 특정 테스트에서만 요청 body로 덮어쓸 수 있습니다.

## 공통 요청 필드

| field | 설명 | 예시 |
| --- | --- | --- |
| activeMQIp | ActiveMQ broker URL. 생략 시 서버 기본값 사용 | `tcp://localhost:61616` |
| id | ActiveMQ 계정. 생략 시 서버 기본값 사용 | `artemis` |
| pw | ActiveMQ 비밀번호. 생략 시 서버 기본값 사용 | `artemis` |
| topic | 전송할 topic. 생략 시 서버 기본값 사용 | `topic.cep.output.0` |
| tcName | transaction/message id | `KE2D1Z11` |
| delayTime | 메시지 간 대기 시간(ms) | `2000` |
| repeatBoolean | 반복 전송 여부 | `true` |
| repeatTime | `messageCount`가 없을 때 전송 지속 시간(ms) | `10000` |
| messageCount | 명시적 생성 횟수. 지정 시 `repeatTime`보다 우선 | `1000` |
| concurrentTasks | 동시 실행 task 수. 1~2000 | `10` |

## 1. Dry-run

Dry-run은 ActiveMQ로 전송하지 않고 생성될 메시지 샘플만 반환합니다. 응답은 구조화된 DTO이며, `limit`은 최대 100개입니다.

```http
POST http://localhost:8080/activemq/request/file-data/dry-run?limit=5
Content-Type: application/json
```

```json
{
  "tcName": "KE2D1Z11",
  "delayTime": 0,
  "repeatBoolean": true,
  "repeatTime": 0,
  "messageCount": 1,
  "filePath": "c:/Project/",
  "formatFileName": "KE2D1Z11_format.txt",
  "dataFileName": "KE2D1Z11_data.txt",
  "concurrentTasks": 1
}
```

응답 예시:

```json
{
  "requestedLimit": 5,
  "returnedCount": 3,
  "estimatedRepeatCount": 1,
  "estimatedSourceRowCount": 3,
  "estimatedTotalMessagesPerTask": 3,
  "generationMode": "FILE_DATA",
  "messages": [
    "{...}"
  ]
}
```

지원 API:

| method | path | 설명 |
| --- | --- | --- |
| POST | `/activemq/request/dry-run?limit=10` | 일반 format/value 기반 생성 결과 확인 |
| POST | `/activemq/request/file/dry-run?limit=10` | 파일 메시지 생성 결과 확인 |
| POST | `/activemq/request/file-data/dry-run?limit=10` | format/data 파일 기반 생성 결과 확인 |

## 2. 포맷 기반 랜덤 대량 데이터 생성

`value`를 비우거나 생략하면 `format` 정의에 맞춰 랜덤 데이터를 생성합니다.

```http
POST http://localhost:8080/activemq/request/random-bulk
Content-Type: application/json
```

```json
{
  "tcName": "KE2D1Z11",
  "delayTime": 0,
  "repeatBoolean": true,
  "repeatTime": 0,
  "messageCount": 1000,
  "concurrentTasks": 1,
  "format": [
    {
      "dataId": "temperature,pressure,status,createdAt,enabled",
      "dataType": "Double,Integer,String,Date,Boolean",
      "randomBoolean": "1,1,1,1,1",
      "randomCondition": "20.0..35.0,900..1100,8,yyyyMMddHHmmssSSS,"
    }
  ],
  "value": []
}
```

### randomCondition 규칙

| type | randomCondition 예시 | 설명 |
| --- | --- | --- |
| String | `10` | 길이 10 랜덤 문자열 |
| String | `5..20` | 길이 5~20 랜덤 문자열 |
| Integer | `100` | 0~100 정수 |
| Integer | `10..20` | 10~20 정수 |
| Double | `100.5` | 0~100.5 실수, 소수점 3자리 반올림 |
| Double | `10.5..20.5` | 10.5~20.5 실수 |
| Boolean | 빈 값 | `true` 또는 `false` |
| Date | 빈 값 | `yyyyMMddHHmmssSSS` |
| Date | `yyyyMMddHHmmss` | 지정한 DateTimeFormatter 패턴 |

## 3. 포맷 + 값 목록 기반 대량 데이터 전송

`value`가 있으면 value row를 format의 `dataId` 순서에 맞춰 메시지로 만듭니다.
`messageCount`는 value row 전체를 몇 번 반복할지 의미합니다.

```http
POST http://localhost:8080/activemq/request/value-bulk
Content-Type: application/json
```

```json
{
  "tcName": "KE2D1Z11",
  "delayTime": 100,
  "repeatBoolean": true,
  "repeatTime": 0,
  "messageCount": 3,
  "concurrentTasks": 1,
  "format": [
    {
      "dataId": "temperature,pressure,status",
      "dataType": "Double,Integer,String",
      "randomBoolean": "0,0,0",
      "randomCondition": "20.0..35.0,900..1100,8"
    }
  ],
  "value": [
    {
      "0": "25.1,1001,OK",
      "1": "25.2,1002,OK",
      "2": "25.3,1003,WARN"
    }
  ]
}
```

## 4. 파일 메시지 반복 전송

서버 로컬 파일의 JSON fragment를 반복 전송합니다.
`repeatBoolean=false`여도 1회 전송합니다.

```http
POST http://localhost:8080/activemq/request/file/file-bulk
Content-Type: application/json
```

```json
{
  "tcName": "KE2D1Z11",
  "delayTime": 1000,
  "repeatBoolean": true,
  "repeatTime": 0,
  "messageCount": 100,
  "filePath": "c:/Project/",
  "fileName": "KE2D1Z11_format.txt",
  "concurrentTasks": 1
}
```

## 5. 포맷 파일 + 데이터 파일 전송

핵심 사용 방식입니다. `KE2D1Z11_format.txt`와 `KE2D1Z11_data.txt`를 읽어서 format 파일의 placeholder를 data 파일 값으로 치환한 뒤 ActiveMQ로 전송합니다.

```http
POST http://localhost:8080/activemq/request/file-data/file-data-bulk
Content-Type: application/json
```

```json
{
  "tcName": "KE2D1Z11",
  "delayTime": 100,
  "repeatBoolean": true,
  "repeatTime": 0,
  "messageCount": 100,
  "filePath": "c:/Project/",
  "formatFileName": "KE2D1Z11_format.txt",
  "dataFileName": "KE2D1Z11_data.txt",
  "concurrentTasks": 1
}
```

위 예시는 data 파일 전체를 100번 반복해서 전송합니다. data row가 3개이면 총 300개 메시지가 전송됩니다.

### 5-1. 순서 기반 data 파일

format file 예시:

```json
"temperature": "{{temperature}}",
"pressure": "{{pressure}}",
"status": "{{status}}"
```

data file 예시:

```csv
25.1,1001,OK
25.2,1002,OK
25.3,1003,WARN
```

### 5-2. 헤더 기반 data 파일

```csv
status,temperature,pressure
OK,25.1,1001
OK,25.2,1002
WARN,25.3,1003
```

컬럼 순서와 관계없이 placeholder 이름 기준으로 매핑됩니다.

### 5-3. quoted CSV

쉼표가 포함된 값은 큰따옴표로 감싸면 됩니다.

```csv
status,message,temperature
OK,"hello, world",25.1
WARN,"quoted ""text"" sample",25.2
```

## 6. JmsTemplate 기반 부하 테스트 모드

기존 `/activemq/request/{taskId}` 방식은 `concurrentTasks`만큼 task가 생기며, task별로 ActiveMQ connection/session/producer가 생성될 수 있습니다.
JmsTemplate 모드는 Spring `CachingConnectionFactory`를 사용해 session을 캐시하고, `workerCount` 작업자가 `messageCount`를 나눠 전송합니다.

```http
POST http://localhost:8080/activemq/request/jms-template-load/jms-load-1
Content-Type: application/json
```

```json
{
  "tcName": "KP1D0012",
  "messageCount": 10000,
  "delayTime": 0,
  "workerCount": 20,
  "topic": "topic.cep.output.0",
  "payload": "{\"source\":\"jms-template-load\",\"type\":\"LOAD_TEST\"}"
}
```

이 모드는 “동시 producer 1000개”가 아니라, 제한된 worker/session cache 기반으로 메시지 처리량을 측정하는 데 더 적합합니다.

## 7. Task API

| method | path | 설명 |
| --- | --- | --- |
| GET | `/activemq/task/running-tasks` | 실행 중인 task 목록 조회 |
| GET | `/activemq/task/statuses` | 전체 task 상태 조회 |
| GET | `/activemq/task/statuses/running` | RUNNING 상태 task 조회 |
| DELETE | `/activemq/task/statuses/finished` | 완료된 task 상태 이력 삭제 |
| POST | `/activemq/task/cancel-tasks` | 전체 task 취소 요청 |
| POST | `/activemq/task/cancel-task/{taskId}` | 특정 task 취소 요청 |

Task 상태는 `RUNNING`, `SUCCESS`, `FAILED`, `CANCELLED`를 가집니다. 상태 이력은 `simulator.task.max-history` 설정값에 따라 오래된 완료 이력부터 정리됩니다.

## 8. 운영 로그 보안

메시지 로그 출력 시 `id`, `username`, `user`, `pw`, `password` 패턴은 `****`로 마스킹합니다.

## 테스트

```bash
mvn test
```

CI는 GitHub Actions에서 Java 21 기준으로 `mvn -B test`를 수행합니다.
