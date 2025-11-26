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
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	"github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/core/service"
	"gorm.io/gorm"
)

type IProjectUseCase interface {
	CreateProject(ctx context.Context, userID int64, project *entity.ProjectEntity) (*entity.ProjectEntity, error)

	UpdateProject(ctx context.Context, userID, projectID int64, req *request.UpdateProjectRequest) error
	UpdateProjectStatus(ctx context.Context, userID int64, projectID int64, status string) error
	ToggleFavorite(ctx context.Context, userID int64, projectID int64) error
	CompleteProject(ctx context.Context, userID int64, projectID int64) error
	ArchiveProject(ctx context.Context, userID int64, projectID int64) error

	DeleteProject(ctx context.Context, userID int64, projectID int64) error
	BulkDeleteProjects(ctx context.Context, userID int64, projectIDs []int64) error

	GetProjectByID(ctx context.Context, userID int64, projectID int64) (*entity.ProjectEntity, error)
	GetProjectWithStats(ctx context.Context, userID int64, projectID int64) (*entity.ProjectEntity, error)
	GetProjectsByUserID(ctx context.Context, userID int64, filter *store.ProjectFilter) ([]*entity.ProjectEntity, error)
	CountProjectsByUserID(ctx context.Context, userID int64, filter *store.ProjectFilter) (int64, error)
	GetProjectsWithStats(ctx context.Context, userID int64, filter *store.ProjectFilter) ([]*entity.ProjectEntity, error)
	GetFavoriteProjects(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error)
	GetOverdueProjects(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error)

	AddTaskToProject(ctx context.Context, userID int64, projectID int64, taskID int64) error
	RemoveTaskFromProject(ctx context.Context, userID int64, taskID int64) error
	MoveTaskToProject(ctx context.Context, userID int64, taskID int64, newProjectID int64) error
	GetProjectTasks(ctx context.Context, userID int64, projectID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error)

	RefreshProjectProgress(ctx context.Context, projectID int64) error
}

type projectUseCase struct {
	projectService service.IProjectService
	taskService    service.ITaskService
	txService      service.ITransactionService
	mapper         *mapper.ProjectMapper
}

func NewProjectUseCase(
	projectService service.IProjectService,
	taskService service.ITaskService,
	txService service.ITransactionService,
) IProjectUseCase {
	return &projectUseCase{
		projectService: projectService,
		taskService:    taskService,
		txService:      txService,
		mapper:         mapper.NewProjectMapper(),
	}
}

func (u *projectUseCase) CreateProject(ctx context.Context, userID int64, project *entity.ProjectEntity) (*entity.ProjectEntity, error) {
	result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		project, err := u.projectService.CreateProject(ctx, tx, userID, project)
		if err != nil {
			return nil, err
		}
		return project, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.ProjectEntity), nil
}

func (u *projectUseCase) UpdateProject(ctx context.Context, userID, projectID int64, req *request.UpdateProjectRequest) error {
	project, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return err
	}
	project = u.mapper.UpdateRequestToEntity(req, project)
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.projectService.UpdateProject(ctx, tx, userID, project)
	})
}

func (u *projectUseCase) UpdateProjectStatus(ctx context.Context, userID int64, projectID int64, status string) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.projectService.UpdateProjectStatus(ctx, tx, userID, projectID, status)
	})
}

func (u *projectUseCase) ToggleFavorite(ctx context.Context, userID int64, projectID int64) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		project, err := u.projectService.GetProjectByID(ctx, projectID)
		if err != nil {
			return err
		}
		if project.UserID != userID {
			return errors.New(constant.UpdateProjectForbidden)
		}
		project.IsFavorite = !project.IsFavorite
		return u.projectService.UpdateProject(ctx, tx, userID, project)
	})
}

func (u *projectUseCase) CompleteProject(ctx context.Context, userID int64, projectID int64) error {
	return u.UpdateProjectStatus(ctx, userID, projectID, "COMPLETED")
}

func (u *projectUseCase) ArchiveProject(ctx context.Context, userID int64, projectID int64) error {
	return u.UpdateProjectStatus(ctx, userID, projectID, "ARCHIVED")
}

func (u *projectUseCase) DeleteProject(ctx context.Context, userID int64, projectID int64) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.projectService.DeleteProject(ctx, tx, userID, projectID)
	})
}

func (u *projectUseCase) BulkDeleteProjects(ctx context.Context, userID int64, projectIDs []int64) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		for _, projectID := range projectIDs {
			if err := u.projectService.DeleteProject(ctx, tx, userID, projectID); err != nil {
				return err
			}
		}
		return nil
	})
}

