/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type UpdateCommentDTO struct {
	Content      string `json:"content" validate:"required"`
	ActiveStatus string `json:"activeStatus" validate:"omitempty,oneof=ACTIVE INACTIVE"`
}
