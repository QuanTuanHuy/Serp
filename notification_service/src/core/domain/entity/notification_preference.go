/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import (
	"errors"
	"time"

	"github.com/serp/notification-service/src/core/domain/constant"
	"github.com/serp/notification-service/src/core/domain/enum"
)

type NotificationPreferenceEntity struct {
	BaseEntity

	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	EnableInApp bool `json:"enableInApp"`
	EnableEmail bool `json:"enableEmail"`
	EnablePush  bool `json:"enablePush"`

	QuietHoursEnabled  bool `json:"quietHoursEnabled"`
	QuietHoursStartMin *int `json:"quietHoursStart,omitempty"`
	QuietHoursEndMin   *int `json:"quietHoursEnd,omitempty"`
}

func NewNotificationPreference(userID int64) *NotificationPreferenceEntity {
	return &NotificationPreferenceEntity{
		UserID: userID,

		EnableInApp: true,
		EnableEmail: false,
		EnablePush:  true,

		QuietHoursEnabled: false,
	}
}

func (p *NotificationPreferenceEntity) GetEnabledChannels() []enum.DeliveryChannel {
	var channels []enum.DeliveryChannel
	if p.EnableInApp {
		channels = append(channels, enum.ChannelInApp)
	}
	if p.EnableEmail {
		channels = append(channels, enum.ChannelEmail)
	}
	if p.EnablePush {
		channels = append(channels, enum.ChannelPush)
	}
	return channels
}

func (p *NotificationPreferenceEntity) IsChannelEnabled(channel enum.DeliveryChannel) bool {
	switch channel {
	case enum.ChannelInApp:
		return p.EnableInApp
	case enum.ChannelEmail:
		return p.EnableEmail
	case enum.ChannelPush:
		return p.EnablePush
	default:
		return false
	}
}

func (p *NotificationPreferenceEntity) IsQuietHours(checkTimeMin int) bool {
	if !p.QuietHoursEnabled || p.QuietHoursStartMin == nil || p.QuietHoursEndMin == nil {
		return false
	}
	start := *p.QuietHoursStartMin
	end := *p.QuietHoursEndMin
	if start < end {
		return checkTimeMin >= start && checkTimeMin < end
	}
	return checkTimeMin >= start || checkTimeMin < end
}

func (p *NotificationPreferenceEntity) IsQuietHoursNow() bool {
	currentTimeMin := func() int {
		now := time.Now()
		return now.Hour()*60 + now.Minute()
	}()
	return p.IsQuietHours(currentTimeMin)
}

func (p *NotificationPreferenceEntity) Validate() error {
	if p.QuietHoursStartMin != nil {
		if *p.QuietHoursStartMin < 0 || *p.QuietHoursStartMin >= 1440 {
			return errors.New(constant.ErrInvalidQuietHours)
		}
	}
	if p.QuietHoursEndMin != nil {
		if *p.QuietHoursEndMin < 0 || *p.QuietHoursEndMin >= 1440 {
			return errors.New(constant.ErrInvalidQuietHours)
		}
	}
	return nil
}
