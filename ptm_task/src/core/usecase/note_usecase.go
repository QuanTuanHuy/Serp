/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"

	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/core/service"
	"gorm.io/gorm"
)

type INoteUseCase interface {
	CreateNote(ctx context.Context, userID int64, note *entity.NoteEntity) (*entity.NoteEntity, error)
	CreateNoteForTask(ctx context.Context, userID int64, taskID int64, content string, attachments []entity.NoteAttachment) (*entity.NoteEntity, error)
	CreateNoteForProject(ctx context.Context, userID int64, projectID int64, content string, attachments []entity.NoteAttachment) (*entity.NoteEntity, error)

	UpdateNote(ctx context.Context, userID int64, note *entity.NoteEntity) error
	TogglePin(ctx context.Context, userID int64, noteID int64) error
	DeleteNote(ctx context.Context, userID int64, noteID int64) error
	BulkDeleteNotes(ctx context.Context, userID int64, noteIDs []int64) error

	GetNoteByID(ctx context.Context, userID int64, noteID int64) (*entity.NoteEntity, error)
	GetNotesByTaskID(ctx context.Context, userID int64, taskID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error)
	GetNotesByProjectID(ctx context.Context, userID int64, projectID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error)
	GetNotesByUserID(ctx context.Context, userID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error)
	SearchNotes(ctx context.Context, userID int64, searchQuery string, filter *store.NoteFilter) ([]*entity.NoteEntity, error)

	DeleteNotesByTask(ctx context.Context, userID int64, taskID int64) error
	DeleteNotesByProject(ctx context.Context, userID int64, projectID int64) error
}

type noteUseCase struct {
	noteService    service.INoteService
	taskService    service.ITaskService
	projectService service.IProjectService
	txService      service.ITransactionService
}

func NewNoteUseCase(
	noteService service.INoteService,
	taskService service.ITaskService,
	projectService service.IProjectService,
	txService service.ITransactionService,
) INoteUseCase {
	return &noteUseCase{
		noteService:    noteService,
		taskService:    taskService,
		projectService: projectService,
		txService:      txService,
	}
}

func (u *noteUseCase) CreateNote(ctx context.Context, userID int64, note *entity.NoteEntity) (*entity.NoteEntity, error) {
	if note.TaskID != nil {
		task, err := u.taskService.GetTaskByID(ctx, *note.TaskID)
		if err != nil {
			return nil, err
		}
		if task.UserID != userID {
			return nil, errors.New(constant.TaskNotFound)
		}
	}

	if note.ProjectID != nil {
		project, err := u.projectService.GetProjectByID(ctx, *note.ProjectID)
		if err != nil {
			return nil, err
		}
		if project.UserID != userID {
			return nil, errors.New(constant.ProjectNotFound)
		}
	}

	var createdNote *entity.NoteEntity
	err := u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		created, err := u.noteService.CreateNote(ctx, tx, userID, note)
		if err != nil {
			return err
		}
		createdNote = created
		return nil
	})
	if err != nil {
		return nil, err
	}
	return createdNote, nil
}

func (u *noteUseCase) CreateNoteForTask(ctx context.Context, userID int64, taskID int64, content string, attachments []entity.NoteAttachment) (*entity.NoteEntity, error) {
	task, err := u.taskService.GetTaskByID(ctx, taskID)
	if err != nil {
		return nil, err
	}
	if task.UserID != userID {
		return nil, errors.New(constant.UpdateTaskForbidden)
	}

	note := entity.NewNoteEntity()
	note.TaskID = &taskID
	note.TenantID = task.TenantID
	note.Content = content
	note.Attachments = attachments

	return u.CreateNote(ctx, userID, note)
}

func (u *noteUseCase) CreateNoteForProject(ctx context.Context, userID int64, projectID int64, content string, attachments []entity.NoteAttachment) (*entity.NoteEntity, error) {
	project, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if project.UserID != userID {
		return nil, errors.New(constant.UpdateProjectForbidden)
	}

	note := entity.NewNoteEntity()
	note.ProjectID = &projectID
	note.TenantID = project.TenantID
	note.Content = content
	note.Attachments = attachments

	return u.CreateNote(ctx, userID, note)
}

func (u *noteUseCase) UpdateNote(ctx context.Context, userID int64, note *entity.NoteEntity) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.noteService.UpdateNote(ctx, tx, userID, note)
	})
}

func (u *noteUseCase) TogglePin(ctx context.Context, userID int64, noteID int64) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.noteService.TogglePin(ctx, tx, userID, noteID)
	})
}

func (u *noteUseCase) DeleteNote(ctx context.Context, userID int64, noteID int64) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.noteService.DeleteNote(ctx, tx, userID, noteID)
	})
}

func (u *noteUseCase) BulkDeleteNotes(ctx context.Context, userID int64, noteIDs []int64) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		for _, noteID := range noteIDs {
			if err := u.noteService.DeleteNote(ctx, tx, userID, noteID); err != nil {
				return err
			}
		}
		return nil
	})
}

func (u *noteUseCase) GetNoteByID(ctx context.Context, userID int64, noteID int64) (*entity.NoteEntity, error) {
	note, err := u.noteService.GetNoteByID(ctx, noteID)
	if err != nil {
		return nil, err
	}
	if note.UserID != userID {
		return nil, errors.New(constant.GetNoteForbidden)
	}
	return note, nil
}

func (u *noteUseCase) GetNotesByTaskID(ctx context.Context, userID int64, taskID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error) {
	task, err := u.taskService.GetTaskByID(ctx, taskID)
	if err != nil {
		return nil, err
	}
	if task.UserID != userID {
		return nil, errors.New(constant.GetNoteForbidden)
	}

	return u.noteService.GetNotesByTaskID(ctx, taskID, filter)
}

func (u *noteUseCase) GetNotesByProjectID(ctx context.Context, userID int64, projectID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error) {
	project, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if project.UserID != userID {
		return nil, errors.New(constant.GetNoteForbidden)
	}

	return u.noteService.GetNotesByProjectID(ctx, projectID, filter)
}

func (u *noteUseCase) GetNotesByUserID(ctx context.Context, userID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error) {
	return u.noteService.GetNotesByUserID(ctx, userID, filter)
}

func (u *noteUseCase) SearchNotes(ctx context.Context, userID int64, searchQuery string, filter *store.NoteFilter) ([]*entity.NoteEntity, error) {
	return u.noteService.SearchNotes(ctx, userID, searchQuery, filter)
}

func (u *noteUseCase) DeleteNotesByTask(ctx context.Context, userID int64, taskID int64) error {
	task, err := u.taskService.GetTaskByID(ctx, taskID)
	if err != nil {
		return err
	}
	if task.UserID != userID {
		return errors.New(constant.DeleteNoteForbidden)
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.noteService.DeleteNote(ctx, tx, userID, taskID)
	})
}

func (u *noteUseCase) DeleteNotesByProject(ctx context.Context, userID int64, projectID int64) error {
	project, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return err
	}
	if project.UserID != userID {
		return errors.New(constant.DeleteNoteForbidden)
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.noteService.DeleteNote(ctx, tx, userID, projectID)
	})
}
