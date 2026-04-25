/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum AccountType {
    PROSPECT("Prospect"),
    CUSTOMER("Customer");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }
}
