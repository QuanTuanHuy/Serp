/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.first_mile.dto.message.HandoverManifestSyncEvent;
import serp.project.first_mile.service.PostOfficeHandoverManifestService;

@Component
public class HandoverManifestSyncDlqHandler implements DlqMessageHandler {
    private final ObjectMapper objectMapper;
    private final PostOfficeHandoverManifestService postOfficeHandoverManifestService;
    private final String supportedTopic;

    public HandoverManifestSyncDlqHandler(
            ObjectMapper objectMapper,
            PostOfficeHandoverManifestService postOfficeHandoverManifestService,
            @Value("${app.kafka.topics.sync-handover-manifest:HANDOVER_MANIFEST_SYNC}") String supportedTopic
    ) {
        this.objectMapper = objectMapper;
        this.postOfficeHandoverManifestService = postOfficeHandoverManifestService;
        this.supportedTopic = supportedTopic;
    }

    @Override
    public String getSupportedTopic() {
        return supportedTopic;
    }

    @Override
    public void process(String payload) throws Exception {
        HandoverManifestSyncEvent event = objectMapper.readValue(payload, HandoverManifestSyncEvent.class);
        postOfficeHandoverManifestService.applyInboundSync(event);
    }
}
