/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type BoardEntity struct {
	BaseEntity

	ProjectID int64  `json:"projectId"`
	Name      string `json:"name"`

	ActiveStatus string `json:"activeStatus"`
}
