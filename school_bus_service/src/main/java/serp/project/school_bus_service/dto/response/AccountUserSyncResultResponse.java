package serp.project.school_bus_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response returning statistics and status of a manual sync execution.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountUserSyncResultResponse {

    private String syncCode;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime finishedAt;
    
    private int successCount;
    
    private int failedCount;
    
    private int skippedCount;
    
    private String message;
    
    private List<ErrorItem> errors;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorItem {
        private Long accountUserId;
        private String email;
        private String reason;
    }

}
