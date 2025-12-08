/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import serp.project.crm.core.domain.entity.LeadEntity;

public interface ILeadScoringService {
    Integer calculateSmartScore(LeadEntity lead);
}
