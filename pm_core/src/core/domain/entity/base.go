/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type BaseEntity struct {
	ID        int64 `json:"id"`
	CreatedAt int64 `json:"createdAt"`
	CreatedBy int64 `json:"createdBy"`
	UpdatedAt int64 `json:"updatedAt"`
	UpdatedBy int64 `json:"updatedBy"`
}
