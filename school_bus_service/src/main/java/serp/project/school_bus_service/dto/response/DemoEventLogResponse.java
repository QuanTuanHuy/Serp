package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DemoEventLogResponse extends BaseResponse {
    private Long demoSessionId;
    private String eventType;
    private LocalDateTime eventTime;
    private String payloadJson;
}

