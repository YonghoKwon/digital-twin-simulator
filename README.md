# ActiveMQ Simulator

## REST API 를 통한 데이터 전송을 시뮬레이션 하는 프로그램

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

### 3. 