/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.ptm_optimization.core.domain.dto.GeneralResponse;
import serp.project.ptm_optimization.core.domain.dto.message.CreateTaskMessage;
import serp.project.ptm_optimization.core.domain.mapper.TaskDtoMapper;
import serp.project.ptm_optimization.core.service.IParentTaskService;
import serp.project.ptm_optimization.core.service.ITaskService;
import serp.project.ptm_optimization.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskUseCase {
    private final IParentTaskService parentTaskService;
    private final ITaskService taskService;

    private final TaskDtoMapper taskDtoMapper;

    private final ResponseUtils responseUtils;

    @Transactional
    public GeneralResponse<?> createTaskFromMessage(CreateTaskMessage message) {
        try {
            var parentTask = parentTaskService.getParentTaskByGroupTaskId(message.getGroupTaskId());
            if (parentTask == null) {
                var newParentTask = taskDtoMapper.toParentTaskEntity(message);
                parentTask = parentTaskService.save(newParentTask);
            }

            var task = taskDtoMapper.toEntity(message, parentTask);
            taskService.save(task);

            return responseUtils.success("Task created successfully");
        } catch (Exception e) {
            log.error("Error in createTaskFromMessage: ", e);
            throw e;
        }
    }
}
