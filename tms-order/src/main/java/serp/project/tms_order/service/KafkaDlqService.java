/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service;

public interface KafkaDlqService {
    void saveFailedMessage(String topic, String messageKey, String payload, String errorMessage, Long tenantId);

    void retryPendingMessages();
}
