/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.service;

import serp.project.ptm_optimization.core.domain.dto.request.CreateTaskRegistrationDto;
import serp.project.ptm_optimization.core.domain.entity.TaskRegistrationEntity;

public interface ITaskRegistrationService {
    TaskRegistrationEntity createTaskRegistration(Long userId, CreateTaskRegistrationDto req);
}
