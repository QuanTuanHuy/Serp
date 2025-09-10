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

type INoteService interface {
	CreateNote(ctx context.Context, req *request.CreateNoteRequest) (*response.BaseResponse, error)
	GetAllNotes(ctx context.Context) (*response.BaseResponse, error)
	GetNoteByID(ctx context.Context, noteID int64) (*response.BaseResponse, error)
	DeleteNote(ctx context.Context, noteID int64) (*response.BaseResponse, error)
	LockNote(ctx context.Context, noteID int64) (*response.BaseResponse, error)
	UnlockNote(ctx context.Context, noteID int64) (*response.BaseResponse, error)
}

type NoteService struct {
	noteClient port.INoteClientPort
}

func (n *NoteService) CreateNote(ctx context.Context, req *request.CreateNoteRequest) (*response.BaseResponse, error) {
	res, err := n.noteClient.CreateNote(ctx, req)
	if err != nil {
		log.Error(ctx, "Error creating note: %v", err)
		return nil, err
	}
	return res, nil
}

func (n *NoteService) GetAllNotes(ctx context.Context) (*response.BaseResponse, error) {
	res, err := n.noteClient.GetAllNotes(ctx)
	if err != nil {
		log.Error(ctx, "Error getting all notes: %v", err)
		return nil, err
	}
	return res, nil
}

func (n *NoteService) GetNoteByID(ctx context.Context, noteID int64) (*response.BaseResponse, error) {
	res, err := n.noteClient.GetNoteByID(ctx, noteID)
	if err != nil {
		log.Error(ctx, "Error getting note by ID: %v", err)
		return nil, err
	}
	return res, nil
}

func (n *NoteService) DeleteNote(ctx context.Context, noteID int64) (*response.BaseResponse, error) {
	res, err := n.noteClient.DeleteNote(ctx, noteID)
	if err != nil {
		log.Error(ctx, "Error deleting note: %v", err)
		return nil, err
	}
	return res, nil
}

func (n *NoteService) LockNote(ctx context.Context, noteID int64) (*response.BaseResponse, error) {
	res, err := n.noteClient.LockNote(ctx, noteID)
	if err != nil {
		log.Error(ctx, "Error locking note: %v", err)
		return nil, err
	}
	return res, nil
}

func (n *NoteService) UnlockNote(ctx context.Context, noteID int64) (*response.BaseResponse, error) {
	res, err := n.noteClient.UnlockNote(ctx, noteID)
	if err != nil {
		log.Error(ctx, "Error unlocking note: %v", err)
		return nil, err
	}
	return res, nil
}

func NewNoteService(noteClient port.INoteClientPort) INoteService {
	return &NoteService{
		noteClient: noteClient,
	}
}
