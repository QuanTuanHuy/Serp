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
	"gorm.io/gorm"
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
	txService   service.ITransactionService
}

func (n *NoteUseCase) CreateNote(ctx context.Context, userID int64, request *request.CreateNoteDTO) (*entity.NoteEntity, error) {
	result, err := n.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		note, err := n.noteService.CreateNote(ctx, tx, userID, request)
		if err != nil {
			return nil, err
		}
		return note, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.NoteEntity), nil
}

func (n *NoteUseCase) DeleteNote(ctx context.Context, userID int64, noteID int64) error {
	note, err := n.noteService.GetNoteByID(ctx, noteID)
	if err != nil {
		return err
	}
	if note.OwnerID != userID {
		return errors.New(constant.DeleteNoteForbidden)
	}
	return n.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return n.noteService.DeleteNote(ctx, tx, noteID)
	})
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
	result, err := n.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		updatedNote, err := n.noteService.UpdateNote(ctx, tx, note)
		if err != nil {
			return nil, err
		}
		return updatedNote, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.NoteEntity), nil
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
	result, err := n.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		updatedNote, err := n.noteService.UpdateNote(ctx, tx, note)
		if err != nil {
			return nil, err
		}
		return updatedNote, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.NoteEntity), nil
}

func NewNoteUseCase(noteService service.INoteService, txService service.ITransactionService) INoteUseCase {
	return &NoteUseCase{
		noteService: noteService,
		txService:   txService,
	}
}
