/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum LeadStatus {
    NEW("New Lead"),
    CONTACTED("Initial Contact Made"),
    NURTURING("In Nurturing"),
    QUALIFIED("Qualified Lead"),
    DISQUALIFIED("Disqualified Lead"),
    CONVERTED("Converted to Opportunity");

    private final String description;

    LeadStatus(String description) {
        this.description = description;
    }
}
