/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"time"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	port2 "github.com/serp/ptm-task/src/core/port/client"
	port "github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

type INoteService interface {
	CreateNote(ctx context.Context, tx *gorm.DB, userID int64, request *request.CreateNoteDTO) (*entity.NoteEntity, error)
	GetNotesByUserID(ctx context.Context, userID int64) ([]*entity.NoteEntity, error)
	GetNoteByID(ctx context.Context, noteID int64) (*entity.NoteEntity, error)
	UpdateNote(ctx context.Context, tx *gorm.DB, note *entity.NoteEntity) (*entity.NoteEntity, error)
	DeleteNote(ctx context.Context, tx *gorm.DB, noteID int64) error
}

type NoteService struct {
	notePort  port.INotePort
	redisPort port2.IRedisPort
}

func (n *NoteService) UpdateNote(ctx context.Context, tx *gorm.DB, note *entity.NoteEntity) (*entity.NoteEntity, error) {
	note, err := n.notePort.UpdateNote(ctx, tx, note)
	if err != nil {
		log.Error(ctx, "Failed to update note: ", err)
		return nil, err
	}
	return note, nil
}

func (n *NoteService) CreateNote(ctx context.Context, tx *gorm.DB, userID int64, request *request.CreateNoteDTO) (*entity.NoteEntity, error) {
	note := mapper.ToNoteEntity(request)
	note.OwnerID = userID
	if note.Name == "" {
		note.Name = n.createNoteName()
	}

	note, err := n.notePort.CreateNode(ctx, tx, note)
	if err != nil {
		log.Error(ctx, "Failed to create note: ", err)
		return nil, err
	}
	return note, nil
}

func (n *NoteService) createNoteName() string {
	return time.Now().Format("2006-1-2 15:04:05")
}

func (n *NoteService) DeleteNote(ctx context.Context, tx *gorm.DB, noteID int64) error {
	err := n.notePort.DeleteNote(ctx, tx, noteID)
	if err != nil {
		log.Error(ctx, "Failed to delete note: ", err)
		return err
	}
	return nil
}

func (n *NoteService) GetNoteByID(ctx context.Context, noteID int64) (*entity.NoteEntity, error) {
	note, err := n.notePort.GetNoteByID(ctx, noteID)
	if err != nil {
		log.Error(ctx, "Failed to get note by ID: ", err)
		return nil, err
	}
	if note == nil {
		log.Error(ctx, "Note not found with ID: ", noteID)
		return nil, errors.New(constant.NoteNotFound)
	}
	return note, nil
}

func (n *NoteService) GetNotesByUserID(ctx context.Context, userID int64) ([]*entity.NoteEntity, error) {
	notes, err := n.notePort.GetNotesByUserID(ctx, userID)
	if err != nil {
		log.Error(ctx, "Failed to get notes by user ID: ", err)
		return nil, err
	}
	return notes, nil
}

func NewNoteService(notePort port.INotePort, redisPort port2.IRedisPort) INoteService {
	return &NoteService{
		notePort:  notePort,
		redisPort: redisPort,
	}
}
