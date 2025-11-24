/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/ptm-task/src/core/domain/enum"

type CommentEntity struct {
	BaseEntity
	Content      string            `json:"content"`
	TaskID       int64             `json:"taskId"`
	ActiveStatus enum.ActiveStatus `json:"activeStatus"`
}
