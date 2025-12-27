/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"

	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	"github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/core/service"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type ITaskUseCase interface {
	CreateTask(ctx context.Context, userID, tenantID int64, req *request.CreateTaskRequest) (*entity.TaskEntity, error)
	UpdateTask(ctx context.Context, userID, taskID int64, req *request.UpdateTaskRequest) (*entity.TaskEntity, error)
	DeleteTask(ctx context.Context, userID int64, taskID int64) error

	GetTaskByID(ctx context.Context, userID int64, taskID int64) (*entity.TaskEntity, error)
	GetTaskTreeByTaskID(ctx context.Context, userID, taskID int64) (*entity.TaskEntity, error)
	GetTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error)
	GetTasksByProjectID(ctx context.Context, userID int64, projectID int64) ([]*entity.TaskEntity, error)
	CountTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) (int64, error)
}

type taskUseCase struct {
	logger          *zap.Logger
	mapper          *mapper.TaskMapper
	taskService     service.ITaskService
	templateService service.ITaskTemplateService
	projectService  service.IProjectService
	txService       service.ITransactionService
}

func NewTaskUseCase(
	logger *zap.Logger,
	taskService service.ITaskService,
	templateService service.ITaskTemplateService,
	projectService service.IProjectService,
	noteService service.INoteService,
	txService service.ITransactionService,
) ITaskUseCase {
	return &taskUseCase{
		logger:          logger,
		mapper:          mapper.NewTaskMapper(),
		taskService:     taskService,
		templateService: templateService,
		projectService:  projectService,
		txService:       txService,
	}
}

func (u *taskUseCase) CreateTask(ctx context.Context, userID, tenantID int64, req *request.CreateTaskRequest) (*entity.TaskEntity, error) {
	var err error
	var parentTask *entity.TaskEntity
	if req.ParentTaskID != nil {
		parentTask, err = u.taskService.GetTaskByUserIDAndID(ctx, userID, *req.ParentTaskID)
		if err != nil {
			return nil, err
		}
		if parentTask.IsCompleted() {
			return nil, errors.New(constant.CreateSubtaskUnderCompletedTaskForbidden)
		}
	}
	var project *entity.ProjectEntity
	if req.ProjectID != nil {
		project, err = u.projectService.GetProjectByUserIDAndID(ctx, userID, *req.ProjectID)
		if err != nil {
			return nil, err
		}
	}

	task := u.mapper.CreateRequestToEntity(req, userID, tenantID)
	result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		task, err := u.taskService.CreateTask(ctx, tx, userID, task)
		if err != nil {
			return nil, err
		}

		if parentTask != nil {
			parentUpdateEntity, parentUpdateReq, err := u.handleParentTaskUpdateOnSubtaskChange(ctx, req.ParentTaskID)
			if err != nil {
				return nil, err
			}
			_, err = u.taskService.UpdateTask(ctx, tx, userID, parentTask, parentUpdateEntity)
			if err != nil {
				return nil, err
			}
			err = u.taskService.PushTaskUpdatedEvent(ctx, parentTask, parentUpdateReq)
			if err != nil {
				u.logger.Error("failed to push parent task updated event", zap.Error(err))
			}
		}

		err = u.taskService.PushTaskCreatedEvent(ctx, task)
		if err != nil {
			u.logger.Error("failed to push task created event", zap.Error(err))
			return nil, err
		}

		if project != nil {
			project.UpdateStatsForAddedTask(false)
			err = u.projectService.UpdateProjectProgress(ctx, tx, project.ID, project.TotalTasks, project.CompletedTasks)
			if err != nil {
				return nil, err
			}
		}

		return task, nil
	})
	if err != nil {
		return nil, err
	}

	return result.(*entity.TaskEntity), nil
}