func (u *projectUseCase) GetProjectByID(ctx context.Context, userID int64, projectID int64) (*entity.ProjectEntity, error) {
	project, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if project.UserID != userID {
		return nil, errors.New(constant.GetProjectForbidden)
	}
	return project, nil
}

func (u *projectUseCase) GetProjectWithStats(ctx context.Context, userID int64, projectID int64) (*entity.ProjectEntity, error) {
	project, err := u.projectService.GetProjectWithStats(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if project.UserID != userID {
		return nil, errors.New(constant.GetProjectForbidden)
	}
	return project, nil
}

func (u *projectUseCase) GetProjectsByUserID(ctx context.Context, userID int64, filter *store.ProjectFilter) ([]*entity.ProjectEntity, error) {
	return u.projectService.GetProjectsByUserID(ctx, userID, filter)
}

func (u *projectUseCase) CountProjectsByUserID(ctx context.Context, userID int64, filter *store.ProjectFilter) (int64, error) {
	return u.projectService.CountProjectsByUserID(ctx, userID, filter)
}

func (u *projectUseCase) GetProjectsWithStats(ctx context.Context, userID int64, filter *store.ProjectFilter) ([]*entity.ProjectEntity, error) {
	return u.projectService.GetProjectsWithStats(ctx, userID, filter)
}

func (u *projectUseCase) GetFavoriteProjects(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error) {
	return u.projectService.GetFavoriteProjects(ctx, userID)
}

func (u *projectUseCase) GetOverdueProjects(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error) {
	return u.projectService.GetOverdueProjects(ctx, userID)
}

func (u *projectUseCase) AddTaskToProject(ctx context.Context, userID int64, projectID int64, taskID int64) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		project, err := u.projectService.GetProjectByID(ctx, projectID)
		if err != nil {
			return err
		}
		if project.UserID != userID {
			return errors.New(constant.UpdateProjectForbidden)
		}

		task, err := u.taskService.GetTaskByID(ctx, taskID)
		if err != nil {
			return err
		}
		if task.UserID != userID {
			return errors.New(constant.UpdateTaskForbidden)
		}

		task.ProjectID = &projectID
		if _, err := u.taskService.UpdateTask(ctx, tx, userID, task); err != nil {
			return err
		}

		return u.RefreshProjectProgress(ctx, projectID)
	})
}

func (u *projectUseCase) RemoveTaskFromProject(ctx context.Context, userID int64, taskID int64) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		task, err := u.taskService.GetTaskByID(ctx, taskID)
		if err != nil {
			return err
		}
		if task.UserID != userID {
			return errors.New(constant.UpdateTaskForbidden)
		}

		oldProjectID := task.ProjectID
		task.ProjectID = nil
		if _, err := u.taskService.UpdateTask(ctx, tx, userID, task); err != nil {
			return err
		}

		if oldProjectID != nil {
			return u.RefreshProjectProgress(ctx, *oldProjectID)
		}
		return nil
	})
}

func (u *projectUseCase) MoveTaskToProject(ctx context.Context, userID int64, taskID int64, newProjectID int64) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		newProject, err := u.projectService.GetProjectByID(ctx, newProjectID)
		if err != nil {
			return err
		}
		if newProject.UserID != userID {
			return errors.New(constant.UpdateProjectForbidden)
		}

		task, err := u.taskService.GetTaskByID(ctx, taskID)
		if err != nil {
			return err
		}
		if task.UserID != userID {
			return errors.New(constant.UpdateTaskForbidden)
		}

		oldProjectID := task.ProjectID
		task.ProjectID = &newProjectID
		if _, err := u.taskService.UpdateTask(ctx, tx, userID, task); err != nil {
			return err
		}

		if oldProjectID != nil {
			if err := u.RefreshProjectProgress(ctx, *oldProjectID); err != nil {
				return err
			}
		}
		return u.RefreshProjectProgress(ctx, newProjectID)
	})
}

func (u *projectUseCase) GetProjectTasks(ctx context.Context, userID int64, projectID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error) {
	project, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if project.UserID != userID {
		return nil, errors.New(constant.GetProjectForbidden)
	}

	filter.ProjectID = &projectID
	return u.taskService.GetTasksByUserID(ctx, userID, filter)
}

func (u *projectUseCase) RefreshProjectProgress(ctx context.Context, projectID int64) error {
	_, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return err
	}
	tasks, err := u.taskService.GetTaskByProjectID(ctx, projectID)
	if err != nil {
		return err
	}
	if len(tasks) == 0 {
		return nil
	}

	var totalTasks, completedTasks int
	for _, task := range tasks {
		totalTasks++
		if enum.TaskStatus(task.Status).IsCompleted() {
			completedTasks++
		}
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.projectService.UpdateProjectProgress(ctx, tx, projectID, totalTasks, completedTasks)
	})
}
