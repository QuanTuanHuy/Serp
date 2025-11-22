/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type NoteAttachment struct {
	Name string `json:"name"`
	URL  string `json:"url"`
	Size int64  `json:"size"`
	Type string `json:"type"`
}

type NoteEntity struct {
	BaseEntity

	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	TaskID    *int64 `json:"taskId,omitempty"`
	ProjectID *int64 `json:"projectId,omitempty"`

	Content      string  `json:"content"`
	ContentPlain *string `json:"contentPlain,omitempty"`

	Attachments []NoteAttachment `json:"attachments,omitempty"`

	IsPinned bool `json:"isPinned"`

	ActiveStatus string `json:"activeStatus"`
}

func NewNoteEntity() *NoteEntity {
	return &NoteEntity{
		ActiveStatus: "ACTIVE",
		IsPinned:     false,
		Attachments:  []NoteAttachment{},
	}
}

func (n *NoteEntity) IsAttachedToTask() bool {
	return n.TaskID != nil
}

func (n *NoteEntity) IsAttachedToProject() bool {
	return n.ProjectID != nil
}

func (n *NoteEntity) HasAttachments() bool {
	return len(n.Attachments) > 0
}

func (n *NoteEntity) GetTotalAttachmentSize() int64 {
	var total int64
	for _, attachment := range n.Attachments {
		total += attachment.Size
	}
	return total
}

func (n *NoteEntity) AddAttachment(attachment NoteAttachment) {
	n.Attachments = append(n.Attachments, attachment)
}

func (n *NoteEntity) RemoveAttachment(url string) {
	filtered := make([]NoteAttachment, 0)
	for _, attachment := range n.Attachments {
		if attachment.URL != url {
			filtered = append(filtered, attachment)
		}
	}
	n.Attachments = filtered
}
