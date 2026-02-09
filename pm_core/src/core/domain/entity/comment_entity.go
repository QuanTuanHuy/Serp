/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type CommentEntity struct {
	BaseEntity

	WorkItemID int64  `json:"workItemId"`
	AuthorID   int64  `json:"authorId"`
	Content    string `json:"content"`

	ActiveStatus string `json:"activeStatus"`
}
