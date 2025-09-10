/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	request "github.com/serp/api-gateway/src/core/domain/dto/request/ptm"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IProjectClientPort interface {
	GetProjectsByUserID(ctx context.Context) (*response.BaseResponse, error)
	CreateProject(ctx context.Context, req *request.CreateProjectRequest) (*response.BaseResponse, error)
	UpdateProject(ctx context.Context, projectID int64, req *request.UpdateProjectRequest) (*response.BaseResponse, error)
	GetProjectByID(ctx context.Context, projectID int64) (*response.BaseResponse, error)
	GetProjects(ctx context.Context, params *request.GetProjectsRequest) (*response.BaseResponse, error)
	GetProjectsByName(ctx context.Context, name string) (*response.BaseResponse, error)
	ArchiveProject(ctx context.Context, projectID int64) (*response.BaseResponse, error)
	GetGroupTasksByProjectID(ctx context.Context, projectID int64) (*response.BaseResponse, error)
}
