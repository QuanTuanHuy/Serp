/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.kafka.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationCreateRequestedEvent {
    private NotificationEventMetadata meta;
    private NotificationCreateRequestedData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NotificationEventMetadata {
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NotificationCreateRequestedData {
        private Long userId;
        private Long tenantId;
        private String title;
        private String message;
        private String type;
        private String category;
        private String priority;
        private String sourceService;
        private String sourceEventId;
        private String actionUrl;
        private String actionType;
        private String entityType;
        private Long entityId;
        private List<String> deliveryChannels;
        private Map<String, Object> metadata;
    }
}
