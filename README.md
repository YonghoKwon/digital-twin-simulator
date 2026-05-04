# Digital Twin Simulator

REST API 요청으로 특정 포맷의 데이터를 대량 생성한 뒤 ActiveMQ Artemis Topic으로 전송하는 시뮬레이터입니다.

## Swagger UI

- http://localhost:8080/swagger-ui/

## 실행

```bash
mvn spring-boot:run
```

## 공통 요청 필드

| field | 설명 | 예시 |
| --- | --- | --- |
| activeMQIp | ActiveMQ Artemis broker URL | `amqp://localhost:61616` |
| id | ActiveMQ 계정 | `admin` |
| pw | ActiveMQ 비밀번호 | `admin` |
| topic | 전송할 topic | `jms.topic.cep.output.9` |
| tcName | transaction/message id | `KE2D1Z11` |
| delayTime | 메시지 간 대기 시간(ms) | `2000` |
| repeatBoolean | 반복 전송 여부 | `true` |
| repeatTime | `messageCount`가 없을 때 전송 지속 시간(ms) | `10000` |
| messageCount | 명시적 생성 횟수. 지정 시 `repeatTime`보다 우선 | `1000` |
| concurrentTasks | 동시 실행 task 수. 1~2000 | `10` |

## 1. 포맷 기반 랜덤 대량 데이터 생성

`value`를 비우거나 생략하면 `format` 정의에 맞춰 랜덤 데이터를 생성합니다.

```http
POST http://localhost:8080/activemq/request/random-bulk
Content-Type: application/json
```

```json
{
  "activeMQIp": "amqp://localhost:61616",
  "id": "admin",
  "pw": "admin",
  "topic": "jms.topic.cep.output.9",
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

## 2. 포맷 + 값 목록 기반 대량 데이터 전송

`value`가 있으면 value의 row를 format의 `dataId` 순서에 맞춰 메시지로 만듭니다.
`messageCount`는 value row 전체를 몇 번 반복할지 의미합니다.

```http
POST http://localhost:8080/activemq/request/value-bulk
Content-Type: application/json
```

```json
{
  "activeMQIp": "amqp://localhost:61616",
  "id": "admin",
  "pw": "admin",
  "topic": "jms.topic.cep.output.9",
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

위 예시는 value row 3개를 3번 반복하므로 총 9개 메시지를 전송합니다.

## 3. 파일 메시지 반복 전송

서버 로컬 파일의 JSON fragment를 반복 전송합니다.
`repeatBoolean=false`여도 1회 전송합니다.

```http
POST http://localhost:8080/activemq/request/file/file-bulk
Content-Type: application/json
```

```json
{
  "activeMQIp": "amqp://localhost:61616",
  "id": "admin",
  "pw": "admin",
  "topic": "jms.topic.cep.output.9",
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

## 4. 포맷 파일 + 데이터 파일 전송

핵심 사용 방식입니다. `KE2D1Z11_format.txt`와 `KE2D1Z11_data.txt`를 읽어서 format 파일의 placeholder를 data 파일 값으로 치환한 뒤 ActiveMQ로 전송합니다.

```http
POST http://localhost:8080/activemq/request/file-data/file-data-bulk
Content-Type: application/json
```

```json
{
  "activeMQIp": "amqp://localhost:61616",
  "id": "admin",
  "pw": "admin",
  "topic": "jms.topic.cep.output.9",
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

### 4-1. 순서 기반 data 파일

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

이 경우 첫 번째 컬럼은 `temperature`, 두 번째 컬럼은 `pressure`, 세 번째 컬럼은 `status`에 들어갑니다.

### 4-2. 헤더 기반 data 파일

이제 data 파일 첫 줄에 header를 둘 수 있습니다. header에 placeholder 이름이 포함되어 있으면 컬럼 순서가 달라도 이름 기준으로 매핑됩니다.

format file 예시:

```json
"temperature": "{{temperature}}",
"pressure": "{{pressure}}",
"status": "{{status}}"
```

data file 예시:

```csv
status,temperature,pressure
OK,25.1,1001
OK,25.2,1002
WARN,25.3,1003
```

이 경우 컬럼 순서와 관계없이 다음처럼 매핑됩니다.

```text
temperature = 25.1
pressure = 1001
status = OK
```

## Task API

| method | path | 설명 |
| --- | --- | --- |
| GET | `/activemq/task/running-tasks` | 실행 중인 task 목록 조회 |
| POST | `/activemq/task/cancel-tasks` | 전체 task 취소 요청 |
| POST | `/activemq/task/cancel-task/{taskId}` | 특정 task 취소 요청 |

## 테스트

```bash
mvn test
```

CI는 GitHub Actions에서 Java 21 기준으로 `mvn -B test`를 수행합니다.
