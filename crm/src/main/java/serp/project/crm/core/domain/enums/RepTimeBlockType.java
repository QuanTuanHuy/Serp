/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum RepTimeBlockType {
    MEETING("Meeting");

    private final String type;

    RepTimeBlockType(String type) {
        this.type = type;
    }
}
