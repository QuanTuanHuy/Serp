# SERP Java Platform

Shared Java platform modules for SERP microservices.

## Modules

- `serp-java-bom`: shared dependency alignment.
- `serp-starter-core`: core cross-cutting auto configuration.
- `serp-starter-security-keycloak`: standard JWT + auth context integration.
- `serp-starter-kafka`: producer helper + consumer framework (retry/DLT/parser/interceptor).
- `serp-starter-redis`: cache + lock helper with key strategy.

## Build

```bash
mvn -f serp_java_platform/pom.xml clean package
```

## Publish to GitHub Packages

```bash
mvn -f serp_java_platform/pom.xml -DskipTests deploy
```

## Minimal usage example in a service

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.serp-project</groupId>
      <artifactId>serp-java-bom</artifactId>
      <version>0.1.0-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>io.github.serp-project</groupId>
    <artifactId>serp-starter-security-keycloak</artifactId>
  </dependency>
  <dependency>
    <groupId>io.github.serp-project</groupId>
    <artifactId>serp-starter-kafka</artifactId>
  </dependency>
  <dependency>
    <groupId>io.github.serp-project</groupId>
    <artifactId>serp-starter-redis</artifactId>
  </dependency>
</dependencies>
```

```yaml
serp:
  security:
    jwt:
      jwk-set-uri: ${KEYCLOAK_URL}/realms/serp/protocol/openid-connect/certs
    service:
      required-role: SERP_SERVICES
      allowed-client-ids:
        - serp-account
        - serp-crm
    public-urls:
      - method: POST
        pattern: /api/v1/auth/login
      - method: POST
        pattern: /api/v1/auth/refresh-token
  kafka:
    producer:
      default-topic: serp.events
      correlation-id-header: X-Correlation-Id
    consumer:
      listener:
        ack-mode: RECORD
        concurrency: 1
        sync-commits: true
      retry:
        max-attempts: 3
        initial-interval-ms: 1000
        multiplier: 2.0
        max-interval-ms: 10000
        non-retryable-exceptions:
          - org.springframework.kafka.support.serializer.DeserializationException
          - com.fasterxml.jackson.core.JsonProcessingException
          - java.lang.IllegalArgumentException
      dlt:
        enabled: true
        suffix: .dlt
        same-partition: true
        topic-mappings:
          critical.events: critical.events.dlt
      headers:
        correlation-id-header: X-Correlation-Id
        exception-stack-summary-header: x-serp-exception-stack
      observation-enabled: true
  redis:
    cache:
      prefix: serp:cache
      default-ttl-seconds: 300
    lock:
      prefix: serp:lock
      default-ttl-seconds: 30
```

## Kafka Consumer Quick Start

`serp-starter-kafka` now ships default `kafkaListenerContainerFactory` with:
- `DefaultErrorHandler` + exponential backoff
- DLT recoverer (same partition by default)
- non-retryable exception classification
- record interceptor for correlation-id to MDC
- observation toggle

Sample listener with typed parser:

```java
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.sample.ui.messaging;

import io.github.serp.platform.kafka.consumer.SerpKafkaConsumerMetadata;
import io.github.serp.platform.kafka.consumer.SerpKafkaRecordParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SampleKafkaConsumer {
    private final SerpKafkaRecordParser recordParser;

    @KafkaListener(topics = "sample.events", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(ConsumerRecord<String, String> record) {
        SampleEvent event = recordParser.parse(record, SampleEvent.class);
        SerpKafkaConsumerMetadata metadata =
                SerpKafkaConsumerMetadata.fromRecord(record, "X-Correlation-Id", null);

        log.info("Consume event topic={}, partition={}, offset={}, key={}",
                metadata.getTopic(), metadata.getPartition(), metadata.getOffset(), metadata.getKey());

        // Business logic; throw exception to trigger retry/DLT policy.
    }
}
```

For migration details and templates:
- `docs/serp-kafka-consumer-migration.md`
- `docs/serp-kafka-consumer-template.md`
