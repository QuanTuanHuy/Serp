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
    QUALIFIED("Qualified Lead"),
    DISQUALIFIED("Disqualified Lead"),
    CONVERTED("Converted to Customer");

    private final String description;

    LeadStatus(String description) {
        this.description = description;
    }
}
