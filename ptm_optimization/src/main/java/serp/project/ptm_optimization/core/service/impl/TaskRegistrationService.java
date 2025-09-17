/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.ptm_optimization.core.domain.constant.ErrorMessage;
import serp.project.ptm_optimization.core.domain.dto.request.CreateTaskRegistrationDto;
import serp.project.ptm_optimization.core.domain.entity.TaskRegistrationEntity;
import serp.project.ptm_optimization.core.exception.AppException;
import serp.project.ptm_optimization.core.port.store.ITaskRegistrationPort;
import serp.project.ptm_optimization.core.service.ITaskRegistrationService;
import serp.project.ptm_optimization.infrastructure.store.mapper.TaskRegistrationMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskRegistrationService implements ITaskRegistrationService {
    private final ITaskRegistrationPort taskRegistrationPort;
    private final TaskRegistrationMapper taskRegistrationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskRegistrationEntity createTaskRegistration(Long userId, CreateTaskRegistrationDto req) {
        var existedTaskRegistration = taskRegistrationPort.getByUserId(userId);
        if (existedTaskRegistration != null) {
            log.error("Task registration already exists for userId: {}", userId);
            throw new AppException(ErrorMessage.TASK_REGISTRATION_ALREADY_EXISTS);
        }

        var taskRegistration = taskRegistrationMapper.toEntity(userId, req);
        return taskRegistrationPort.save(taskRegistration);
    }
}
