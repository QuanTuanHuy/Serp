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

type IGroupTaskService interface {
	CreateGroupTask(ctx context.Context, req *request.CreateGroupTaskRequest) (*response.BaseResponse, error)
	GetGroupTaskByID(ctx context.Context, groupTaskID int64) (*response.BaseResponse, error)
}

type GroupTaskService struct {
	groupTaskClient port.IGroupTaskClientPort
}

func (g *GroupTaskService) CreateGroupTask(ctx context.Context, req *request.CreateGroupTaskRequest) (*response.BaseResponse, error) {
	res, err := g.groupTaskClient.CreateGroupTask(ctx, req)
	if err != nil {
		log.Error(ctx, "Error creating group task: %v", err)
		return nil, err
	}
	return res, nil
}

func (g *GroupTaskService) GetGroupTaskByID(ctx context.Context, groupTaskID int64) (*response.BaseResponse, error) {
	res, err := g.groupTaskClient.GetGroupTaskByID(ctx, groupTaskID)
	if err != nil {
		log.Error(ctx, "Error getting group task by ID: %v", err)
		return nil, err
	}
	return res, nil
}

func NewGroupTaskService(groupTaskClient port.IGroupTaskClientPort) IGroupTaskService {
	return &GroupTaskService{
		groupTaskClient: groupTaskClient,
	}
}
