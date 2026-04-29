/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.second_mile.kafka.event.UserSyncEvent;
import serp.project.second_mile.service.UserSyncService;

@Component
@RequiredArgsConstructor
public class SyncUserDlqHandler implements DlqMessageHandler{
    private final ObjectMapper objectMapper;
    private final UserSyncService userSyncService;

    @Value("${app.kafka.topics.sync-user:SYNC_USER}")
    private String topic;

    @Override
    public String getSupportedTopic() {
        return topic;
    }

    @Override
    public void process(String payload) throws Exception {
        UserSyncEvent event = objectMapper.readValue(payload, UserSyncEvent.class);
        userSyncService.syncUser(event);
    }
}
