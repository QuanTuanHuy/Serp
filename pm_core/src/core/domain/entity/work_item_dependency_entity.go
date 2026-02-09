/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type WorkItemDependencyEntity struct {
	BaseEntity

	WorkItemID      int64  `json:"workItemId"`
	DependsOnItemID int64  `json:"dependsOnItemId"`
	DependencyType  string `json:"dependencyType"`
}
