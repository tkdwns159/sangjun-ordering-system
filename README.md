# Mock Ordering System

- 주문 서비스
- 결제 서비스
- 식당 서비스
- 고객 서비스

위에 나열된 4개의 서비스로 구성된 모의 주문 시스템입니다. 각각의 서비스가 독립된 인스턴스로 실행되어 Kafka를 통해 이벤트 메세지를 주고받도록 구현했습니다.

식당 서비스는 구현 및 수정이 더 필요한 상태이며, 고객 서비스는 설계단계부터 진행해야하는 상태입니다. 또한, 처음 구현으로부터 달라진 부분이 크기때문에 카프카 메세지 포맷의 수정도 필요합니다.

## Skills

- Java 17
- PostgreSQL
- Spring Boot
- Spring Data JPA
- Kafka
- Mapstruct
- Docker

## 인프라 환경 실행

`infra/docker-compose`

위 디렉토리에 PostgreSQL, ZooKeeper, Kafka에 대한 docker compose 파일이 위치해있습니다.

아래 순서로 실행시키면 됩니다.

1. `docker compose -f zookeeper.yml up -d`
2. `docker compose up -d`

KafkaAdmin과 Topic을 Bean으로 등록해두었으므로, 어플리케이션 실행시 자동으로 topic이 등록됩니다.

**주의사항**

Kafka broker의 `/var/lib/kafka/data` 에 볼륨 마운트 설정을 해놓았으므로, `infra/docker-compose` 내부에 반드시
`volumes/kafka/broker-1` , `volumes/kafka/broker-2`, `volumes/kafka/broker-3` 폴더를 생성해주어야합니다.

위의 주의사항을 따르지 않는다면, docker가 root 권한으로 위의 폴더들을 생성하기 때문에 Kafka broker으로의 마운트가 실패하고 컨테이너 실행도 실패하게 됩니다.

## Details

- [서버 구조](img/server_architecture.png)
- [주문 로직 흐름 순서도](img/flow_chart.png)
- [패키지-클래스 관계도](img/package_and_class_relationship_diagram.png)
- [메시지 흐름 순서도](docs/message_flow.md)
- [API 명세](docs/api_spec.md)
