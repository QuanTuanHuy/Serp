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

type INoteService interface {
	CreateNote(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	SearchNotes(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	GetNoteByID(ctx context.Context, noteID int64) (*response.BaseResponse, error)
	UpdateNote(ctx context.Context, noteID int64, payload map[string]any) (*response.BaseResponse, error)
	DeleteNote(ctx context.Context, noteID int64) (*response.BaseResponse, error)
	GetNotesByProjectID(ctx context.Context, projectID int64) (*response.BaseResponse, error)
	GetNotesByTaskID(ctx context.Context, taskID int64) (*response.BaseResponse, error)
}

type NoteService struct {
	noteClient port.INoteClientPort
}

func (n *NoteService) CreateNote(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := n.noteClient.CreateNote(ctx, payload)
	if err != nil {
		log.Error(ctx, "NoteService: CreateNote error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (n *NoteService) SearchNotes(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := n.noteClient.SearchNotes(ctx, payload)
	if err != nil {
		log.Error(ctx, "NoteService: SearchNotes error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (n *NoteService) GetNoteByID(ctx context.Context, noteID int64) (*response.BaseResponse, error) {
	res, err := n.noteClient.GetNoteByID(ctx, noteID)
	if err != nil {
		log.Error(ctx, "NoteService: GetNoteByID error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (n *NoteService) UpdateNote(ctx context.Context, noteID int64, payload map[string]any) (*response.BaseResponse, error) {
	res, err := n.noteClient.UpdateNote(ctx, noteID, payload)
	if err != nil {
		log.Error(ctx, "NoteService: UpdateNote error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (n *NoteService) DeleteNote(ctx context.Context, noteID int64) (*response.BaseResponse, error) {
	res, err := n.noteClient.DeleteNote(ctx, noteID)
	if err != nil {
		log.Error(ctx, "NoteService: DeleteNote error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (n *NoteService) GetNotesByProjectID(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	res, err := n.noteClient.GetNotesByProjectID(ctx, projectID)
	if err != nil {
		log.Error(ctx, "NoteService: GetNotesByProjectID error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (n *NoteService) GetNotesByTaskID(ctx context.Context, taskID int64) (*response.BaseResponse, error) {
	res, err := n.noteClient.GetNotesByTaskID(ctx, taskID)
	if err != nil {
		log.Error(ctx, "NoteService: GetNotesByTaskID error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewNoteService(noteClient port.INoteClientPort) INoteService {
	return &NoteService{
		noteClient: noteClient,
	}
}
