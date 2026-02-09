/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/pm-core/src/core/domain/enum"

type ActivityLogEntity struct {
	BaseEntity

	ProjectID  int64  `json:"projectId"`
	WorkItemID *int64 `json:"workItemId,omitempty"`
	UserID     int64  `json:"userId"`

	EntityType string `json:"entityType"`
	EntityID   int64  `json:"entityId"`

	Action   string  `json:"action"`
	Field    *string `json:"field,omitempty"`
	OldValue *string `json:"oldValue,omitempty"`
	NewValue *string `json:"newValue,omitempty"`
}

func NewActivityLog(
	projectID int64,
	userID int64,
	entityType enum.ActivityEntityType,
	entityID int64,
	action enum.ActivityAction,
) *ActivityLogEntity {
	return &ActivityLogEntity{
		ProjectID:  projectID,
		UserID:     userID,
		EntityType: string(entityType),
		EntityID:   entityID,
		Action:     string(action),
	}
}

func NewWorkItemActivityLog(
	projectID int64,
	workItemID int64,
	userID int64,
	action enum.ActivityAction,
) *ActivityLogEntity {
	return &ActivityLogEntity{
		ProjectID:  projectID,
		WorkItemID: &workItemID,
		UserID:     userID,
		EntityType: string(enum.ActivityEntityWorkItem),
		EntityID:   workItemID,
		Action:     string(action),
	}
}

func NewFieldChangeLog(
	projectID int64,
	workItemID int64,
	userID int64,
	field string,
	oldValue string,
	newValue string,
) *ActivityLogEntity {
	log := NewWorkItemActivityLog(projectID, workItemID, userID, enum.ActionUpdated)
	log.Field = &field
	log.OldValue = &oldValue
	log.NewValue = &newValue
	return log
}
