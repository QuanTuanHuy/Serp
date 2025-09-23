/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum OpportunityStage {
    PROSPECTING("Prospecting"),
    QUALIFICATION("Qualification"),
    PROPOSAL("Proposal Sent"),
    NEGOTIATION("In Negotiation"),
    CLOSED_WON("Closed Won"),
    CLOSED_LOST("Closed Lost");

    private final String description;

    OpportunityStage(String description) {
        this.description = description;
    }

    public boolean isClosed() {
        return this == CLOSED_WON || this == CLOSED_LOST;
    }

    public boolean isActive() {
        return !isClosed();
    }
}