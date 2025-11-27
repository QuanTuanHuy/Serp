/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

import "github.com/serp/ptm-task/src/core/domain/entity"

type CreateNoteRequest struct {
	TaskID    *int64 `json:"taskId,omitempty" validate:"omitempty,min=1"`
	ProjectID *int64 `json:"projectId,omitempty" validate:"omitempty,min=1"`

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
	BaseFilterRequest
	TaskID    *int64 `form:"taskId,omitempty"`
	ProjectID *int64 `form:"projectId,omitempty"`

	IsPinned       *bool `form:"isPinned,omitempty"`
	HasAttachments *bool `form:"hasAttachments,omitempty"`

	CreatedFrom *int64 `form:"createdFrom,omitempty"`
	CreatedTo   *int64 `form:"createdTo,omitempty"`
}

type SearchNotesRequest struct {
	Query string `form:"query" validate:"required"`
	NoteFilterRequest
}
