/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type INoteClientPort interface {
	CreateNote(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	SearchNotes(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	GetNoteByID(ctx context.Context, noteID int64) (*response.BaseResponse, error)
	UpdateNote(ctx context.Context, noteID int64, payload map[string]any) (*response.BaseResponse, error)
	DeleteNote(ctx context.Context, noteID int64) (*response.BaseResponse, error)
	GetNotesByProjectID(ctx context.Context, projectID int64) (*response.BaseResponse, error)
	GetNotesByTaskID(ctx context.Context, taskID int64) (*response.BaseResponse, error)
}
