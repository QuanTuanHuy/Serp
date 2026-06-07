/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.client.kafka;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaPublisherAdapterTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private JsonUtils jsonUtils;

    private KafkaPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new KafkaPublisherAdapter(kafkaTemplate, jsonUtils);
    }

    @Test
    void sendMessageSyncShouldPreserveRawJsonStringPayload() throws Exception {
        String topic = "serp.notification.user.events";
        String key = "2";
        String payload = "{\"meta\":{\"id\":\"pm-event-7000\"}}";

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(topic, 0),
                0L,
                0,
                0L,
                0,
                payload.length()
        );
        SendResult<String, String> sendResult = new SendResult<>(
                new ProducerRecord<>(topic, key, payload),
                metadata
        );
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        adapter.sendMessageSync(key, payload, topic);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq(topic), eq(key), payloadCaptor.capture());
        assertEquals(payload, payloadCaptor.getValue());
    }
}
