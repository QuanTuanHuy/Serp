/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"encoding/json"
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/infrastructure/store/model"
)

func ToScheduleTaskEntity(m *model.ScheduleTaskModel) *entity.ScheduleTaskEntity {
	if m == nil {
		return nil
	}

	var dependentTaskIDs []int64
	if m.DependentTaskIDs != "" {
		_ = json.Unmarshal([]byte(m.DependentTaskIDs), &dependentTaskIDs)
	}

	var earliestStartMs *int64
	if m.EarliestStartMs != nil {
		ms := m.EarliestStartMs.UnixMilli()
		earliestStartMs = &ms
	}

	var deadlineMs *int64
	if m.DeadlineMs != nil {
		ms := m.DeadlineMs.UnixMilli()
		deadlineMs = &ms
	}

	var preferredStartMs *int64
	if m.PreferredStartMs != nil {
		ms := m.PreferredStartMs.UnixMilli()
		preferredStartMs = &ms
	}

	var pinnedStartMs *int64
	if m.PinnedStartMs != nil {
		ms := m.PinnedStartMs.UnixMilli()
		pinnedStartMs = &ms
	}

	var pinnedEndMs *int64
	if m.PinnedEndMs != nil {
		ms := m.PinnedEndMs.UnixMilli()
		pinnedEndMs = &ms
	}

	return &entity.ScheduleTaskEntity{
		BaseEntity: entity.BaseEntity{
			ID:        m.ID,
			CreatedAt: m.CreatedAt.UnixMilli(),
			UpdatedAt: m.UpdatedAt.UnixMilli(),
		},
		UserID:              m.UserID,
		TenantID:            m.TenantID,
		SchedulePlanID:      m.SchedulePlanID,
		TaskID:              m.TaskID,
		TaskSnapshotHash:    m.TaskSnapshotHash,
		Title:               m.Title,
		DurationMin:         m.DurationMin,
		Priority:            enum.Priority(m.Priority),
		PriorityScore:       m.PriorityScore,
		Category:            m.Category,
		IsDeepWork:          m.IsDeepWork,
		EarliestStartMs:     earliestStartMs,
		DeadlineMs:          deadlineMs,
		PreferredStartMs:    preferredStartMs,
		AllowSplit:          m.AllowSplit,
		MinSplitDurationMin: m.MinSplitDurationMin,
		MaxSplitCount:       m.MaxSplitCount,
		IsPinned:            m.IsPinned,
		PinnedStartMs:       pinnedStartMs,
		PinnedEndMs:         pinnedEndMs,
		DependentTaskIDs:    dependentTaskIDs,
		BufferBeforeMin:     m.BufferBeforeMin,
		BufferAfterMin:      m.BufferAfterMin,
		ScheduleStatus:      enum.ScheduleTaskStatus(m.ScheduleStatus),
		UnscheduledReason:   m.UnscheduledReason,
	}
}

func ToScheduleTaskModel(e *entity.ScheduleTaskEntity) *model.ScheduleTaskModel {
	if e == nil {
		return nil
	}

	dependentTaskIDsJSON, err := json.Marshal(e.DependentTaskIDs)
	if err != nil {
		dependentTaskIDsJSON = []byte("[]")
	}

	var earliestStartMs *time.Time
	if e.EarliestStartMs != nil {
		t := time.UnixMilli(*e.EarliestStartMs)
		earliestStartMs = &t
	}

	var deadlineMs *time.Time
	if e.DeadlineMs != nil {
		t := time.UnixMilli(*e.DeadlineMs)
		deadlineMs = &t
	}

	var preferredStartMs *time.Time
	if e.PreferredStartMs != nil {
		t := time.UnixMilli(*e.PreferredStartMs)
		preferredStartMs = &t
	}

	var pinnedStartMs *time.Time
	if e.PinnedStartMs != nil {
		t := time.UnixMilli(*e.PinnedStartMs)
		pinnedStartMs = &t
	}

	var pinnedEndMs *time.Time
	if e.PinnedEndMs != nil {
		t := time.UnixMilli(*e.PinnedEndMs)
		pinnedEndMs = &t
	}

	return &model.ScheduleTaskModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		UserID:              e.UserID,
		TenantID:            e.TenantID,
		SchedulePlanID:      e.SchedulePlanID,
		TaskID:              e.TaskID,
		TaskSnapshotHash:    e.TaskSnapshotHash,
		Title:               e.Title,
		DurationMin:         e.DurationMin,
		Priority:            string(e.Priority),
		PriorityScore:       e.PriorityScore,
		Category:            e.Category,
		IsDeepWork:          e.IsDeepWork,
		EarliestStartMs:     earliestStartMs,
		DeadlineMs:          deadlineMs,
		PreferredStartMs:    preferredStartMs,
		AllowSplit:          e.AllowSplit,
		MinSplitDurationMin: e.MinSplitDurationMin,
		MaxSplitCount:       e.MaxSplitCount,
		IsPinned:            e.IsPinned,
		PinnedStartMs:       pinnedStartMs,
		PinnedEndMs:         pinnedEndMs,
		DependentTaskIDs:    string(dependentTaskIDsJSON),
		BufferBeforeMin:     e.BufferBeforeMin,
		BufferAfterMin:      e.BufferAfterMin,
		ScheduleStatus:      string(e.ScheduleStatus),
		UnscheduledReason:   e.UnscheduledReason,
	}
}

func ToScheduleTaskEntities(models []*model.ScheduleTaskModel) []*entity.ScheduleTaskEntity {
	var entities []*entity.ScheduleTaskEntity
	for _, m := range models {
		entities = append(entities, ToScheduleTaskEntity(m))
	}
	return entities
}

func ToScheduleTaskModels(entities []*entity.ScheduleTaskEntity) []*model.ScheduleTaskModel {
	var models []*model.ScheduleTaskModel
	for _, e := range entities {
		models = append(models, ToScheduleTaskModel(e))
	}
	return models
}
