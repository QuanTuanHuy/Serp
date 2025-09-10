/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateGroupTaskRequest struct {
	ProjectID   int64     `json:"projectId"`
	Title       string    `json:"title"`
	Description *string   `json:"description"`
	Priority    []*string `json:"priority"`
	Status      *string   `json:"status"`
}
