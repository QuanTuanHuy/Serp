/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/service"
	"gorm.io/gorm"
)

type IProjectUseCase interface {
	CreateProject(ctx context.Context, userID int64, request *request.CreateProjectDTO) (*entity.ProjectEntity, error)
	UpdateProject(ctx context.Context, userID, projectID int64, request *request.UpdateProjectDTO) (*entity.ProjectEntity, error)
	GetProjectByID(ctx context.Context, ID int64) (*entity.ProjectEntity, error)
	GetProjects(ctx context.Context, params *request.GetProjectParams) ([]*entity.ProjectEntity, int64, error)
	GetProjectsByName(ctx context.Context, userID int64, searchName string, maxDistance int, limit int) ([]*entity.ProjectEntity, error)
	GetProjectsByUserID(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error)
	ArchiveProject(ctx context.Context, userID, projectID int64) error
}

type ProjectUseCase struct {
	projectService   service.IProjectService
	groupTaskService service.IGroupTaskService
	txService        service.ITransactionService
}

func (p *ProjectUseCase) ArchiveProject(ctx context.Context, userID int64, projectID int64) error {
	return p.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return p.projectService.ArchiveProject(ctx, tx, userID, projectID)
	})
}

func (p *ProjectUseCase) GetProjectsByUserID(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error) {
	return p.projectService.GetProjectsByUserID(ctx, userID)
}

func (p *ProjectUseCase) GetProjects(ctx context.Context, params *request.GetProjectParams) ([]*entity.ProjectEntity, int64, error) {
	return p.projectService.GetProjects(ctx, params)
}

func (p *ProjectUseCase) GetProjectByID(ctx context.Context, ID int64) (*entity.ProjectEntity, error) {
	project, err := p.projectService.GetProjectByID(ctx, ID)
	if err != nil {
		return nil, err
	}
	groupTasks, err := p.groupTaskService.GetGroupTasksByProjectID(ctx, ID)
	if err != nil {
		return nil, err
	}
	project.GroupTasks = groupTasks
	return project, nil
}

func (p *ProjectUseCase) CreateProject(ctx context.Context, userID int64, request *request.CreateProjectDTO) (*entity.ProjectEntity, error) {
	result, err := p.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		return p.projectService.CreateProject(ctx, tx, userID, request)
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.ProjectEntity), nil
}

func (p *ProjectUseCase) UpdateProject(ctx context.Context, userID int64, projectID int64, request *request.UpdateProjectDTO) (*entity.ProjectEntity, error) {
	result, err := p.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		return p.projectService.UpdateProject(ctx, tx, userID, projectID, request)
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.ProjectEntity), nil
}

func (p *ProjectUseCase) GetProjectsByName(ctx context.Context, userID int64, searchName string, maxDistance int, limit int) ([]*entity.ProjectEntity, error) {
	return p.projectService.GetProjectsByName(ctx, userID, searchName, maxDistance, limit)
}

func NewProjectUseCase(projectService service.IProjectService,
	groupTaskService service.IGroupTaskService,
	txService service.ITransactionService) IProjectUseCase {
	return &ProjectUseCase{
		projectService:   projectService,
		groupTaskService: groupTaskService,
		txService:        txService,
	}
}
