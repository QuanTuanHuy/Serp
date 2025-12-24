package serp.project.account.core.domain.dto.message;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;
import serp.project.account.core.domain.constant.Constants;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateNotificationEvent {
    private Long userId;
    private Long tenantId;
    private String title;
    private String message;
    @Builder.Default
    private String type = "INFO";

    private String category;
    @Builder.Default
    private String priority = "MEDIUM";

    @Builder.Default
    private String sourceService = Constants.SERVICE_NAME;
    
    private String actionUrl;
    private String actionType;

    private String entityType;
    private Long entityId;

    @Builder.Default
    private List<String> deliveryChanels = List.of("IN_APP");
    private Map<String, Object> metadata;
}
