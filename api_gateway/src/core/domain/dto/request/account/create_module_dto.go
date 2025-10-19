/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateModuleDto struct {
	Name        string  `json:"name"`
	Code        string  `json:"code"`
	Description string  `json:"description"`
	Version     *string `json:"version"`
	Icon        *string `json:"icon"`
	Url         *string `json:"url"`
	IsActive    *bool   `json:"isActive"`
}
