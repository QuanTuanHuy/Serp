/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type NotificationPreferenceEntity struct {
	BaseEntity

	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	EnableInApp bool `json:"enableInApp"`
	EnableEmail bool `json:"enableEmail"`
	EnablePush  bool `json:"enablePush"`

	CategorySettings map[string]CategoryPreference `json:"categorySettings"`

	QuietHoursEnabled  bool `json:"quietHoursEnabled"`
	QuietHoursStartMin *int `json:"quietHoursStart,omitempty"`
	QuietHoursEndMin   *int `json:"quietHoursEnd,omitempty"`
}

type CategoryPreference struct {
	Enabled  bool     `json:"enabled"`
	Channels []string `json:"channels"` // ["in_app", "email"]
}
