# 모의 주문 시스템

Hexagonal 아키텍처와 Choreography Saga 패턴을 활용하여 구현한 모의 주문 시스템입니다. 아래 Udemy 강의를 수강한 내용을 토대로 구현하였습니다.

https://www.udemy.com/course/microservices-clean-architecture-ddd-saga-outbox-kafka-kubernetes/

SQL로 mocking 데이터를 생성하여 해당 데이터로만 실행 테스트를 진행했습니다. 또한, 코어 서비스가 아닌 customer-service 및 식당/음식 CRUD API의
구현은 생략했습니다. 주문에 관련된 2개의 REST API만 구현하여, 아직은 mocking 데이터를 활용하여 주문 및 주문 확인만 가능한 상태입니다.

추후 리팩토링과 테스트를 추가하고 customer-service를 구현하여 더욱 완성도 있는 프로젝트로 만들 예정입니다.

## 사용 기술

- Java 17 (Java 11까지의 기능만 활용)
- PostgreSQL
- Spring Boot
- Spring Data JPA
- Kafka
- Docker

## 인프라 환경 실행

`infra/docker-compose`

위 디렉토리에 PostgreSQL와 Kafka에 대한 docker compose 파일이 위치있습니다.

아래 순서로 실행시키면 됩니다.

1. `docker compose -f postgresql.yml up -d`
2. `docker compose -f common.yml -f zookeeper.yml up -d`
3. `docker compose -f common.yml -f kafka_cluster.yml up -d`
4. `docker compose -f init_kafka.yml up`

`init_kafka.yml`는 topic을 생성시키는 compose 파일이므로 처음에 한번만 실행시키면 됩니다.

**주의할 점**

Kafka broker의 `/var/lib/kafka/data` 에 볼륨 마운트 설정을 해놓았으므로, `infra/docker-compose` 내부에 반드시
`volumes/kafka/broker-1` , `volumes/kafka/broker-2`, `volumes/kafka/broker-3` 폴더를 생성해주어야합니다.

그렇게 하지 않는다면, docker가 root 권한으로 폴더를 생성하기 때문에 Kafka broker으로의 마운트가 실패하고 컨테이너 실행도 실패하게 됩니다.

## 프로젝트 구조

![Order_Project_Graph.jpg](img%2FOrder_Project_Graph.jpg)
![Restaurant_Project_Graph.jpg](img%2FRestaurant_Project_Graph.jpg)
![Payment_Project_Graph.jpg](img%2FPayment_Project_Graph.jpg)
각 프로젝트는 헥사고날 아키텍처로 구성되어 있고, 다음과 같은 서브 프로젝트로 이루어져 있습니다.

### {domain name}-domain-core

엔티티와 최소단위의 비즈니스 로직이 구현되어 있는 서브 프로젝트입니다.

### {domain name}-application-service

domain-core 서브 프로젝트를 참조하여, 여러 엔티티의 비즈니스 로직을 조합하여 복잡한 비즈니스 로직이 구현되어 있는 서브 프로젝트입니다.

Input adapter는 해당 프로젝트를 참조하여 구현됩니다. 반대로, output adatper의 경우 해당 프로젝트가 output port를 통해 참조하여
비즈니스 로직을 구현합니다.

### {domain name}-application

Input adapter가 위치한 서브 프로젝트입니다.

해당 프로젝트에서는 order 도메인에만 application 서브 프로젝트가 존재하고,
REST API Controller가 위치해 있습니다.

### {domain name}-dataaccess

데이터 접근을 위한 output adapter가 위치한 서브 프로젝트입니다.

JPA entity와 repository가 구현되어 있습니다.

### {domain name}-messaging

메세지 큐를 위한 adapter가 위치한 서브 프로젝트입니다.

해당 프로젝트에서는 Kafka를 사용했습니다.
Kafka Listener는 input adapter이고 Kakfa Publisher는 output adapter라 헥사고날 아키텍처 측면에서는 다른 분류이지만
메세지 큐에는 두 가지 요소가 항상 같이 존재하므로 해당 서브 프로젝트에 같이 위치시켰습니다.

### {domain name}-container

Spring Boot와 같은 프레임워크가 위치한 서브 프로젝트입니다.

해당 서브 프로젝트에서는 다른 모든 서브 프로젝트를 참조하여 어플리케이션을 실행시킵니다. 이 프로젝트에서는 Spring Boot를 사용했습니다.

## REST API Spec

---

**POST /orders**

- Request Body

``````
{
  "customerId": "49f72dbd-ae1d-4f6b-b17c-122d402e55c5",  // 고객 ID (UUID 형식)
  "restaurantId": "d215b5f8-0249-4dc5-89a3-51fd148cfb45",  // 음식점 ID (UUID 형식)
  "orderAddress": {
    "street": "street_1",  // 도로명 주소
    "postalCode": "1000AB",  // 우편번호
    "city": "Amsterdam"  // 도시명
  },
  "price": 200.00,  // 총 주문 가격
  "items": [  // 주문 항목 배열
    {
      "productId": "d215b5f8-0249-4dc5-89a3-51fd148cfb48",  // 제품 ID (UUID 형식)
      "quantity": 1,  // 수량
      "price": 50.00,  // 개당 가격
      "subTotal": 50.00  // 합계
    },
    {
      "productId": "d215b5f8-0249-4dc5-89a3-51fd148cfb48",  // 제품 ID (UUID 형식)
      "quantity": 3,  // 수량
      "price": 50.00,  // 개당 가격
      "subTotal": 150.00  // 합계
    }
  ]
}
``````

- 성공 응답 (HTTP 상태 코드: 200 OK)

``````
{
    "orderTrackingId": "6688de1d-a5ba-4f7f-af17-4a16fd7f833b",
    "orderStatus": "PENDING",
    "message": "Order Created Successfully"
}
``````

- 실패 응답 (HTTP 상태 코드: 400 BAD REQUEST)

``````
{
    "code": "Bad Request",
    "message": {실패 사유}
}
``````

---
**GET /orders/{orderTrackingId}**

- 요청 예시

``````
GET /orders/f410ab7d-8678-49b4-84d6-35c00933932e
``````

- 성공 응답 (HTTP 상태 코드: 200 OK)

``````
{
    "orderTrackingId": "f410ab7d-8678-49b4-84d6-35c00933932e",  // 주문 추적 ID (UUID 형식)
    "orderStatus": "APPROVED",  // 주문 상태 (예: APPROVED, PAID, APPROVED)
    "failureMessages": [  // 실패 메시지 배열 (주문이 성공적으로 처리되면 빈 배열)
        ""
    ]
}
``````

- 실패 응답 (HTTP 상태 코드: 400 BAD REQUEST)

``````
{
    "code": "Not Found",
    "message": "Could not find order with tracking idf430ab7d-8678-49b4-84d6-35c00933932e"
}
``````

## 메세지 플로우

![saga-2.png](img%2Fsaga-2.png)