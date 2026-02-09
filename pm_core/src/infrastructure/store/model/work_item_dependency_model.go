/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type WorkItemDependencyModel struct {
	BaseModel

	WorkItemID      int64  `gorm:"not null;index:idx_dependency_workitem"`
	DependsOnItemID int64  `gorm:"not null"`
	DependencyType  string `gorm:"type:varchar(20);not null"`
}

func (WorkItemDependencyModel) TableName() string {
	return "work_item_dependencies"
}
