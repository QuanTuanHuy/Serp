/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateUserTagRequest struct {
	Name   string   `json:"name"`
	Color  *string  `json:"color"`
	Weight *float64 `json:"weight"`
}
