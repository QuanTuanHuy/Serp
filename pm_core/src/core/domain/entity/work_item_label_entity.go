/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type WorkItemLabelEntity struct {
	BaseEntity

	WorkItemID int64 `json:"workItemId"`
	LabelID    int64 `json:"labelId"`
}
