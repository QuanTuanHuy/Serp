/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.usecase;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.ptm_optimization.core.domain.dto.GeneralResponse;
import serp.project.ptm_optimization.core.domain.dto.request.CreateTaskRegistrationDto;
import serp.project.ptm_optimization.core.service.ITaskRegistrationService;
import serp.project.ptm_optimization.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskRegistrationUseCase {
    private final ITaskRegistrationService taskRegistrationService;

    private final ResponseUtils responseUtils;

    public GeneralResponse<?> createTaskRegistration(Long userId, CreateTaskRegistrationDto req) {
        try {
            var result = taskRegistrationService.createTaskRegistration(userId, req);
            return responseUtils.success(result);
        } catch (Exception e) {
            log.error("Error creating task registration for userId: {}, error: {}", userId, e.getMessage());
            throw e;
        }
    }
}
