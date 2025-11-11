/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type UpdateMenuDisplayDto struct {
	Name        *string `json:"name"`
	Path        *string `json:"path"`
	Icon        *string `json:"icon"`
	Order       *int    `json:"order"`
	IsVisible   *bool   `json:"isVisible"`
	Description *string `json:"description"`
}
