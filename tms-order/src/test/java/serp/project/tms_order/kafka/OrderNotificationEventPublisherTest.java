/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.enums.PaymentStatus;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderNotificationEventPublisherTest {
    private static final String TOPIC = "serp.notification.user.events";

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OrderNotificationEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new OrderNotificationEventPublisher(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(publisher, "userNotificationTopic", TOPIC);
    }

    @Test
    void publishOrderConfirmedSendsCreateNotificationEvent() throws Exception {
        when(kafkaTemplate.send(eq(TOPIC), eq("ORD-001"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publishOrderConfirmed(order("42"));

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq(TOPIC), eq("ORD-001"), payloadCaptor.capture());

        JsonNode payload = objectMapper.readTree(payloadCaptor.getValue());
        assertEquals("tms-order.order-confirmed.1", payload.path("meta").path("id").asText());
        assertEquals("notification.create.requested", payload.path("meta").path("type").asText());
        assertEquals("tms-order", payload.path("meta").path("source").asText());
        assertEquals("1", payload.path("meta").path("v").asText());
        assertEquals(42L, payload.path("data").path("userId").asLong());
        assertEquals(9L, payload.path("data").path("tenantId").asLong());
        assertEquals("Order confirmed", payload.path("data").path("title").asText());
        assertEquals("SUCCESS", payload.path("data").path("type").asText());
        assertEquals("TMS", payload.path("data").path("category").asText());
        assertEquals("IN_APP", payload.path("data").path("deliveryChannels").get(0).asText());
        assertEquals("/first-mile/orders", payload.path("data").path("actionUrl").asText());
        assertEquals("TMS_ORDER", payload.path("data").path("entityType").asText());
        assertEquals(1L, payload.path("data").path("entityId").asLong());
        assertEquals("ORD-001", payload.path("data").path("metadata").path("orderCode").asText());
    }

    @Test
    void publishOrderCancelledSkipsWhenCreatedByIsNotNumeric() {
        publisher.publishOrderCancelled(order("system"));

        verifyNoInteractions(kafkaTemplate);
    }

    private Order order(String createdBy) {
        return Order.builder()
                .id(1L)
                .orderCode("ORD-001")
                .customerOrderCode("CUS-001")
                .status(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.UNPAID)
                .tenantId(9L)
                .createdBy(createdBy)
                .build();
    }
}
