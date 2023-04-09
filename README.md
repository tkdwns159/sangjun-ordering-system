# 모의 주문 시스템
Hexagonal 아키텍처와 Choreography Saga 패턴을 활용하여 구현한 모의 주문 시스템입니다.

아래 Udemy 강의를 수강한 내용을 토대로 구현하였습니다.
https://www.udemy.com/course/microservices-clean-architecture-ddd-saga-outbox-kafka-kubernetes/

## 프로젝트 구조
![Order_Project_Graph.jpg](img%2FOrder_Project_Graph.jpg)
![Restaurant_Project_Graph.jpg](img%2FRestaurant_Project_Graph.jpg)
![Payment_Project_Graph.jpg](img%2FPayment_Project_Graph.jpg)
각 프로젝트는 헥사고날 아키텍처로 구성되어 있습니다.
### Domain-core
비즈니스 로직이 위치한 계층입니다.
### Application-service
Domain-core의 API를 활용하여 복잡한 비즈니스 로직이 구현된 계층입니다.
### Application
REST API Controller가 위치한 계층입니다. (Input Adapter)
### Dataaccess
데이터 접근 계층입니다. Entity와 JPA repository가 위치한 계층입니다. (Output Adapter)
### Messaging
카프카 리스너 (Input Adapter), 카프카 퍼블리셔 (Output Adapter)가 위치한 계층입니다.

## 메세지 플로우
![saga-2.png](img%2Fsaga-2.png)