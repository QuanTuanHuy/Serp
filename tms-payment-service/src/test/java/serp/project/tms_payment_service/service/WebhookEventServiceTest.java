/*
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */
package serp.project.tms_payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import serp.project.tms_payment_service.repository.WebhookEventRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebhookEventServiceTest {

    @Test
    void processRetryableEventsDispatchesThroughTransactionalProxy() {
        WebhookEventRepository webhookEventRepository = mock(WebhookEventRepository.class);
        WebhookEventService self = mock(WebhookEventService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<WebhookEventService> selfProvider = mock(ObjectProvider.class);
        WebhookEventService service = new WebhookEventService(
                webhookEventRepository,
                new ObjectMapper(),
                selfProvider
        );

        when(webhookEventRepository.findRetryableEventIds(any())).thenReturn(List.of(14L));
        when(selfProvider.getObject()).thenReturn(self);

        service.processRetryableEvents(100);

        verify(self).dispatchEvent(14L);
    }
}
