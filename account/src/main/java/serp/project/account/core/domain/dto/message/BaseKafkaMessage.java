package serp.project.account.core.domain.dto.message;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BaseKafkaMessage<T> {
    private T data;
    private Meta meta;

    @Builder
    @Data
    public static class Meta {
        @JsonProperty("id")
        private String eventId;

        @JsonProperty("type")
        private String eventType;

        private String source;

        @JsonProperty("v")
        private String version;

        @JsonProperty("ts")
        private Long timestamp;

        private String traceId;
    }

    public static <T> BaseKafkaMessage<T> of(String source, String eventType, T data) {
        String eventId = UUID.randomUUID().toString();
        String traceId = UUID.randomUUID().toString();
        String version = "1.0";
        
        return BaseKafkaMessage.<T>builder()
                .data(data)
                .meta(Meta.builder()
                        .eventId(eventId)
                        .eventType(eventType)
                        .source(source)
                        .version(version)
                        .timestamp(Instant.now().toEpochMilli())
                        .traceId(traceId)
                        .build())
                .build();
    }
}
