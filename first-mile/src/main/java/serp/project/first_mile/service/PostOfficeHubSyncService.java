/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.message.HubPostOfficeSyncEvent;

public interface PostOfficeHubSyncService {

    void applyInboundHubPostOfficeEvent(HubPostOfficeSyncEvent event);
}
