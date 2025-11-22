/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"encoding/json"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/datatypes"
)

type NoteMapper struct{}

func NewNoteMapper() *NoteMapper {
	return &NoteMapper{}
}

func (m *NoteMapper) ToEntity(noteModel *model.NoteModel) *entity.NoteEntity {
	if noteModel == nil {
		return nil
	}

	note := &entity.NoteEntity{
		BaseEntity: entity.BaseEntity{
			ID:        noteModel.ID,
			CreatedAt: noteModel.CreatedAt.UnixMilli(),
			UpdatedAt: noteModel.UpdatedAt.UnixMilli(),
		},
		UserID:   noteModel.UserID,
		TenantID: noteModel.TenantID,

		TaskID:    noteModel.TaskID,
		ProjectID: noteModel.ProjectID,

		Content:      noteModel.Content,
		ContentPlain: noteModel.ContentPlain,

		IsPinned:     noteModel.IsPinned,
		ActiveStatus: noteModel.ActiveStatus,
	}

	note.Attachments = m.jsonToAttachments(noteModel.Attachments)

	return note
}

func (m *NoteMapper) ToModel(note *entity.NoteEntity) *model.NoteModel {
	if note == nil {
		return nil
	}

	noteModel := &model.NoteModel{
		BaseModel: model.BaseModel{
			ID: note.ID,
		},
		UserID:   note.UserID,
		TenantID: note.TenantID,

		TaskID:    note.TaskID,
		ProjectID: note.ProjectID,

		Content:      note.Content,
		ContentPlain: note.ContentPlain,

		IsPinned:     note.IsPinned,
		ActiveStatus: note.ActiveStatus,
	}

	noteModel.Attachments = m.attachmentsToJSON(note.Attachments)

	return noteModel
}

func (m *NoteMapper) ToEntities(models []*model.NoteModel) []*entity.NoteEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.NoteEntity, 0, len(models))
	for _, model := range models {
		if entity := m.ToEntity(model); entity != nil {
			entities = append(entities, entity)
		}
	}

	return entities
}

func (m *NoteMapper) ToModels(entities []*entity.NoteEntity) []*model.NoteModel {
	if entities == nil {
		return nil
	}

	models := make([]*model.NoteModel, 0, len(entities))
	for _, entity := range entities {
		if model := m.ToModel(entity); model != nil {
			models = append(models, model)
		}
	}

	return models
}

func (m *NoteMapper) attachmentsToJSON(attachments []entity.NoteAttachment) datatypes.JSON {
	if attachments == nil {
		return datatypes.JSON([]byte("[]"))
	}

	bytes, err := json.Marshal(attachments)
	if err != nil {
		return datatypes.JSON([]byte("[]"))
	}

	return datatypes.JSON(bytes)
}

func (m *NoteMapper) jsonToAttachments(jsonData datatypes.JSON) []entity.NoteAttachment {
	if len(jsonData) == 0 {
		return []entity.NoteAttachment{}
	}

	var attachments []entity.NoteAttachment
	if err := json.Unmarshal(jsonData, &attachments); err != nil {
		return []entity.NoteAttachment{}
	}

	return attachments
}
