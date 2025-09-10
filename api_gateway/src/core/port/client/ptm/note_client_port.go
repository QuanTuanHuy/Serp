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

type INoteClientPort interface {
	CreateNote(ctx context.Context, req *request.CreateNoteRequest) (*response.BaseResponse, error)
	GetAllNotes(ctx context.Context) (*response.BaseResponse, error)
	GetNoteByID(ctx context.Context, noteID int64) (*response.BaseResponse, error)
	DeleteNote(ctx context.Context, noteID int64) (*response.BaseResponse, error)
	LockNote(ctx context.Context, noteID int64) (*response.BaseResponse, error)
	UnlockNote(ctx context.Context, noteID int64) (*response.BaseResponse, error)
}
