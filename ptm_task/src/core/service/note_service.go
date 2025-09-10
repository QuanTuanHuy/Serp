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
)

type INoteService interface {
	CreateNote(ctx context.Context, userID int64, request *request.CreateNoteDTO) (*entity.NoteEntity, error)
	GetNotesByUserID(ctx context.Context, userID int64) ([]*entity.NoteEntity, error)
	GetNoteByID(ctx context.Context, noteID int64) (*entity.NoteEntity, error)
	UpdateNote(ctx context.Context, note *entity.NoteEntity) (*entity.NoteEntity, error)
	DeleteNote(ctx context.Context, noteID int64) error
}

type NoteService struct {
	notePort  port.INotePort
	dbTxPort  port.IDBTransactionPort
	redisPort port2.IRedisPort
}

func (n *NoteService) UpdateNote(ctx context.Context, note *entity.NoteEntity) (*entity.NoteEntity, error) {
	var err error
	tx := n.dbTxPort.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			log.Error(ctx, "Recovered from panic while updating note: ", r)
			n.dbTxPort.Rollback(tx)
		}
		if err != nil {
			log.Error(ctx, "Failed to update note: ", err)
			n.dbTxPort.Rollback(tx)
		}
	}()

	note, err = n.notePort.UpdateNote(ctx, tx, note)
	if err != nil {
		log.Error(ctx, "Failed to update note: ", err)
		return nil, err
	}
	err = n.dbTxPort.Commit(tx)
	if err != nil {
		log.Error(ctx, "Failed to commit transaction after updating note: ", err)
		return nil, err
	}
	return note, nil
}

func (n *NoteService) CreateNote(ctx context.Context, userID int64, request *request.CreateNoteDTO) (*entity.NoteEntity, error) {
	note := mapper.ToNoteEntity(request)
	note.OwnerID = userID
	if note.Name == "" {
		note.Name = n.createNoteName()
	}

	var err error
	tx := n.dbTxPort.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			log.Error(ctx, "Recovered from panic while creating note: ", r)
			n.dbTxPort.Rollback(tx)
		}
		if err != nil {
			log.Error(ctx, "Failed to create note: ", err)
			n.dbTxPort.Rollback(tx)
		}
	}()

	note, err = n.notePort.CreateNode(ctx, tx, note)
	if err != nil {
		log.Error(ctx, "Failed to create note: ", err)
		return nil, err
	}
	err = n.dbTxPort.Commit(tx)
	if err != nil {
		log.Error(ctx, "Failed to commit transaction after creating note: ", err)
		return nil, err
	}
	return note, nil
}

func (n *NoteService) createNoteName() string {
	return time.Now().Format("2006-1-2 15:04:05")
}

func (n *NoteService) DeleteNote(ctx context.Context, noteID int64) error {
	var err error
	tx := n.dbTxPort.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			log.Error(ctx, "Recovered from panic while deleting note: ", r)
			n.dbTxPort.Rollback(tx)
		}
		if err != nil {
			log.Error(ctx, "Failed to delete note: ", err)
			n.dbTxPort.Rollback(tx)
		}
	}()

	err = n.notePort.DeleteNote(ctx, tx, noteID)
	if err != nil {
		log.Error(ctx, "Failed to delete note: ", err)
		return err
	}
	err = n.dbTxPort.Commit(tx)
	if err != nil {
		log.Error(ctx, "Failed to commit transaction after deleting note: ", err)
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

func NewNoteService(notePort port.INotePort, dbTxPort port.IDBTransactionPort, redisPort port2.IRedisPort) INoteService {
	return &NoteService{
		notePort:  notePort,
		dbTxPort:  dbTxPort,
		redisPort: redisPort,
	}
}
