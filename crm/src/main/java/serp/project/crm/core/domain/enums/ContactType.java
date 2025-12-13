/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum ContactType {
    PRIMARY("Primary"),
    SECONDARY("Secondary"),
    BILLING("Billing"),
    TECHNICAL("Technical"),
    ;
    
    private final String typeName;

    ContactType(String typeName) {
        this.typeName = typeName;
    }
}
