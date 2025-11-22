/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package response

import "github.com/serp/ptm-task/src/core/domain/entity"

type NoteResponse struct {
	ID       int64 `json:"id"`
	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	TaskID    *int64 `json:"taskId,omitempty"`
	ProjectID *int64 `json:"projectId,omitempty"`

	Content      string  `json:"content"`
	ContentPlain *string `json:"contentPlain,omitempty"`

	Attachments []entity.NoteAttachment `json:"attachments,omitempty"`

	IsPinned     bool   `json:"isPinned"`
	ActiveStatus string `json:"activeStatus"`

	CreatedAt int64 `json:"createdAt"`
	UpdatedAt int64 `json:"updatedAt"`

	TaskTitle    *string `json:"taskTitle,omitempty"`
	ProjectTitle *string `json:"projectTitle,omitempty"`
}

type NoteListResponse struct {
	Notes      []*NoteResponse `json:"notes"`
	TotalCount int64           `json:"totalCount"`
	Limit      int             `json:"limit"`
	Offset     int             `json:"offset"`
}

type NoteSearchResponse struct {
	Notes      []*NoteResponse `json:"notes"`
	TotalCount int64           `json:"totalCount"`
	Query      string          `json:"query"`
}
