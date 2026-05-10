/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.first_mile.dto.message.HubPostOfficeSyncEvent;
import serp.project.first_mile.service.PostOfficeHubSyncService;

@Component
@RequiredArgsConstructor
public class HubPostOfficeSyncDlqHandler implements DlqMessageHandler {

    private final ObjectMapper objectMapper;
    private final PostOfficeHubSyncService postOfficeHubSyncService;

    @Value("${app.kafka.topics.sync-hub-post-office:HUB_POST_OFFICE_SYNC}")
    private String topic;

    @Override
    public String getSupportedTopic() {
        return topic;
    }

    @Override
    public void process(String payload) throws Exception {
        HubPostOfficeSyncEvent event = objectMapper.readValue(payload, HubPostOfficeSyncEvent.class);
        postOfficeHubSyncService.applyInboundHubPostOfficeEvent(event);
    }
}
