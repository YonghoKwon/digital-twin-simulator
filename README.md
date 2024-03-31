# ActiveMQ Simulator

## REST API 를 통한 데이터 전송을 시뮬레이션 하는 프로그램

### swagger-ui url
- http://localhost:8080/swagger-ui/

### 0. 필수 항목들
- activeMQIp : activeMQ ip & port ex) amqp://220.80.16.171:61616
- id : ex) artemis or admin
- pw : ex) artemis or admin
- topic : topic name ex) jms.topic.cep.output.9
- tcName : tc name ex) KE2D1Z11
- delayTime : 메시지의 딜레이 타임 ex) 2000 (2초)

### 1. repeatBoolean(반복유무)
- TRUE : repeatTime( / 1000) 만큼 반복
- FALSE : 

### 2. value(전송할 데이터)
- size < 1 : random 조건에 맞게 데이터 생성
- size >= 1 : value에 지정된 데이터로 생성

### 3. Format(항목 id, type, random value 유무, random value 범위)

### REST API 종류
- /activemq/{taskId} : taskId에 해당하는 task를 실행하여 activeMQ artemis에 message 전송
 > ex) http://localhost:8080/activemq/test
 > @PathVariable String taskId : taskId의 앞 부분
 > @RequestBody ActiveMQRequestDto activeMQRequestDto : activeMQ ip, id, pw 등 정보
 > return 값 : taskId-{UUID}

- /cancel-task/{taskId} : taskId에 해당하는 task를 취소
 > ex) http://localhost:8080/cancel-task/test-{UUID}
 > @PathVariable String taskId : taskId-{UUID}

- /running-task : 현재 실행중인 task 목록 조회
 > ex) http://localhost:8080/running-task
 > return 값 : ActiveMQTaskInfoDto(taskId-{UUID}, taskCancelApiUrl)