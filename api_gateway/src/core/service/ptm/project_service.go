/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"fmt"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/ptm"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/ptm"
)

type IProjectService interface {
	GetProjectsByUserID(ctx context.Context) (*response.BaseResponse, error)
	CreateProject(ctx context.Context, req *request.CreateProjectRequest) (*response.BaseResponse, error)
	UpdateProject(ctx context.Context, projectID int64, req *request.UpdateProjectRequest) (*response.BaseResponse, error)
	GetProjectByID(ctx context.Context, projectID int64) (*response.BaseResponse, error)
	GetProjects(ctx context.Context, params *request.GetProjectsRequest) (*response.BaseResponse, error)
	GetProjectsByName(ctx context.Context, name string) (*response.BaseResponse, error)
	ArchiveProject(ctx context.Context, projectID int64) (*response.BaseResponse, error)
	GetGroupTasksByProjectID(ctx context.Context, projectID int64) (*response.BaseResponse, error)
}

type ProjectService struct {
	projectClient port.IProjectClientPort
}

func (p *ProjectService) GetProjectsByUserID(ctx context.Context) (*response.BaseResponse, error) {
	res, err := p.projectClient.GetProjectsByUserID(ctx)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Error getting projects for user: %v", err))
		return nil, fmt.Errorf("failed to get projects for user: %w", err)
	}
	return res, nil
}

func (p *ProjectService) CreateProject(ctx context.Context, req *request.CreateProjectRequest) (*response.BaseResponse, error) {
	res, err := p.projectClient.CreateProject(ctx, req)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Error creating project: %v", err))
		return nil, fmt.Errorf("failed to create project: %w", err)
	}
	return res, nil
}

func (p *ProjectService) UpdateProject(ctx context.Context, projectID int64, req *request.UpdateProjectRequest) (*response.BaseResponse, error) {
	res, err := p.projectClient.UpdateProject(ctx, projectID, req)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Error updating project: %v", err))
		return nil, fmt.Errorf("failed to update project: %w", err)
	}
	return res, nil
}

func (p *ProjectService) GetProjectByID(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	res, err := p.projectClient.GetProjectByID(ctx, projectID)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Error getting project by ID: %v", err))
		return nil, fmt.Errorf("failed to get project by ID: %w", err)
	}
	return res, nil
}

func (p *ProjectService) GetProjects(ctx context.Context, params *request.GetProjectsRequest) (*response.BaseResponse, error) {
	res, err := p.projectClient.GetProjects(ctx, params)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Error getting projects: %v", err))
		return nil, fmt.Errorf("failed to get projects: %w", err)
	}
	return res, nil
}

func (p *ProjectService) GetProjectsByName(ctx context.Context, name string) (*response.BaseResponse, error) {
	res, err := p.projectClient.GetProjectsByName(ctx, name)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Error getting projects by name: %v", err))
		return nil, fmt.Errorf("failed to get projects by name: %w", err)
	}
	return res, nil
}

func (p *ProjectService) ArchiveProject(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	res, err := p.projectClient.ArchiveProject(ctx, projectID)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Error archiving project: %v", err))
		return nil, fmt.Errorf("failed to archive project: %w", err)
	}
	return res, nil
}

func (p *ProjectService) GetGroupTasksByProjectID(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	res, err := p.projectClient.GetGroupTasksByProjectID(ctx, projectID)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Error getting group tasks by project ID: %v", err))
		return nil, fmt.Errorf("failed to get group tasks by project ID: %w", err)
	}
	return res, nil
}

func NewProjectService(projectClient port.IProjectClientPort) IProjectService {
	return &ProjectService{
		projectClient: projectClient,
	}
}
