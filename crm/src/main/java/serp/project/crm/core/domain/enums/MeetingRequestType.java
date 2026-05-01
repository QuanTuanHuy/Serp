/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum MeetingRequestType {
    DISCOVERY(30, 8),
    DEMO(60, 12),
    PROPOSAL(45, 16),
    NEGOTIATION(90, 20),
    QBR(120, 18);

    private final int defaultDurationMinutes;
    private final int priorityPoints;

    MeetingRequestType(int defaultDurationMinutes, int priorityPoints) {
        this.defaultDurationMinutes = defaultDurationMinutes;
        this.priorityPoints = priorityPoints;
    }
}
