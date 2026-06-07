package serp.project.first_mile.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.first_mile.dto.message.SyncUserFirstMileEvent;
import serp.project.first_mile.service.PostOfficeStaffSyncService;

@Component
@RequiredArgsConstructor
public class SyncUserFirstMileDlqHandler implements DlqMessageHandler {

    private final ObjectMapper objectMapper;
    private final PostOfficeStaffSyncService postOfficeStaffSyncService;

    @Value("${app.kafka.topics.sync-user-first-mile:SYNC_USER}")
    private String topic;

    @Override
    public String getSupportedTopic() {
        return topic;
    }

    @Override
    public void process(String payload) throws Exception {
        SyncUserFirstMileEvent event = objectMapper.readValue(payload, SyncUserFirstMileEvent.class);
        postOfficeStaffSyncService.syncUser(event);
    }
}
