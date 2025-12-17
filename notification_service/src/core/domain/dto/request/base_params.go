/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type BaseParams struct {
	Page      int    `form:"page,default=0" validate:"min=0"`
	PageSize  int    `form:"pageSize,default=10" validate:"min=1,max=100"`
	SortBy    string `form:"sortBy,default=id"`
	SortOrder string `form:"sortOrder,default=DESC"`
}
