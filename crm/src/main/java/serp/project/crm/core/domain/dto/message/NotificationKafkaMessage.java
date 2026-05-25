/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationKafkaMessage {

    private NotificationMessageMetadata meta;
    private Object data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationMessageMetadata {
        private String id;
        private String type;
        private String source;
        private String v;
        private Long ts;
        private String traceId;
    }
}
