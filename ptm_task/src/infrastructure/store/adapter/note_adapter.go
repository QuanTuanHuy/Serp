/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/infrastructure/store/mapper"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type NoteAdapter struct {
	db     *gorm.DB
	mapper *mapper.NoteMapper
}

func NewNoteAdapter(db *gorm.DB) store.INotePort {
	return &NoteAdapter{
		db:     db,
		mapper: mapper.NewNoteMapper(),
	}
}

func (a *NoteAdapter) CreateNote(ctx context.Context, tx *gorm.DB, note *entity.NoteEntity) error {
	db := a.getDB(tx)
	noteModel := a.mapper.ToModel(note)
	if err := db.WithContext(ctx).Create(noteModel).Error; err != nil {
		return fmt.Errorf("failed to create note: %w", err)
	}
	return nil
}

func (a *NoteAdapter) CreateNotes(ctx context.Context, tx *gorm.DB, notes []*entity.NoteEntity) error {
	if len(notes) == 0 {
		return nil
	}

	db := a.getDB(tx)
	noteModels := a.mapper.ToModels(notes)
	if err := db.WithContext(ctx).CreateInBatches(noteModels, 100).Error; err != nil {
		return fmt.Errorf("failed to create notes: %w", err)
	}

	return nil
}

func (a *NoteAdapter) GetNoteByID(ctx context.Context, id int64) (*entity.NoteEntity, error) {
	var noteModel model.NoteModel

	if err := a.db.WithContext(ctx).First(&noteModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get note by id: %w", err)
	}

	return a.mapper.ToEntity(&noteModel), nil
}

func (a *NoteAdapter) GetNotesByIDs(ctx context.Context, ids []int64) ([]*entity.NoteEntity, error) {
	if len(ids) == 0 {
		return []*entity.NoteEntity{}, nil
	}
	if len(ids) == 1 {
		note, err := a.GetNoteByID(ctx, ids[0])
		if err != nil {
			return nil, err
		}
		if note == nil {
			return []*entity.NoteEntity{}, nil
		}
		return []*entity.NoteEntity{note}, nil
	}

	var noteModels []*model.NoteModel
	if err := a.db.WithContext(ctx).Where("id IN ?", ids).Find(&noteModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get notes by ids: %w", err)
	}

	return a.mapper.ToEntities(noteModels), nil
}

func (a *NoteAdapter) GetNotesByTaskID(ctx context.Context, taskID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error) {
	var noteModels []*model.NoteModel

	query := a.db.WithContext(ctx).Where("task_id = ?", taskID)

	if filter != nil {
		query = a.applyNoteFilters(query, filter)
	} else {
		query = query.Where("active_status = ?", "ACTIVE").Order("is_pinned DESC, created_at DESC")
	}

	if err := query.Find(&noteModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get notes by task id: %w", err)
	}
	return a.mapper.ToEntities(noteModels), nil
}

func (a *NoteAdapter) GetNotesByProjectID(ctx context.Context, projectID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error) {
	var noteModels []*model.NoteModel

	query := a.db.WithContext(ctx).Where("project_id = ?", projectID)

	if filter != nil {
		query = a.applyNoteFilters(query, filter)
	} else {
		query = query.Where("active_status = ?", "ACTIVE").Order("is_pinned DESC, created_at DESC")
	}

	if err := query.Find(&noteModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get notes by project id: %w", err)
	}

	return a.mapper.ToEntities(noteModels), nil
}

func (a *NoteAdapter) GetNotesByUserID(ctx context.Context, userID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error) {
	var noteModels []*model.NoteModel

	query := a.buildNoteQuery(userID, filter)

	if err := query.WithContext(ctx).Find(&noteModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get notes by user id: %w", err)
	}

	return a.mapper.ToEntities(noteModels), nil
}

func (a *NoteAdapter) CountNotesByUserID(ctx context.Context, userID int64, filter *store.NoteFilter) (int64, error) {
	var count int64
	if filter != nil {
		filter.Limit = 0
		filter.Offset = 0
	}

	query := a.buildNoteQuery(userID, filter)
	if err := query.WithContext(ctx).Model(&model.NoteModel{}).Count(&count).Error; err != nil {
		return 0, fmt.Errorf("failed to count notes: %w", err)
	}

	return count, nil
}

