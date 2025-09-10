/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type UpdateProjectRequest struct {
	Name         *string `json:"name"`
	Description  *string `json:"description"`
	Color        *string `json:"color"`
	Status       *string `json:"status"`
	ActiveStatus *string `json:"activeStatus"`
}
