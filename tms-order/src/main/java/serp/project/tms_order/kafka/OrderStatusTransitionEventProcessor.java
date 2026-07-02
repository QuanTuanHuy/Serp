/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.tms_order.dto.request.InternalOrderStatusTransitionRequest;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kafka.event.OrderStatusTransitionEvent;
import serp.project.tms_order.service.OrderTransitionService;

@Component
@RequiredArgsConstructor
public class OrderStatusTransitionEventProcessor {

    private final ObjectMapper objectMapper;
    private final OrderTransitionService orderTransitionService;

    public Long process(String payload) throws Exception {
        OrderStatusTransitionEvent event = objectMapper.readValue(payload, OrderStatusTransitionEvent.class);
        validate(event);
        orderTransitionService.applyTransitions(event.getRequest(), event.getTenantId());
        return event.getTenantId();
    }

    public Long extractTenantId(String payload) {
        try {
            OrderStatusTransitionEvent event = objectMapper.readValue(payload, OrderStatusTransitionEvent.class);
            return event == null ? null : event.getTenantId();
        } catch (Exception exception) {
            return null;
        }
    }

    private void validate(OrderStatusTransitionEvent event) {
        InternalOrderStatusTransitionRequest request = event == null ? null : event.getRequest();
        if (event == null || event.getTenantId() == null || request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }
}
