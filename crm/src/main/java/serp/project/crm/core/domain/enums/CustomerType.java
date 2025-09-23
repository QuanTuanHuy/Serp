/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum CustomerType {
    INDIVIDUAL("Individual Customer"),
    COMPANY("Corporate Customer"),
    GOVERNMENT("Government Entity"),
    NON_PROFIT("Non-Profit Organization");

    private final String description;

    CustomerType(String description) {
        this.description = description;
    }
}