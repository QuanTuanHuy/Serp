/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type NotificationPreferenceModel struct {
	BaseModel

	UserID   int64 `gorm:"not null;uniqueIndex:idx_pref_user_tenant"`
	TenantID int64 `gorm:"not null;uniqueIndex:idx_pref_user_tenant"`

	EnableInApp bool `gorm:"not null;default:true"`
	EnableEmail bool `gorm:"not null;default:true"`
	EnablePush  bool `gorm:"not null;default:true"`

	QuietHoursEnabled  bool `gorm:"not null;default:false"`
	QuietHoursStartMin *int `gorm:"type:int"`
	QuietHoursEndMin   *int `gorm:"type:int"`
}

func (NotificationPreferenceModel) TableName() string {
	return "notification_preferences"
}
