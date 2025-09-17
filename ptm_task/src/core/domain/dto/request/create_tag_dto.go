/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateTagDTO struct {
	Name   string   `json:"name" validate:"required,min=1,max=200"`
	Color  *string  `json:"color" validate:"omitempty,max=100"`
	Weight *float64 `json:"weight" validate:"omitempty,min=0"`
}
