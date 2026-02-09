/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type LabelEntity struct {
	BaseEntity

	ProjectID int64  `json:"projectId"`
	Name      string `json:"name"`
	Color     string `json:"color"`

	ActiveStatus string `json:"activeStatus"`
}
