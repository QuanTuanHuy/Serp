/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type GetMenuDisplayParams struct {
	Page          *int    `json:"page"`
	PageSize      *int    `json:"pageSize"`
	SortBy        *string `json:"sortBy"`
	SortDirection *string `json:"sortDirection"`
	ModuleId      *int64  `json:"moduleId"`
	Search        *string `json:"search"`
}
