/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/notification-service/src/core/domain/entity"
	m "github.com/serp/notification-service/src/infrastructure/store/model"
)

type NotificationPreferenceMapper struct{}

func NewNotificationPreferenceMapper() *NotificationPreferenceMapper {
	return &NotificationPreferenceMapper{}
}

func (NotificationPreferenceMapper) ToModel(e *entity.NotificationPreferenceEntity) *m.NotificationPreferenceModel {
	if e == nil {
		return nil
	}

	return &m.NotificationPreferenceModel{
		BaseModel:          m.BaseModel{ID: e.ID},
		UserID:             e.UserID,
		TenantID:           e.TenantID,
		EnableInApp:        e.EnableInApp,
		EnableEmail:        e.EnableEmail,
		EnablePush:         e.EnablePush,
		QuietHoursEnabled:  e.QuietHoursEnabled,
		QuietHoursStartMin: e.QuietHoursStartMin,
		QuietHoursEndMin:   e.QuietHoursEndMin,
	}
}

func (NotificationPreferenceMapper) ToEntity(mo *m.NotificationPreferenceModel) *entity.NotificationPreferenceEntity {
	if mo == nil {
		return nil
	}

	return &entity.NotificationPreferenceEntity{
		BaseEntity:         entity.BaseEntity{ID: mo.ID, CreatedAt: TimeMsFromTime(mo.CreatedAt), UpdatedAt: TimeMsFromTime(mo.UpdatedAt)},
		UserID:             mo.UserID,
		TenantID:           mo.TenantID,
		EnableInApp:        mo.EnableInApp,
		EnableEmail:        mo.EnableEmail,
		EnablePush:         mo.EnablePush,
		QuietHoursEnabled:  mo.QuietHoursEnabled,
		QuietHoursStartMin: mo.QuietHoursStartMin,
		QuietHoursEndMin:   mo.QuietHoursEndMin,
	}
}
