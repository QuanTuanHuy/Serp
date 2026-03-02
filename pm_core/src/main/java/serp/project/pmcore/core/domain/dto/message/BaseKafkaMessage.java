/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.dto.message;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseKafkaMessage<T> {
    private T data;
    private Meta meta;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        @JsonProperty("id")
        private String eventId;

        @JsonProperty("type")
        private String eventType;

        @JsonProperty("source")
        private String source;

        @JsonProperty("v")
        private String version;

        @JsonProperty("ts")
        private Long timestamp;

        @JsonProperty("traceId")
        private String traceId;

        @JsonProperty("correlationId")
        private String correlationId;

        @JsonProperty("tenantId")
        private Long tenantId;

        @JsonProperty("actorId")
        private Long actorId;

        @JsonProperty("aggregateType")
        private String aggregateType;

        @JsonProperty("aggregateId")
        private String aggregateId;
    }

    public static <T> BaseKafkaMessage<T> of(
            String source,
            String eventType,
            Long tenantId,
            Long actorId,
            String aggregateType,
            String aggregateId,
            T data) {
        return BaseKafkaMessage.<T>builder()
                .data(data)
                .meta(Meta.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType(eventType)
                        .source(source)
                        .version("1.0")
                        .timestamp(Instant.now().toEpochMilli())
                        .traceId(UUID.randomUUID().toString())
                        .correlationId(UUID.randomUUID().toString())
                        .tenantId(tenantId)
                        .actorId(actorId)
                        .aggregateType(aggregateType)
                        .aggregateId(aggregateId)
                        .build())
                .build();
    }

    public static <T> BaseKafkaMessage<T> of(
            String source,
            String eventType,
            Long tenantId,
            Long actorId,
            String aggregateType,
            String aggregateId,
            String correlationId,
            T data) {
        BaseKafkaMessage<T> message = of(source, eventType, tenantId, actorId,
                aggregateType, aggregateId, data);
        message.getMeta().setCorrelationId(correlationId);
        return message;
    }
}
