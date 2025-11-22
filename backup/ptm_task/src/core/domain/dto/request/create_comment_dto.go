/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateCommentDTO struct {
	Content string `json:"content" validate:"required"`
	TaskID  int64  `json:"taskId" validate:"required"`
}
