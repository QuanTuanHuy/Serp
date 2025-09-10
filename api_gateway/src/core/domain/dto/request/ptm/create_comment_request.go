/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateCommentRequest struct {
	Content string `json:"content"`
	TaskID  int64  `json:"taskId"`
}
