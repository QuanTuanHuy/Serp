/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type TaskDependencyGraphModel struct {
	BaseModel

	UserID          int64 `gorm:"not null;index:idx_task_dependency_user"`
	TaskID          int64 `gorm:"not null;index:idx_task_dependency_task"`
	DependsOnTaskID int64 `gorm:"not null;index:idx_task_dependency_depends_on"`

	IsValid         bool    `gorm:"not null;default:true"`
	ValidationError *string `gorm:"type:text"`

	DependencyDepth int `gorm:"not null;default:0;index:idx_task_dependency_depth"`
}

func (TaskDependencyGraphModel) TableName() string {
	return "task_dependency_graphs"
}
