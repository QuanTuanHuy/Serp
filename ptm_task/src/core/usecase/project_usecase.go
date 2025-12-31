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
	"gorm.io/gorm"
)

type IProjectUseCase interface {
	CreateProject(ctx context.Context, userID int64, project *entity.ProjectEntity) (*entity.ProjectEntity, error)

	UpdateProject(ctx context.Context, userID, projectID int64, req *request.UpdateProjectRequest) error

	DeleteProject(ctx context.Context, userID int64, projectID int64) error
	BulkDeleteProjects(ctx context.Context, userID int64, projectIDs []int64) error

	GetProjectByID(ctx context.Context, userID int64, projectID int64) (*entity.ProjectEntity, error)
	GetProjectWithStats(ctx context.Context, userID int64, projectID int64) (*entity.ProjectEntity, error)
	GetProjectsByUserID(ctx context.Context, userID int64, filter *store.ProjectFilter) ([]*entity.ProjectEntity, error)
	CountProjectsByUserID(ctx context.Context, userID int64, filter *store.ProjectFilter) (int64, error)
	GetProjectsWithStats(ctx context.Context, userID int64, filter *store.ProjectFilter) ([]*entity.ProjectEntity, error)
	GetFavoriteProjects(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error)
	GetOverdueProjects(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error)
	GetProjectTasks(ctx context.Context, userID int64, projectID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error)
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
	oldProject, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return err
	}
	if oldProject.UserID != userID {
		return errors.New(constant.UpdateProjectForbidden)
	}

	newProject := *oldProject
	u.mapper.UpdateRequestToEntity(req, &newProject)

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.projectService.UpdateProject(ctx, tx, userID, oldProject, &newProject)
	})
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
