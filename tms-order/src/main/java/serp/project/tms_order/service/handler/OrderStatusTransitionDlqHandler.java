/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.handler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.tms_order.kafka.OrderStatusTransitionEventProcessor;

@Component
public class OrderStatusTransitionDlqHandler implements DlqMessageHandler {

    private final String supportedTopic;
    private final OrderStatusTransitionEventProcessor eventProcessor;

    public OrderStatusTransitionDlqHandler(
            @Value("${app.kafka.topics.order-status-transition:ORDER_STATUS_TRANSITIONS}") String supportedTopic,
            OrderStatusTransitionEventProcessor eventProcessor
    ) {
        this.supportedTopic = supportedTopic;
        this.eventProcessor = eventProcessor;
    }

    @Override
    public String getSupportedTopic() {
        return supportedTopic;
    }

    @Override
    public void process(String payload) throws Exception {
        eventProcessor.process(payload);
    }
}
