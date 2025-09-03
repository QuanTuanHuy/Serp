/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type GetProjectParams struct {
	UserID       *int64
	Limit        *int64
	Offset       *int64
	Status       *string `form:"status"`
	ActiveStatus *string `form:"activeStatus"`
}
