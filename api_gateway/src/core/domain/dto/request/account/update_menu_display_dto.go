/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type UpdateMenuDisplayDto struct {
	Name         *string `json:"name"`
	Icon         *string `json:"icon"`
	Path         *string `json:"path"`
	ParentId     *int64  `json:"parentId"`
	ModuleId     *int64  `json:"moduleId"`
	DisplayOrder *int    `json:"displayOrder"`
	IsActive     *bool   `json:"isActive"`
}
