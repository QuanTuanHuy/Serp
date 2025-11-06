/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

import "github.com/serp/ptm-task/src/core/domain/entity"

type CreateNoteRequest struct {
	TaskID    *int64 `json:"taskId,omitempty"`
	ProjectID *int64 `json:"projectId,omitempty"`

	Content string `json:"content" validate:"required"`

	Attachments []entity.NoteAttachment `json:"attachments,omitempty"`
	IsPinned    *bool                   `json:"isPinned,omitempty"`
}

type UpdateNoteRequest struct {
	Content     *string                  `json:"content,omitempty" validate:"omitempty"`
	Attachments *[]entity.NoteAttachment `json:"attachments,omitempty"`
	IsPinned    *bool                    `json:"isPinned,omitempty"`
}

type NoteFilterRequest struct {
	TaskID    *int64 `json:"taskId,omitempty"`
	ProjectID *int64 `json:"projectId,omitempty"`

	IsPinned       *bool `json:"isPinned,omitempty"`
	HasAttachments *bool `json:"hasAttachments,omitempty"`

	CreatedFrom *int64 `json:"createdFrom,omitempty"`
	CreatedTo   *int64 `json:"createdTo,omitempty"`

	SortBy    *string `json:"sortBy,omitempty" validate:"omitempty,oneof=created_at updated_at"`
	SortOrder *string `json:"sortOrder,omitempty" validate:"omitempty,oneof=ASC DESC"`

	Limit  *int `json:"limit,omitempty" validate:"omitempty,min=1,max=100"`
	Offset *int `json:"offset,omitempty" validate:"omitempty,min=0"`
}

type SearchNotesRequest struct {
	Query  string             `json:"query" validate:"required"`
	Filter *NoteFilterRequest `json:"filter,omitempty"`
}
