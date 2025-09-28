/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum ContactType {
    INDIVIDUAL("Individual"),
    COMPANY("Company");

    private final String typeName;

    ContactType(String typeName) {
        this.typeName = typeName;
    }
}
