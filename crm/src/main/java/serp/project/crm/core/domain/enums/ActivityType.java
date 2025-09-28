/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum ActivityType {
    CALL("Call"),
    MEETING("Meeting"),
    EMAIL("Email"),
    TASK("Task");

    private final String typeName;

    ActivityType(String typeName) {
        this.typeName = typeName;
    }
}
