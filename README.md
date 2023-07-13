# Mock Ordering System

클린 아키텍처와 DDD, 그리고 이벤트 큐를 활용하는 사가 패턴을 연습하고자 프로젝트를 만들어보았습니다.

## Skills

- Java 17 (Java 11까지의 기능만 활용)
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

1. `docker compose -f postgresql.yml up -d`
2. `docker compose -f common.yml -f zookeeper.yml up -d`
3. `docker compose -f common.yml -f kafka_cluster.yml up -d`

KafkaAdmin과 Topic을 Bean으로 등록해두었으므로, 어플리케이션 실행시 자동으로 topic이 등록됩니다.

**주의할 점**

Kafka broker의 `/var/lib/kafka/data` 에 볼륨 마운트 설정을 해놓았으므로, `infra/docker-compose` 내부에 반드시
`volumes/kafka/broker-1` , `volumes/kafka/broker-2`, `volumes/kafka/broker-3` 폴더를 생성해주어야합니다.

그렇게 하지 않는다면, docker가 root 권한으로 폴더를 생성하기 때문에 Kafka broker으로의 마운트가 실패하고 컨테이너 실행도 실패하게 됩니다.

## Details

- [API specification](docs/api_spec.md)
- [Message flow](docs/message_flow.md)
- [Class diagram](docs/code_project_structure.md)
