/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IProjectClientPort interface {
	CreateProject(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	GetAllProjects(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	GetProjectByID(ctx context.Context, projectID int64) (*response.BaseResponse, error)
	GetTasksByProjectID(ctx context.Context, projectID int64) (*response.BaseResponse, error)
	UpdateProject(ctx context.Context, projectID int64, payload map[string]any) (*response.BaseResponse, error)
	DeleteProject(ctx context.Context, projectID int64) (*response.BaseResponse, error)
}