func (a *NoteAdapter) UpdateNote(ctx context.Context, tx *gorm.DB, note *entity.NoteEntity) error {
	db := a.getDB(tx)
	noteModel := a.mapper.ToModel(note)

	if err := db.WithContext(ctx).Save(noteModel).Error; err != nil {
		return fmt.Errorf("failed to update note: %w", err)
	}

	return nil
}

func (a *NoteAdapter) UpdateNotePin(ctx context.Context, tx *gorm.DB, noteID int64, isPinned bool) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.NoteModel{}).
		Where("id = ?", noteID).
		Update("is_pinned", isPinned).Error; err != nil {
		return fmt.Errorf("failed to update note pin: %w", err)
	}

	return nil
}

func (a *NoteAdapter) SoftDeleteNote(ctx context.Context, tx *gorm.DB, noteID int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.NoteModel{}).
		Where("id = ?", noteID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete note: %w", err)
	}

	return nil
}

func (a *NoteAdapter) SoftDeleteNotesByTaskID(ctx context.Context, tx *gorm.DB, taskID int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.NoteModel{}).
		Where("task_id = ?", taskID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete notes by task id: %w", err)
	}

	return nil
}

func (a *NoteAdapter) SoftDeleteNotesByProjectID(ctx context.Context, tx *gorm.DB, projectID int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.NoteModel{}).
		Where("project_id = ?", projectID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete notes by project id: %w", err)
	}

	return nil
}

func (a *NoteAdapter) SearchNotes(ctx context.Context, userID int64, searchQuery string, filter *store.NoteFilter) ([]*entity.NoteEntity, error) {
	var noteModels []*model.NoteModel

	searchPattern := "%" + searchQuery + "%"

	query := a.db.WithContext(ctx).
		Where("user_id = ?", userID).
		Where("content ILIKE ?", searchPattern)

	if filter != nil {
		query = a.applyNoteFilters(query, filter)
	} else {
		query = query.Where("active_status = ?", "ACTIVE").
			Order("is_pinned DESC, created_at DESC").
			Limit(20)
	}
	if err := query.Find(&noteModels).Error; err != nil {
		return nil, fmt.Errorf("failed to search notes: %w", err)
	}

	return a.mapper.ToEntities(noteModels), nil
}

func (a *NoteAdapter) GetPinnedNotes(ctx context.Context, userID int64) ([]*entity.NoteEntity, error) {
	var noteModels []*model.NoteModel

	if err := a.db.WithContext(ctx).
		Where("user_id = ? AND is_pinned = ? AND active_status = ?", userID, true, "ACTIVE").
		Order("created_at DESC").
		Find(&noteModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get pinned notes: %w", err)
	}

	return a.mapper.ToEntities(noteModels), nil
}

func (a *NoteAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}

func (a *NoteAdapter) buildNoteQuery(userID int64, filter *store.NoteFilter) *gorm.DB {
	query := a.db.Where("user_id = ?", userID)
	return a.applyNoteFilters(query, filter)
}

func (a *NoteAdapter) applyNoteFilters(query *gorm.DB, filter *store.NoteFilter) *gorm.DB {
	if filter == nil {
		filter = store.NewNoteFilter()
	}

	if filter.ActiveStatus != nil {
		query = query.Where("active_status = ?", *filter.ActiveStatus)
	} else {
		query = query.Where("active_status = ?", "ACTIVE")
	}

	if filter.TaskID != nil {
		query = query.Where("task_id = ?", *filter.TaskID)
	}

	if filter.ProjectID != nil {
		query = query.Where("project_id = ?", *filter.ProjectID)
	}

	if filter.IsPinned != nil {
		query = query.Where("is_pinned = ?", *filter.IsPinned)
	}

	if filter.HasAttachments != nil {
		if *filter.HasAttachments {
			query = query.Where("attachments IS NOT NULL AND attachments != '[]'")
		} else {
			query = query.Where("attachments IS NULL OR attachments = '[]'")
		}
	}

	if filter.CreatedFrom != nil {
		query = query.Where("created_at >= ?", *filter.CreatedFrom)
	}
	if filter.CreatedTo != nil {
		query = query.Where("created_at <= ?", *filter.CreatedTo)
	}

	if filter.SortBy != "" && filter.SortOrder != "" {
		query = query.Order(fmt.Sprintf("%s %s", filter.SortBy, filter.SortOrder))
	} else {
		query = query.Order("is_pinned DESC, created_at DESC")
	}

	if filter.Limit > 0 {
		query = query.Limit(filter.Limit)
	}
	if filter.Offset > 0 {
		query = query.Offset(filter.Offset)
	}

	return query
}
