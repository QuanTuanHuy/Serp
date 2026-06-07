/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import serp.project.crm.core.domain.entity.ActivityEntity;

public interface IRepTimeBlockService {

    void syncFromActivity(ActivityEntity activity, Long tenantId);

    void removeByActivityId(Long activityId, Long tenantId);
}
