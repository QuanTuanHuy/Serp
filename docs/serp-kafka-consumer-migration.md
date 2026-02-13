# SERP Kafka Consumer Migration Guide

## Goal
Migrate Java services from local `KafkaConsumerConfig` classes to `serp-starter-kafka` defaults.

## Scope
- Remove duplicated consumer factory and listener container config.
- Use `serp.kafka.consumer.*` for retry/DLT/listener policy.
- Keep broker/security properties under `spring.kafka.*`.

## Dependency
Add starter dependency in service `pom.xml`:

```xml
<properties>
  <serp.platform.version>0.1.0-SNAPSHOT</serp.platform.version>
</properties>

<dependencies>
  <dependency>
    <groupId>io.github.serp-project</groupId>
    <artifactId>serp-starter-kafka</artifactId>
    <version>${serp.platform.version}</version>
  </dependency>
</dependencies>
```

## Property Mapping

| Legacy | New |
| --- | --- |
| `spring.kafka.listener.ack-mode` | `serp.kafka.consumer.listener.ack-mode` |
| `factory.setConcurrency(...)` in Java config | `serp.kafka.consumer.listener.concurrency` |
| Manual `DefaultErrorHandler` setup | `serp.kafka.consumer.retry.*` |
| Manual DLT topic suffix logic | `serp.kafka.consumer.dlt.suffix` |
| Custom non-retryable exception list | `serp.kafka.consumer.retry.non-retryable-exceptions` |

Broker and deserializer settings remain in `spring.kafka.*`.

## Recommended Baseline

```yaml
serp:
  kafka:
    consumer:
      listener:
        ack-mode: RECORD
        concurrency: 2
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
```

## Service Code Changes
1. Delete local `KafkaConsumerConfig` unless extra custom behavior is required.
2. Keep `@KafkaListener` methods; do not acknowledge in `finally`.
3. Throw exceptions for failure paths so starter error handler can apply retry/DLT.
4. Use `SerpKafkaRecordParser` for typed payload parsing.

## Discuss Service Pilot Diff-Level Changes
1. `discuss_service/pom.xml`
`+ io.github.serp-project:serp-starter-kafka`

2. `discuss_service/src/main/resources/application.yaml`
`+ serp.kafka.consumer.listener/retry/dlt/headers`
`- spring.kafka.listener.ack-mode: manual`

3. `discuss_service/src/main/java/serp/project/discuss_service/ui/messaging/AbstractKafkaConsumer.java`
`- acknowledge() in finally`
`+ fail-fast by throwing exceptions on parse/resolve errors`

4. `discuss_service/src/main/java/serp/project/discuss_service/ui/messaging/DiscussKafkaConsumer.java`
`+ SerpKafkaRecordParser`
`+ SerpKafkaConsumerMetadata`
`- Acknowledgment parameter in listener methods`

## Do / Don't
Do:
- Throw exception when processing fails and let retry/DLT handle it.
- Keep listener logic small; move business logic to service/use case.
- Log topic/partition/offset/key/correlation-id on failures.

Don't:
- Don't acknowledge in `finally`.
- Don't swallow exceptions that should trigger retry/DLT.
- Don't duplicate `KafkaConsumerConfig` unless custom behavior is truly required.
