# E-commerce Microservices System

This project contains several Spring Boot services orchestrated with Kafka for inter-service communication.
Docker Compose provides infrastructure dependencies like Kafka, Zookeeper, and PostgreSQL databases for each service.

## Running the stack

Use Docker Compose to start all required services:

```bash
docker-compose up -d
```

This starts Kafka, monitoring tools (Prometheus, Grafana, Jaeger), and separate PostgreSQL containers for each microservice.

A new **delivery_postgres** container was added for the delivery service and listens on port `5436`. Its data is persisted in the `delivery_data` volume.
