/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import serp.project.account.core.domain.dto.message.CreateNotificationEvent;
import serp.project.account.core.domain.dto.message.SendEmailRequest;

public interface INotificationService {
    void sendNotification(CreateNotificationEvent event);

    void sendEmail(Long actorId, Long tenantId, String aggregateType, Long aggregateId, SendEmailRequest request);
}
