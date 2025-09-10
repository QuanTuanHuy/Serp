/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type GetProjectsRequest struct {
	Page         int     `form:"page"`
	PageSize     int     `form:"pageSize"`
	Status       *string `form:"status"`
	ActiveStatus *string `form:"activeStatus"`
}
