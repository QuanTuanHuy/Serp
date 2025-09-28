/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.OpportunityStage;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OpportunityEntity extends BaseEntity {
    private String name;
    private String description;

    private Long leadId;
    private Long customerId;

    private OpportunityStage stage;
    private BigDecimal estimatedValue;
    private Integer probability;
    private LocalDate expectedCloseDate;
    private LocalDate actualCloseDate;
    private Long assignedTo;
    private String notes;
    private String lossReason;

    private LeadEntity lead;
    private CustomerEntity customer;

    // Stage management
    public boolean isWon() {
        return OpportunityStage.CLOSED_WON.equals(this.stage);
    }

    public boolean isLost() {
        return OpportunityStage.CLOSED_LOST.equals(this.stage);
    }

    public boolean isClosed() {
        return isWon() || isLost();
    }

    public boolean canAdvanceStage(OpportunityStage newStage) {
        if (isClosed()) {
            return false;
        }

        switch (stage) {
            case PROSPECTING:
                return OpportunityStage.QUALIFICATION.equals(newStage) ||
                        OpportunityStage.CLOSED_LOST.equals(newStage);
            case QUALIFICATION:
                return OpportunityStage.PROPOSAL.equals(newStage) ||
                        OpportunityStage.PROSPECTING.equals(newStage) ||
                        OpportunityStage.CLOSED_LOST.equals(newStage);
            case PROPOSAL:
                return OpportunityStage.NEGOTIATION.equals(newStage) ||
                        OpportunityStage.QUALIFICATION.equals(newStage) ||
                        OpportunityStage.CLOSED_LOST.equals(newStage);
            case NEGOTIATION:
                return OpportunityStage.CLOSED_WON.equals(newStage) ||
                        OpportunityStage.PROPOSAL.equals(newStage) ||
                        OpportunityStage.CLOSED_LOST.equals(newStage);
            default:
                return false;
        }
    }

    public void advanceToStage(OpportunityStage newStage, Long updatedBy) {
        if (!canAdvanceStage(newStage)) {
            throw new IllegalStateException("Cannot advance to the specified stage from the current stage.");
        }
        this.stage = newStage;
        this.setUpdatedBy(updatedBy);
        updateProbabilityForStage(newStage);
        if (isClosed()) {
            this.actualCloseDate = LocalDate.now();
        }
    }

    private void updateProbabilityForStage(OpportunityStage stage) {
        switch (stage) {
            case PROSPECTING:
                this.probability = 10;
                break;
            case QUALIFICATION:
                this.probability = 25;
                break;
            case PROPOSAL:
                this.probability = 50;
                break;
            case NEGOTIATION:
                this.probability = 75;
                break;
            case CLOSED_WON:
                this.probability = 100;
                break;
            case CLOSED_LOST:
                this.probability = 0;
                break;
        }
    }
}
