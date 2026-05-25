/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.kafka.event.HubPostOfficeSyncEvent;

public interface HubPostOfficeInboundSyncService {

    void applyFirstMileKafkaEvent(HubPostOfficeSyncEvent event);
}
