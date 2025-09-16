/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/ptm-task/src/core/domain/enum"

type UserTagEntity struct {
	BaseEntity
	Name         string            `json:"name"`
	Color        string            `json:"color"`
	Weight       float64           `json:"weight"`
	ActiveStatus enum.ActiveStatus `json:"activeStatus"`
	UserID       int64             `json:"userId"`
}
