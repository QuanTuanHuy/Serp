/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import (
	"errors"
	"strings"
	"time"

	"github.com/serp/pm-core/src/core/domain/enum"
)

type CommentEntity struct {
	BaseEntity

	WorkItemID      int64  `json:"workItemId"`
	AuthorID        int64  `json:"authorId"`
	Content         string `json:"content"`
	ParentCommentID *int64 `json:"parentCommentId,omitempty"`

	IsEdited   bool   `json:"isEdited"`
	EditedAtMs *int64 `json:"editedAtMs,omitempty"`

	ActiveStatus string `json:"activeStatus"`
}

func NewCommentEntity() *CommentEntity {
	return &CommentEntity{
		IsEdited:     false,
		ActiveStatus: string(enum.Active),
	}
}

func (c *CommentEntity) Validate() error {
	if strings.TrimSpace(c.Content) == "" {
		return errors.New("comment content is required")
	}
	if c.WorkItemID == 0 {
		return errors.New("comment must be associated with a work item")
	}
	return nil
}

func (c *CommentEntity) IsReply() bool {
	return c.ParentCommentID != nil
}

func (c *CommentEntity) IsAuthor(userID int64) bool {
	return c.AuthorID == userID
}

func (c *CommentEntity) MarkAsEdited() {
	nowMs := time.Now().UnixMilli()
	c.IsEdited = true
	c.EditedAtMs = &nowMs
}
