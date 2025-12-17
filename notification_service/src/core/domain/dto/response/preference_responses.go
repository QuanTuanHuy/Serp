/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package response

type PreferenceResponse struct {
	EnableInApp bool `json:"enableInApp"`
	EnableEmail bool `json:"enableEmail"`
	EnablePush  bool `json:"enablePush"`

	QuietHours QuietHoursDTO `json:"quietHours"`
}

type QuietHoursDTO struct {
	Enabled bool    `json:"enabled"`
	Start   *string `json:"start,omitempty"`
	End     *string `json:"end,omitempty"`
}
