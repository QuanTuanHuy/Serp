/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	"github.com/serp/ptm-task/src/core/service"
)

type INoteUseCase interface {
	CreateNote(ctx context.Context, userID int64, request *request.CreateNoteDTO) (*entity.NoteEntity, error)
	GetNotesByUserID(ctx context.Context, userID int64) ([]*entity.NoteEntity, error)
	GetNoteByID(ctx context.Context, userID, noteID int64) (*entity.NoteEntity, error)
	DeleteNote(ctx context.Context, userID, noteID int64) error
	LockNote(ctx context.Context, userID, noteID int64, request *request.LockNoteDTO) (*entity.NoteEntity, error)
	UnlockNote(ctx context.Context, userID, noteID int64, password string) (*entity.NoteEntity, error)
}

type NoteUseCase struct {
	noteService service.INoteService
}

func (n *NoteUseCase) CreateNote(ctx context.Context, userID int64, request *request.CreateNoteDTO) (*entity.NoteEntity, error) {
	return n.noteService.CreateNote(ctx, userID, request)
}

func (n *NoteUseCase) DeleteNote(ctx context.Context, userID int64, noteID int64) error {
	note, err := n.noteService.GetNoteByID(ctx, noteID)
	if err != nil {
		return err
	}
	if note.OwnerID != userID {
		return errors.New(constant.DeleteNoteForbidden)
	}
	err = n.noteService.DeleteNote(ctx, noteID)
	if err != nil {
		return err
	}
	return nil
}

func (n *NoteUseCase) GetNoteByID(ctx context.Context, userID int64, noteID int64) (*entity.NoteEntity, error) {
	note, err := n.noteService.GetNoteByID(ctx, noteID)
	if err != nil {
		return nil, err
	}
	if note.OwnerID != userID {
		log.Error(ctx, "User does not have permission to access note with ID: ", noteID)
		return nil, errors.New(constant.NoteNotFound)
	}
	return note, nil
}

func (n *NoteUseCase) GetNotesByUserID(ctx context.Context, userID int64) ([]*entity.NoteEntity, error) {
	return n.noteService.GetNotesByUserID(ctx, userID)
}

func (n *NoteUseCase) UnlockNote(ctx context.Context, userID int64, noteID int64, password string) (*entity.NoteEntity, error) {
	note, err := n.GetNoteByID(ctx, userID, noteID)
	if err != nil {
		return nil, err
	}
	if *note.NotePassword != password {
		log.Error(ctx, "Incorrect password for note with ID: ", noteID)
		return nil, errors.New(constant.NotePasswordMismatch)
	}
	note = mapper.UnlockNote(note)
	note, err = n.noteService.UpdateNote(ctx, note)
	if err != nil {
		log.Error(ctx, "Failed to unlock note: ", err)
		return nil, err
	}
	return note, nil
}

func (n *NoteUseCase) LockNote(ctx context.Context, userID, noteID int64, request *request.LockNoteDTO) (*entity.NoteEntity, error) {
	note, err := n.noteService.GetNoteByID(ctx, noteID)
	if err != nil {
		return nil, err
	}
	if note.OwnerID != userID {
		log.Error(ctx, "User does not have permission to lock note with ID: ", noteID)
		return nil, errors.New(constant.LockNoteForbidden)
	}
	note = mapper.LockNote(request, note)
	note, err = n.noteService.UpdateNote(ctx, note)
	if err != nil {
		log.Error(ctx, "Failed to lock note: ", err)
		return nil, err
	}
	return note, nil
}

func NewNoteUseCase(noteService service.INoteService) INoteUseCase {
	return &NoteUseCase{
		noteService: noteService,
	}
}
