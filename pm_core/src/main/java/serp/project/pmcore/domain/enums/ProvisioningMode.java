/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.enums;

public enum ProvisioningMode {
    TEMPLATE_DEFAULT,
    SHARED_FROM_EXISTING,
    CLONE_FROM_SHARED;

    public static ProvisioningMode defaultForCreate(ProvisioningMode mode) {
        return mode == null ? TEMPLATE_DEFAULT : mode;
    }
}