func (u *taskUseCase) UpdateTask(ctx context.Context, userID, taskID int64, req *request.UpdateTaskRequest) (*entity.TaskEntity, error) {
	oldTask, err := u.taskService.GetTaskByID(ctx, taskID)
	if err != nil {
		return nil, err
	}
	if oldTask.UserID != userID {
		return nil, errors.New(constant.UpdateTaskForbidden)
	}

	oldProjectID := oldTask.ProjectID
	wasCompleted := oldTask.IsCompleted()
	oldParentID := oldTask.ParentTaskID

	if req.ProjectID != nil && (oldProjectID == nil || *oldProjectID != *req.ProjectID) {
		_, err = u.projectService.GetProjectByUserIDAndID(ctx, userID, *req.ProjectID)
		if err != nil {
			return nil, err
		}
	}
	if req.ParentTaskID != nil && (oldTask.ParentTaskID == nil || *oldTask.ParentTaskID != *req.ParentTaskID) {
		_, err = u.taskService.GetTaskByUserIDAndID(ctx, userID, *req.ParentTaskID)
		if err != nil {
			return nil, err
		}
	}

	newTask := *oldTask
	u.mapper.UpdateRequestToEntity(req, &newTask)
	isCompleted := newTask.IsCompleted()
	newParentID := newTask.ParentTaskID

	result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		updatedTask, err := u.taskService.UpdateTask(ctx, tx, userID, oldTask, &newTask)
		if err != nil {
			return nil, err
		}

		if err := u.handleProjectProgressUpdate(ctx, tx, oldProjectID, newTask.ProjectID, wasCompleted, isCompleted); err != nil {
			return nil, err
		}

		if wasCompleted != isCompleted {
			if oldParentID != nil {
				oldParent, updateOldParentReq, err := u.handleParentTaskUpdateOnSubtaskChange(ctx, oldParentID)
				if err != nil {
					return nil, err
				}
				_, err = u.taskService.UpdateTask(ctx, tx, userID, oldParent, oldParent)
				if err != nil {
					return nil, err
				}
				err = u.taskService.PushTaskUpdatedEvent(ctx, oldParent, updateOldParentReq)
			}
			if newParentID != nil && (oldParentID == nil || *newParentID != *oldParentID) {
				newParent, updateNewParentReq, err := u.handleParentTaskUpdateOnSubtaskChange(ctx, newParentID)
				if err != nil {
					return nil, err
				}
				_, err = u.taskService.UpdateTask(ctx, tx, userID, newParent, newParent)
				if err != nil {
					return nil, err
				}
				err = u.taskService.PushTaskUpdatedEvent(ctx, newParent, updateNewParentReq)
			}
		}

		err = u.taskService.PushTaskUpdatedEvent(ctx, updatedTask, req)
		if err != nil {
			u.logger.Error("failed to push task updated event", zap.Error(err))
			return nil, err
		}

		return updatedTask, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.TaskEntity), nil
}

func (u *taskUseCase) DeleteTask(ctx context.Context, userID int64, taskID int64) error {
	task, err := u.taskService.GetTaskByID(ctx, taskID)
	if err != nil {
		return err
	}
	if task.UserID != userID {
		return errors.New(constant.DeleteTaskForbidden)
	}

	projectID := task.ProjectID
	parentTaskID := task.ParentTaskID
	var parentTask *entity.TaskEntity
	var parentTaskUpdateReq *request.UpdateTaskRequest

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		deletedTasks, err := u.taskService.DeleteTaskRecursively(ctx, tx, userID, taskID)
		if err != nil {
			return err
		}
		var deletedTaskIDs []int64
		for _, t := range deletedTasks {
			deletedTaskIDs = append(deletedTaskIDs, t.ID)
		}

		if parentTaskID != nil {
			parentTask, err = u.taskService.GetTaskByID(ctx, *parentTaskID)
			if err == nil {
				subtasks, _ := u.taskService.GetTaskByParentID(ctx, *parentTaskID)
				parentTask.RecalculateSubTaskCounts(subtasks)
				if len(subtasks) == 0 {
					parentTask.HasSubtasks = false
				}
				_, err = u.taskService.UpdateTask(ctx, tx, userID, parentTask, parentTask)
				if err != nil {
					u.logger.Error("failed to update parent task after deletion", zap.Error(err))
					return err
				}
				parentTaskUpdateReq = &request.UpdateTaskRequest{
					HasSubtasks:       &parentTask.HasSubtasks,
					TotalSubtaskCount: &parentTask.TotalSubtaskCount,
				}
			}
		}
		if projectID != nil {
			if err := u.refreshProjectStats(ctx, tx, *projectID); err != nil {
				return err
			}
		}

		err = u.taskService.PushBulkTaskDeletedEvent(ctx, deletedTaskIDs, userID)
		if err != nil {
			u.logger.Error("failed to push task deleted event", zap.Error(err))
			return err
		}
		if parentTask != nil {
			err = u.taskService.PushTaskUpdatedEvent(ctx, parentTask, parentTaskUpdateReq)
			if err != nil {
				u.logger.Error("failed to push parent task updated event after deletion", zap.Error(err))
			}
		}

		return nil
	})
}

func (u *taskUseCase) GetTaskByID(ctx context.Context, userID int64, taskID int64) (*entity.TaskEntity, error) {
	task, err := u.taskService.GetTaskByUserIDAndID(ctx, userID, taskID)
	if err != nil {
		return nil, err
	}
	return task, nil
}

func (u *taskUseCase) GetTaskTreeByTaskID(ctx context.Context, userID, taskID int64) (*entity.TaskEntity, error) {
	task, err := u.taskService.GetTaskByID(ctx, taskID)
	if err != nil {
		return nil, err
	}
	if task.UserID != userID {
		return nil, errors.New(constant.GetTaskForbidden)
	}
	return u.taskService.GetTaskTreeByTaskID(ctx, taskID)
}

