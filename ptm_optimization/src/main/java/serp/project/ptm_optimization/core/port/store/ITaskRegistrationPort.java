/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.port.store;

import serp.project.ptm_optimization.core.domain.entity.TaskRegistrationEntity;

public interface ITaskRegistrationPort {
    TaskRegistrationEntity save(TaskRegistrationEntity taskRegistration);

    TaskRegistrationEntity getByUserId(Long userId);
}
