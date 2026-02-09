/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type WorkItemAssignmentModel struct {
	BaseModel

	WorkItemID int64 `gorm:"not null;index:idx_assignment_workitem"`
	UserID     int64 `gorm:"not null;index:idx_assignment_user"`
}

func (WorkItemAssignmentModel) TableName() string {
	return "work_item_assignments"
}
