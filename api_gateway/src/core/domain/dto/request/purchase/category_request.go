/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateCategoryRequest struct {
	Name string `json:"name"`
}

type UpdateCategoryRequest struct {
	Name string `json:"name"`
}

type GetCategoryParams struct {
	Page          *int    `form:"page"`
	Size          *int    `form:"size"`
	SortBy        *string `form:"sortBy"`
	SortDirection *string `form:"sortDirection"`
	Query         *string `form:"query"`
	StatusId      *string `form:"statusId"`
}
