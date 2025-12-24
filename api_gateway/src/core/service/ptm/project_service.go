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

type IProjectService interface {
	CreateProject(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	GetAllProjects(ctx context.Context, payload map[string]string) (*response.BaseResponse, error)
	GetProjectByID(ctx context.Context, projectID int64) (*response.BaseResponse, error)
	GetTasksByProjectID(ctx context.Context, projectID int64) (*response.BaseResponse, error)
	UpdateProject(ctx context.Context, projectID int64, payload map[string]any) (*response.BaseResponse, error)
	DeleteProject(ctx context.Context, projectID int64) (*response.BaseResponse, error)
}

type ProjectService struct {
	projectClient port.IProjectClientPort
}

func (p *ProjectService) CreateProject(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := p.projectClient.CreateProject(ctx, payload)
	if err != nil {
		log.Error(ctx, "ProjectService: CreateProject error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProjectService) GetAllProjects(ctx context.Context, payload map[string]string) (*response.BaseResponse, error) {
	res, err := p.projectClient.GetAllProjects(ctx, payload)
	if err != nil {
		log.Error(ctx, "ProjectService: GetAllProjects error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProjectService) GetProjectByID(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	res, err := p.projectClient.GetProjectByID(ctx, projectID)
	if err != nil {
		log.Error(ctx, "ProjectService: GetProjectByID error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProjectService) GetTasksByProjectID(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	res, err := p.projectClient.GetTasksByProjectID(ctx, projectID)
	if err != nil {
		log.Error(ctx, "ProjectService: GetTasksByProjectID error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProjectService) UpdateProject(ctx context.Context, projectID int64, payload map[string]any) (*response.BaseResponse, error) {
	res, err := p.projectClient.UpdateProject(ctx, projectID, payload)
	if err != nil {
		log.Error(ctx, "ProjectService: UpdateProject error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProjectService) DeleteProject(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	res, err := p.projectClient.DeleteProject(ctx, projectID)
	if err != nil {
		log.Error(ctx, "ProjectService: DeleteProject error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewProjectService(projectClient port.IProjectClientPort) IProjectService {
	return &ProjectService{
		projectClient: projectClient,
	}
}
