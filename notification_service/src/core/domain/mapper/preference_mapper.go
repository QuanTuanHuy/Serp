/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"fmt"

	"github.com/serp/notification-service/src/core/domain/dto/response"
	"github.com/serp/notification-service/src/core/domain/entity"
	"github.com/serp/notification-service/src/kernel/utils"
)

func PreferenceEntityToResponse(pref *entity.NotificationPreferenceEntity) *response.PreferenceResponse {
	if pref == nil {
		return nil
	}

	quietHours := response.QuietHoursDTO{
		Enabled: pref.QuietHoursEnabled,
	}
	if pref.QuietHoursEnabled {
		if pref.QuietHoursStartMin != nil {
			quietHours.Start = utils.StringPtr(minutesToTimeString(*pref.QuietHoursStartMin))
		}
		if pref.QuietHoursEndMin != nil {
			quietHours.End = utils.StringPtr(minutesToTimeString(*pref.QuietHoursEndMin))
		}
	}

	return &response.PreferenceResponse{
		EnableInApp: pref.EnableInApp,
		EnableEmail: pref.EnableEmail,
		EnablePush:  pref.EnablePush,
		QuietHours:  quietHours,
	}
}

func minutesToTimeString(minutes int) string {
	h := minutes / 60
	m := minutes % 60
	return fmt.Sprintf("%02d:%02d", h, m)
}
