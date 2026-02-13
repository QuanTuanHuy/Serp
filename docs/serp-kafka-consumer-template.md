# SERP Kafka Consumer Template

## Listener Template (`@KafkaListener` + typed parser)

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
public class SampleConsumer {
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    private final SerpKafkaRecordParser recordParser;
    private final SampleUseCase sampleUseCase;

    @KafkaListener(topics = "sample.events", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(ConsumerRecord<String, String> record) {
        SampleEvent event = recordParser.parse(record, SampleEvent.class);
        SerpKafkaConsumerMetadata metadata =
                SerpKafkaConsumerMetadata.fromRecord(record, CORRELATION_HEADER);

        log.info("Kafka consume topic={}, partition={}, offset={}, key={}, correlationId={}",
                metadata.getTopic(),
                metadata.getPartition(),
                metadata.getOffset(),
                metadata.getKey(),
                metadata.getCorrelationId());

        // Throwing exception lets starter retry/DLT policy handle failures.
        sampleUseCase.process(event);
    }
}
```

## Generic Payload Template

```java
Map<String, Object> payload = recordParser.parse(
        record,
        new ParameterizedTypeReference<Map<String, Object>>() {
        });
```

## Optional Manual Ack Template
Use this only if business flow explicitly requires manual commit control.

```java
@KafkaListener(topics = "sample.events", groupId = "${spring.kafka.consumer.group-id}")
public void handle(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
    try {
        SampleEvent event = recordParser.parse(record, SampleEvent.class);
        sampleUseCase.process(event);
        acknowledgmentHelper.acknowledge(acknowledgment);
    } catch (Exception ex) {
        acknowledgmentHelper.nack(acknowledgment, Duration.ofSeconds(2));
        throw ex;
    }
}
```

## Failure Strategy
1. Parse error: throw `IllegalArgumentException` (default non-retryable -> DLT).
2. Validation error: throw non-retryable exception.
3. Transient dependency/network/database error: throw retryable exception.

## Do / Don't
Do:
- Keep listener idempotent where possible.
- Include topic/partition/offset/key/correlation-id in logs.
- Let starter-level error handler own retry and DLT behavior.

Don't:
- Don't call `acknowledge()` in `finally`.
- Don't hide processing exceptions with log-only handling.
- Don't parse JSON manually when parser helper can do typed conversion.
