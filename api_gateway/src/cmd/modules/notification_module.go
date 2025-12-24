/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package modules

import (
	"github.com/serp/api-gateway/src/ui/controller/notification"
	"go.uber.org/fx"
)

func NotificationModule() fx.Option {
	return fx.Module("notification",
		// Provide controller
		fx.Provide(notification.NewNotificationProxyController),
	)
}
