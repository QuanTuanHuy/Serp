/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrmKafkaMessage {

    private String cmd;

    private String errorCode;

    private String errorMessage;

    private Long timestamp;

    private Object data;

    private String source;

    private String replyTopic;

    /**
     * Entity ID (for tracking and debugging)
     * Examples: customerId, leadId, opportunityId
     */
    private String entityId;

    /**
     * Entity type
     * Examples: "CUSTOMER", "LEAD", "OPPORTUNITY"
     */
    private String entityType;

    private Long tenantId;

    /**
     * User who triggered the event
     */
    private Long userId;

    /**
     * Additional metadata (optional)
     */
    private Object metadata;

    public static CrmKafkaMessage success(String cmd, String entityId, String entityType, Object data) {
        return CrmKafkaMessage.builder()
                .cmd(cmd)
                .errorCode("00")
                .errorMessage("Success")
                .timestamp(System.currentTimeMillis())
                .data(data)
                .source("crm")
                .entityId(entityId)
                .entityType(entityType)
                .build();
    }

    public static CrmKafkaMessage error(String cmd, String entityId, String entityType,
            String errorCode, String errorMessage) {
        return CrmKafkaMessage.builder()
                .cmd(cmd)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .source("crm")
                .entityId(entityId)
                .entityType(entityType)
                .build();
    }
}
