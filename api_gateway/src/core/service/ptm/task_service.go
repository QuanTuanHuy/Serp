/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/ptm"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/ptm"
)

type ITaskService interface {
	CreateTask(ctx context.Context, req *request.CreateTaskRequest) (*response.BaseResponse, error)
	GetTaskByID(ctx context.Context, taskID int64) (*response.BaseResponse, error)
	UpdateTask(ctx context.Context, taskID int64, req *request.UpdateTaskRequest) (*response.BaseResponse, error)
	DeleteTask(ctx context.Context, taskID int64) (*response.BaseResponse, error)
	GetCommentsByTaskID(ctx context.Context, taskID int64) (*response.BaseResponse, error)
}

type TaskService struct {
	taskClient port.ITaskClientPort
}

func (t *TaskService) CreateTask(ctx context.Context, req *request.CreateTaskRequest) (*response.BaseResponse, error) {
	res, err := t.taskClient.CreateTask(ctx, req)
	if err != nil {
		log.Error(ctx, "Error creating task: %v", err)
		return nil, err
	}
	return res, nil
}

func (t *TaskService) GetTaskByID(ctx context.Context, taskID int64) (*response.BaseResponse, error) {
	res, err := t.taskClient.GetTaskByID(ctx, taskID)
	if err != nil {
		log.Error(ctx, "Error getting task by ID: %v", err)
		return nil, err
	}
	return res, nil
}

func (t *TaskService) UpdateTask(ctx context.Context, taskID int64, req *request.UpdateTaskRequest) (*response.BaseResponse, error) {
	res, err := t.taskClient.UpdateTask(ctx, taskID, req)
	if err != nil {
		log.Error(ctx, "Error updating task: %v", err)
		return nil, err
	}
	return res, nil
}

func (t *TaskService) DeleteTask(ctx context.Context, taskID int64) (*response.BaseResponse, error) {
	res, err := t.taskClient.DeleteTask(ctx, taskID)
	if err != nil {
		log.Error(ctx, "Error deleting task: %v", err)
		return nil, err
	}
	return res, nil
}

func (t *TaskService) GetCommentsByTaskID(ctx context.Context, taskID int64) (*response.BaseResponse, error) {
	res, err := t.taskClient.GetCommentsByTaskID(ctx, taskID)
	if err != nil {
		log.Error(ctx, "Error getting comments by task ID: %v", err)
		return nil, err
	}
	return res, nil
}

func NewTaskService(taskClient port.ITaskClientPort) ITaskService {
	return &TaskService{
		taskClient: taskClient,
	}
}
