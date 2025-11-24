/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/service"
	"gorm.io/gorm"
)

type IGroupTaskUseCase interface {
	CreateGroupTask(ctx context.Context, userID, projectID int64, request *request.CreateGroupTaskDTO) (*entity.GroupTaskEntity, error)
	GetGroupTasksByProjectID(ctx context.Context, userID, projectID int64) ([]*entity.GroupTaskEntity, error)
	GetGroupTaskByID(ctx context.Context, userID, groupTaskID int64) (*entity.GroupTaskEntity, error)
}

type GroupTaskUseCase struct {
	groupTaskService service.IGroupTaskService
	taskService      service.ITaskService
	projectService   service.IProjectService
	txService        service.ITransactionService
}

func (g *GroupTaskUseCase) GetGroupTaskByID(ctx context.Context, userID, groupTaskID int64) (*entity.GroupTaskEntity, error) {
	groupTask, err := g.groupTaskService.GetGroupTaskByID(ctx, groupTaskID)
	if err != nil {
		return nil, err
	}
	project, err := g.projectService.GetProjectByID(ctx, groupTask.ProjectID)
	if err != nil {
		return nil, err
	}
	if project.OwnerID != userID {
		return nil, errors.New(constant.GetGroupTaskForbidden)
	}
	tasks, err := g.taskService.GetTaskHierarchyByGroupTaskID(ctx, groupTaskID)
	if err != nil {
		log.Error(ctx, "Failed to get tasks for group task ID ", groupTaskID, " error: ", err)
		return nil, err
	}
	groupTask.Tasks = tasks

	return groupTask, nil
}

func (g *GroupTaskUseCase) GetGroupTasksByProjectID(ctx context.Context, userID, projectID int64) ([]*entity.GroupTaskEntity, error) {
	project, err := g.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if project.OwnerID != userID {
		log.Error(ctx, "User does not have permission to access project ID: ", projectID)
		return nil, errors.New(constant.GetGroupTaskForbidden)
	}
	groupTasks, err := g.groupTaskService.GetGroupTasksByProjectID(ctx, projectID)
	if err != nil {
		log.Error(ctx, "Failed to get group tasks for project ID ", projectID, " error: ", err)
		return nil, err
	}
	return groupTasks, nil
}

func (g *GroupTaskUseCase) CreateGroupTask(ctx context.Context, userID int64, projectID int64, request *request.CreateGroupTaskDTO) (*entity.GroupTaskEntity, error) {
	project, err := g.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if project.OwnerID != userID {
		log.Error(ctx, "User does not have permission to create group task for project ID: ", projectID)
		return nil, errors.New(constant.ProjectNotFound)
	}
	result, err := g.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		return g.groupTaskService.CreateGroupTask(ctx, tx, projectID, request)
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.GroupTaskEntity), nil
}

func NewGroupTaskUseCase(
	groupTaskService service.IGroupTaskService,
	projectService service.IProjectService,
	taskService service.ITaskService,
	txService service.ITransactionService) IGroupTaskUseCase {
	return &GroupTaskUseCase{
		groupTaskService: groupTaskService,
		taskService:      taskService,
		projectService:   projectService,
		txService:        txService,
	}
}
