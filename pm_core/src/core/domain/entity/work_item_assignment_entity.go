/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type WorkItemAssignmentEntity struct {
	BaseEntity

	WorkItemID int64 `json:"workItemId"`
	UserID     int64 `json:"userId"`
}
