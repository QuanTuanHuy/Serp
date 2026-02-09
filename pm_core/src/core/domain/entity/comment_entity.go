/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

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
