/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"fmt"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	port2 "github.com/serp/ptm-task/src/core/port/client"
	port "github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

type IGroupTaskService interface {
	CreateGroupTask(ctx context.Context, tx *gorm.DB, projectID int64, request *request.CreateGroupTaskDTO) (*entity.GroupTaskEntity, error)
	UpdateGroupTask(ctx context.Context, tx *gorm.DB, groupTaskID int64, groupTask *entity.GroupTaskEntity) (*entity.GroupTaskEntity, error)
	GetDefaultGroupTaskByProjectID(ctx context.Context, projectID int64) (*entity.GroupTaskEntity, error)
	GetGroupTasksByProjectID(ctx context.Context, projectID int64) ([]*entity.GroupTaskEntity, error)
	GetGroupTaskByID(ctx context.Context, groupTaskID int64) (*entity.GroupTaskEntity, error)
	CalculateTasksInGroupTask(ctx context.Context, tx *gorm.DB, groupTaskID int64) (*entity.GroupTaskEntity, error)
}

type GroupTaskService struct {
	groupTaskPort port.IGroupTaskPort
	taskPort      port.ITaskPort
	redisPort     port2.IRedisPort
}

func (g *GroupTaskService) GetGroupTaskByID(ctx context.Context, groupTaskID int64) (*entity.GroupTaskEntity, error) {
	groupTask, err := g.groupTaskPort.GetGroupTaskByID(ctx, groupTaskID)
	if err != nil {
		log.Error(ctx, "Failed to get group task by ID ", groupTaskID, " error ", err)
		return nil, err
	}
	if groupTask == nil {
		log.Warn(ctx, "Group task not found for ID ", groupTaskID)
		return nil, errors.New(constant.GroupTaskNotFound)
	}
	return groupTask, nil
}

func (g *GroupTaskService) CreateGroupTask(ctx context.Context, tx *gorm.DB, projectID int64, request *request.CreateGroupTaskDTO) (*entity.GroupTaskEntity, error) {
	var err error
	defaultGroupTask, err := g.GetDefaultGroupTaskByProjectID(ctx, projectID)
	if err != nil {
		return nil, err
	}
	groupTask := mapper.CreateGroupTaskMapper(request)
	if defaultGroupTask == nil {
		groupTask.IsDefault = true
	}

	groupTask, err = g.groupTaskPort.CreateGroupTask(ctx, tx, groupTask)
	if err != nil {
		log.Error(ctx, "Failed to create group task: ", err)
		return nil, err
	}

	go func() {
		err = g.redisPort.DeleteKeyFromRedis(ctx, fmt.Sprintf(constant.GroupTasksByProjectID, projectID))
		if err != nil {
			log.Error(ctx, "Failed to delete group tasks cache when creating new group task for project ID ", projectID, " error: ", err)
		}
	}()

	return groupTask, nil
}

func (g *GroupTaskService) UpdateGroupTask(ctx context.Context, tx *gorm.DB, groupTaskID int64, groupTask *entity.GroupTaskEntity) (*entity.GroupTaskEntity, error) {
	groupTask, err := g.groupTaskPort.UpdateGroupTask(ctx, tx, groupTaskID, groupTask)
	if err != nil {
		log.Error(ctx, "Failed to update group task ID ", groupTaskID, " error ", err)
		return nil, err
	}

	go func() {
		err = g.redisPort.DeleteKeyFromRedis(ctx, fmt.Sprintf(constant.GroupTasksByProjectID, groupTask.ProjectID))
		if err != nil {
			log.Error(ctx, "Failed to delete group tasks cache when updating group task ID ", groupTaskID, " error: ", err)
		}
	}()

	return groupTask, nil
}

func (g *GroupTaskService) CalculateTasksInGroupTask(ctx context.Context, tx *gorm.DB, groupTaskID int64) (*entity.GroupTaskEntity, error) {
	groupTask, err := g.GetGroupTaskByID(ctx, groupTaskID)
	if err != nil {
		log.Error(ctx, "Failed to get group task by ID ", groupTaskID, " error ", err)
		return nil, err
	}

	tasks, err := g.taskPort.GetTasksByGroupTaskID(ctx, groupTaskID)
	if err != nil {
		log.Error(ctx, "Failed to get tasks for group task ID ", groupTaskID, " error ", err)
		return nil, err
	}
	groupTask.TotalTasks = len(tasks)
	doneTasks := int(0)

	for _, task := range tasks {
		if task.Status == enum.Done {
			doneTasks++
		}
	}
	groupTask.CompletedTasks = doneTasks
	groupTask, err = g.UpdateGroupTask(ctx, tx, groupTaskID, groupTask)
	if err != nil {
		log.Error(ctx, "Failed to update group task after calculating tasks: ", err)
		return nil, err
	}
	return groupTask, nil
}

func (g *GroupTaskService) GetGroupTasksByProjectID(ctx context.Context, projectID int64) ([]*entity.GroupTaskEntity, error) {
	var groupTasks []*entity.GroupTaskEntity
	cacheKey := fmt.Sprintf(constant.GroupTasksByProjectID, projectID)
	err := g.redisPort.GetFromRedis(ctx, cacheKey, &groupTasks)
	if err != nil {
		log.Error(ctx, "Failed to get group tasks from redis for project ID ", projectID, " error: ", err)
		return nil, err
	}
	if groupTasks != nil {
		return groupTasks, nil
	}

	groupTasks, err = g.groupTaskPort.GetGroupTasksByProjectID(ctx, projectID)
	if err != nil {
		log.Error(ctx, "Failed to get group tasks by project ID ", projectID, " error ", err)
		return nil, err
	}

	go func() {
		if len(groupTasks) > 0 {
			err = g.redisPort.SetToRedis(ctx, cacheKey, groupTasks, constant.DefaultTTL)
		}
	}()

	return groupTasks, nil
}

func (g *GroupTaskService) GetDefaultGroupTaskByProjectID(ctx context.Context, projectID int64) (*entity.GroupTaskEntity, error) {
	groupTask, err := g.groupTaskPort.GetDefaultGroupTaskByProjectID(ctx, projectID)
	if err != nil {
		log.Error(ctx, "Failed to get default group task by project ID ", projectID, " error ", err)
		return nil, err
	}
	return groupTask, nil
}

func NewGroupTaskService(
	groupTaskPort port.IGroupTaskPort,
	taskPort port.ITaskPort,
	redisPort port2.IRedisPort) IGroupTaskService {
	return &GroupTaskService{
		groupTaskPort: groupTaskPort,
		taskPort:      taskPort,
		redisPort:     redisPort,
	}
}
