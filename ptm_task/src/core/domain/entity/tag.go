/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/ptm-task/src/core/domain/enum"

type TagEntity struct {
	BaseEntity
	UserID       int64             `json:"userId"`
	Name         string            `json:"name"`
	Color        string            `json:"color"`
	Weight       *float64          `json:"weight,omitempty"`
	ActiveStatus enum.ActiveStatus `json:"activeStatus"`
}
