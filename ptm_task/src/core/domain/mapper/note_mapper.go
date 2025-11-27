/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/dto/response"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/port/store"
)

type NoteMapper struct{}

func NewNoteMapper() *NoteMapper {
	return &NoteMapper{}
}

func (m *NoteMapper) CreateRequestToEntity(req *request.CreateNoteRequest, userID, tenantID int64) *entity.NoteEntity {
	note := entity.NewNoteEntity()
	note.UserID = userID
	note.TenantID = tenantID
	note.TaskID = req.TaskID
	note.ProjectID = req.ProjectID
	note.Content = req.Content
	note.Attachments = req.Attachments

	if req.IsPinned != nil {
		note.IsPinned = *req.IsPinned
	}

	return note
}

func (m *NoteMapper) UpdateRequestToEntity(req *request.UpdateNoteRequest, existing *entity.NoteEntity) *entity.NoteEntity {
	if req.Content != nil {
		existing.Content = *req.Content
	}
	if req.Attachments != nil {
		existing.Attachments = *req.Attachments
	}
	if req.IsPinned != nil {
		existing.IsPinned = *req.IsPinned
	}

	return existing
}

func (m *NoteMapper) EntityToResponse(note *entity.NoteEntity) *response.NoteResponse {
	return &response.NoteResponse{
		ID:           note.ID,
		UserID:       note.UserID,
		TenantID:     note.TenantID,
		TaskID:       note.TaskID,
		ProjectID:    note.ProjectID,
		Content:      note.Content,
		ContentPlain: note.ContentPlain,
		Attachments:  note.Attachments,
		IsPinned:     note.IsPinned,
		ActiveStatus: note.ActiveStatus,
		CreatedAt:    note.CreatedAt,
		UpdatedAt:    note.UpdatedAt,
	}
}

func (m *NoteMapper) EntitiesToResponses(notes []*entity.NoteEntity) []*response.NoteResponse {
	responses := make([]*response.NoteResponse, 0, len(notes))
	for _, note := range notes {
		responses = append(responses, m.EntityToResponse(note))
	}
	return responses
}

func (m *NoteMapper) FilterMapper(req *request.NoteFilterRequest) *store.NoteFilter {
	return &store.NoteFilter{
		TaskID:      req.TaskID,
		ProjectID:   req.ProjectID,
		CreatedFrom: req.CreatedFrom,
		CreatedTo:   req.CreatedTo,
		Limit:       req.PageSize,
		Offset:      req.Page * req.PageSize,
		SortBy:      req.SortBy,
		SortOrder:   req.SortOrder,
	}
}
