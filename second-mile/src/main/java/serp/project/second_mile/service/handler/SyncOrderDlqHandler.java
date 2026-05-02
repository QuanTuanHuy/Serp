/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.second_mile.kafka.event.OrderSyncEvent;
import serp.project.second_mile.service.OrderSyncService;

@Component
@RequiredArgsConstructor
public class SyncOrderDlqHandler implements DlqMessageHandler {
    private final ObjectMapper objectMapper;
    private final OrderSyncService orderSyncService;

    @Value("${app.kafka.topics.sync-order:SYNC_ORDER}")
    private String topic;

    @Override
    public String getSupportedTopic() {
        return topic;
    }

    @Override
    public void process(String payload) throws Exception {
        OrderSyncEvent event = objectMapper.readValue(payload, OrderSyncEvent.class);
        orderSyncService.syncOrder(event);
    }
}
