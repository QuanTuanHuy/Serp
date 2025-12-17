/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type UpdatePreferenceRequest struct {
	EnableInApp *bool `json:"enableInApp,omitempty"`
	EnableEmail *bool `json:"enableEmail,omitempty"`
	EnablePush  *bool `json:"enablePush,omitempty"`

	QuietHoursEnabled  *bool `json:"quietHoursEnabled,omitempty"`
	QuietHoursStartMin *int  `json:"quietHoursStart,omitempty"`
	QuietHoursEndMin   *int  `json:"quietHoursEnd,omitempty"`
}
