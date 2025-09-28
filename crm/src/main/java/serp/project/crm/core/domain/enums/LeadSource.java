/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum LeadSource {
    WEBSITE("Website"),
    SOCIAL_MEDIA("Social Media"),
    REFERRAL("Referral"),
    COLD_CALL("Cold Call"),
    EMAIL_CAMPAIGN("Email Campaign");

    private final String sourceName;

    LeadSource(String sourceName) {
        this.sourceName = sourceName;
    }
}
