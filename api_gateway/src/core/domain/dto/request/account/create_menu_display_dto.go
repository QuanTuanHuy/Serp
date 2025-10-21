/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateMenuDisplayDto struct {
	Name     string  `json:"name"`
	Path     string  `json:"path"`
	Icon     *string `json:"icon"`
	Order    *int    `json:"order"`
	ParentId *int64  `json:"parentId"`
	ModuleId *int64  `json:"moduleId"`
}
