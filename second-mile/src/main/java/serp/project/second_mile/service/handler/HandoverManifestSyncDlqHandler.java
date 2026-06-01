/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.second_mile.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.second_mile.kafka.event.HandoverManifestSyncEvent;
import serp.project.second_mile.service.HandoverManifestService;

@Component
public class HandoverManifestSyncDlqHandler implements DlqMessageHandler {
    private final ObjectMapper objectMapper;
    private final HandoverManifestService handoverManifestService;
    private final String supportedTopic;

    public HandoverManifestSyncDlqHandler(
            ObjectMapper objectMapper,
            HandoverManifestService handoverManifestService,
            @Value("${app.kafka.topics.sync-handover-manifest:HANDOVER_MANIFEST_SYNC}") String supportedTopic
    ) {
        this.objectMapper = objectMapper;
        this.handoverManifestService = handoverManifestService;
        this.supportedTopic = supportedTopic;
    }

    @Override
    public String getSupportedTopic() {
        return supportedTopic;
    }

    @Override
    public void process(String payload) throws Exception {
        HandoverManifestSyncEvent event = objectMapper.readValue(payload, HandoverManifestSyncEvent.class);
        handoverManifestService.applyOutboundSync(event);
    }
}
