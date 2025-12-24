/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/ptm"
)

type ITaskService interface {
	CreateTask(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	GetTasksByUserID(ctx context.Context) (*response.BaseResponse, error)
	GetTaskByID(ctx context.Context, taskID int64) (*response.BaseResponse, error)
	UpdateTask(ctx context.Context, taskID int64, payload map[string]any) (*response.BaseResponse, error)
	DeleteTask(ctx context.Context, taskID int64) (*response.BaseResponse, error)
}

type TaskService struct {
	taskClient port.ITaskClientPort
}

func (t *TaskService) CreateTask(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := t.taskClient.CreateTask(ctx, payload)
	if err != nil {
		log.Error(ctx, "TaskService: CreateTask error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (t *TaskService) GetTasksByUserID(ctx context.Context) (*response.BaseResponse, error) {
	res, err := t.taskClient.GetTasksByUserID(ctx)
	if err != nil {
		log.Error(ctx, "TaskService: GetTasksByUserID error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (t *TaskService) GetTaskByID(ctx context.Context, taskID int64) (*response.BaseResponse, error) {
	res, err := t.taskClient.GetTaskByID(ctx, taskID)
	if err != nil {
		log.Error(ctx, "TaskService: GetTaskByID error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (t *TaskService) UpdateTask(ctx context.Context, taskID int64, payload map[string]any) (*response.BaseResponse, error) {
	res, err := t.taskClient.UpdateTask(ctx, taskID, payload)
	if err != nil {
		log.Error(ctx, "TaskService: UpdateTask error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (t *TaskService) DeleteTask(ctx context.Context, taskID int64) (*response.BaseResponse, error) {
	res, err := t.taskClient.DeleteTask(ctx, taskID)
	if err != nil {
		log.Error(ctx, "TaskService: DeleteTask error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewTaskService(taskClient port.ITaskClientPort) ITaskService {
	return &TaskService{
		taskClient: taskClient,
	}
}
