/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.message.SyncUserFirstMileEvent;

public interface PostOfficeStaffSyncService {
    void syncUser(SyncUserFirstMileEvent event);
}