func (u *taskUseCase) GetTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error) {
	return u.taskService.GetTasksByUserID(ctx, userID, filter)
}

func (u *taskUseCase) GetTasksByProjectID(ctx context.Context, userID int64, projectID int64) ([]*entity.TaskEntity, error) {
	project, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if project.UserID != userID {
		return nil, errors.New(constant.GetTaskForbidden)
	}

	filter := store.NewTaskFilter()
	filter.ProjectID = &projectID
	return u.taskService.GetTasksByUserID(ctx, userID, filter)
}

func (u *taskUseCase) CountTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) (int64, error) {
	return u.taskService.CountTasksByUserID(ctx, userID, filter)
}

// handleProjectProgressUpdate handles incremental project progress updates
// when a task's status or project assignment changes
func (u *taskUseCase) handleProjectProgressUpdate(
	ctx context.Context,
	tx *gorm.DB,
	oldProjectID, newProjectID *int64,
	wasCompleted, isCompleted bool,
) error {
	sameProject := (oldProjectID == nil && newProjectID == nil) ||
		(oldProjectID != nil && newProjectID != nil && *oldProjectID == *newProjectID)

	if sameProject {
		if oldProjectID != nil && wasCompleted != isCompleted {
			return u.updateProjectCompletedCount(ctx, tx, *oldProjectID, isCompleted)
		}
		return nil
	}

	if oldProjectID != nil {
		if err := u.decrementProjectStats(ctx, tx, *oldProjectID, wasCompleted); err != nil {
			return err
		}
	}

	if newProjectID != nil {
		if err := u.incrementProjectStats(ctx, tx, *newProjectID, isCompleted); err != nil {
			return err
		}
	}

	return nil
}

func (u *taskUseCase) updateProjectCompletedCount(ctx context.Context, tx *gorm.DB, projectID int64, increment bool) error {
	project, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return err
	}

	if increment {
		project.CompletedTasks++
	} else {
		if project.CompletedTasks > 0 {
			project.CompletedTasks--
		}
	}

	return u.projectService.UpdateProjectProgress(ctx, tx, projectID, project.TotalTasks, project.CompletedTasks)
}

func (u *taskUseCase) incrementProjectStats(ctx context.Context, tx *gorm.DB, projectID int64, isCompleted bool) error {
	project, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return err
	}

	project.UpdateStatsForAddedTask(isCompleted)
	return u.projectService.UpdateProjectProgress(ctx, tx, projectID, project.TotalTasks, project.CompletedTasks)
}

func (u *taskUseCase) decrementProjectStats(ctx context.Context, tx *gorm.DB, projectID int64, wasCompleted bool) error {
	project, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return err
	}

	project.UpdateStatsForRemovedTask(wasCompleted)
	return u.projectService.UpdateProjectProgress(ctx, tx, projectID, project.TotalTasks, project.CompletedTasks)
}

func (u *taskUseCase) refreshProjectStats(ctx context.Context, tx *gorm.DB, projectID int64) error {
	_, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return err
	}

	tasks, err := u.taskService.GetTaskByProjectID(ctx, projectID)
	if err != nil {
		return err
	}
	totalTasks := len(tasks)
	completedTasks := 0
	for _, task := range tasks {
		if task.IsCompleted() {
			completedTasks++
		}
	}
	return u.projectService.UpdateProjectProgress(ctx, tx, projectID, totalTasks, completedTasks)
}

func (u *taskUseCase) handleParentTaskUpdateOnSubtaskChange(
	ctx context.Context,
	parentTaskID *int64,
) (*entity.TaskEntity, *request.UpdateTaskRequest, error) {
	if parentTaskID == nil {
		return nil, nil, nil
	}
	parentTask, err := u.taskService.GetTaskByID(ctx, *parentTaskID)
	if err != nil {
		u.logger.Error("failed to get parent task for subtask change", zap.Error(err))
		return nil, nil, err
	}
	oldTotalSubtaskCount := parentTask.TotalSubtaskCount
	oldCompletedSubtaskCount := parentTask.CompletedSubtaskCount
	oldHasSubtasks := parentTask.HasSubtasks

	newTotalSubtaskCount := 0
	newCompletedSubtaskCount := 0
	newHasSubtasks := false

	subtasks, _ := u.taskService.GetTaskByParentID(ctx, *parentTaskID)
	if len(subtasks) > 0 {
		newHasSubtasks = true
		newTotalSubtaskCount = len(subtasks)
		for _, st := range subtasks {
			if st.IsCompleted() {
				newCompletedSubtaskCount++
			}
		}
	}
	parentTask.HasSubtasks = newHasSubtasks
	parentTask.TotalSubtaskCount = newTotalSubtaskCount
	parentTask.CompletedSubtaskCount = newCompletedSubtaskCount

	if oldTotalSubtaskCount != newTotalSubtaskCount ||
		oldCompletedSubtaskCount != newCompletedSubtaskCount ||
		oldHasSubtasks != newHasSubtasks {
		updateReq := &request.UpdateTaskRequest{
			HasSubtasks:           &parentTask.HasSubtasks,
			TotalSubtaskCount:     &parentTask.TotalSubtaskCount,
			CompletedSubtaskCount: &parentTask.CompletedSubtaskCount,
		}
		return parentTask, updateReq, nil
	}
	return nil, nil, nil
}

