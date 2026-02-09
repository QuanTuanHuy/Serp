/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type WorkItemLabelModel struct {
	BaseModel

	WorkItemID int64 `gorm:"not null;index:idx_wil_workitem"`
	LabelID    int64 `gorm:"not null;index:idx_wil_label"`
}

func (WorkItemLabelModel) TableName() string {
	return "work_item_labels"
}
