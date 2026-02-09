/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type BoardColumnEntity struct {
	BaseEntity

	BoardID       int64   `json:"boardId"`
	Name          string  `json:"name"`
	Position      int     `json:"position"`
	StatusMapping *string `json:"statusMapping,omitempty"`

	ActiveStatus string `json:"activeStatus"`
}