// func (u *taskUseCase) CreateTaskFromTemplate(ctx context.Context, userID int64, templateID int64, variables map[string]string) (*entity.TaskEntity, error) {
// 	template, err := u.templateService.GetTemplateByID(ctx, templateID)
// 	if err != nil {
// 		return nil, err
// 	}
// 	if template.UserID != userID {
// 		return nil, errors.New(constant.TemplateDoesNotBelongToUser)
// 	}

// 	result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
// 		task := &entity.TaskEntity{
// 			UserID:               userID,
// 			TenantID:             template.TenantID,
// 			Title:                template.SubstituteVariables(variables),
// 			Description:          template.Description,
// 			Priority:             template.Priority,
// 			EstimatedDurationMin: &template.EstimatedDurationMin,
// 			Category:             template.Category,
// 			Tags:                 template.Tags,
// 			IsDeepWork:           template.IsDeepWork,
// 			RecurrencePattern:    template.RecurrencePattern,
// 			RecurrenceConfig:     template.RecurrenceConfig,
// 		}

// 		task, err := u.taskService.CreateTask(ctx, tx, userID, task)
// 		if err != nil {
// 			return nil, err
// 		}

// 		err = u.taskService.PushTaskCreatedEvent(ctx, task)
// 		if err != nil {
// 			u.logger.Error("failed to push task created event", zap.Error(err))
// 			return nil, err
// 		}

// 		_ = u.templateService.IncrementUsageCount(ctx, tx, templateID)

// 		return task, nil
// 	})
// 	if err != nil {
// 		return nil, err
// 	}
// 	return result.(*entity.TaskEntity), nil
// }

// func (u *taskUseCase) BulkDeleteTasks(ctx context.Context, userID int64, taskIDs []int64) error {
// 	// Collect tasks info before deletion
// 	type taskInfo struct {
// 		projectID    *int64
// 		wasCompleted bool
// 	}
// 	tasksInfo := make(map[int64]taskInfo)

// 	for _, taskID := range taskIDs {
// 		task, err := u.taskService.GetTaskByID(ctx, taskID)
// 		if err != nil {
// 			return err
// 		}
// 		if task.UserID != userID {
// 			return errors.New(constant.DeleteTaskForbidden)
// 		}
// 		tasksInfo[taskID] = taskInfo{
// 			projectID:    task.ProjectID,
// 			wasCompleted: enum.TaskStatus(task.Status).IsCompleted(),
// 		}
// 	}

// 	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
// 		// Aggregate changes per project
// 		projectDeltas := make(map[int64]struct {
// 			totalDelta     int
// 			completedDelta int
// 		})

// 		for _, taskID := range taskIDs {
// 			if err := u.taskService.DeleteTask(ctx, tx, userID, taskID); err != nil {
// 				return err
// 			}

// 			info := tasksInfo[taskID]
// 			if info.projectID != nil {
// 				delta := projectDeltas[*info.projectID]
// 				delta.totalDelta--
// 				if info.wasCompleted {
// 					delta.completedDelta--
// 				}
// 				projectDeltas[*info.projectID] = delta
// 			}

// 			err := u.taskService.PushTaskDeletedEvent(ctx, taskID, userID)
// 			if err != nil {
// 				u.logger.Error("failed to push task deleted event", zap.Error(err))
// 				return err
// 			}
// 		}

// 		// Apply aggregated deltas to each project
// 		for projectID, delta := range projectDeltas {
// 			project, err := u.projectService.GetProjectByID(ctx, projectID)
// 			if err != nil {
// 				return err
// 			}
// 			newTotal := project.TotalTasks + delta.totalDelta
// 			newCompleted := project.CompletedTasks + delta.completedDelta
// 			if newTotal < 0 {
// 				newTotal = 0
// 			}
// 			if newCompleted < 0 {
// 				newCompleted = 0
// 			}
// 			if err := u.projectService.UpdateProjectProgress(ctx, tx, projectID, newTotal, newCompleted); err != nil {
// 				return err
// 			}
// 		}

// 		return nil
// 	})
// }
