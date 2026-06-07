/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.second_mile.kafka.event.HubPostOfficeSyncEvent;
import serp.project.second_mile.service.HubPostOfficeInboundSyncService;

@Component
@RequiredArgsConstructor
public class HubPostOfficeSyncDlqHandler implements DlqMessageHandler {

    private final ObjectMapper objectMapper;
    private final HubPostOfficeInboundSyncService hubPostOfficeInboundSyncService;

    @Value("${app.kafka.topics.sync-hub-post-office:HUB_POST_OFFICE_SYNC}")
    private String topic;

    @Override
    public String getSupportedTopic() {
        return topic;
    }

    @Override
    public void process(String payload) throws Exception {
        HubPostOfficeSyncEvent event = objectMapper.readValue(payload, HubPostOfficeSyncEvent.class);
        hubPostOfficeInboundSyncService.applyFirstMileKafkaEvent(event);
    }
}
