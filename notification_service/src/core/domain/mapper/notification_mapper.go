package mapper

import (
	"github.com/serp/notification-service/src/core/domain/dto/request"
	"github.com/serp/notification-service/src/core/domain/entity"
	"github.com/serp/notification-service/src/core/domain/enum"
)

func CreateRequestToEntity(req *request.CreateNotificationRequest) *entity.NotificationEntity {
	if req == nil {
		return nil
	}

	deliveryChannels := make([]enum.DeliveryChannel, 0, len(req.DeliveryChannels))
	for _, channel := range req.DeliveryChannels {
		deliveryChannels = append(deliveryChannels, enum.DeliveryChannel(channel))
	}

	return &entity.NotificationEntity{
		UserID:   req.UserID,
		TenantID: req.TenantID,

		Title:   req.Title,
		Message: req.Message,
		Type:    enum.NotificationType(req.Type),

		Category: enum.NotificationCategory(req.Category),
		Priority: enum.NotificationPriority(req.Priority),

		SourceService: req.SourceService,
		SourceEventID: req.SourceEventID,

		ActionURL:  req.ActionURL,
		ActionType: req.ActionType,

		EntityType: req.EntityType,
		EntityID:   req.EntityID,

		DeliveryChannels: deliveryChannels,
		ExpireAt:         req.ExpiresAt,

		Metadata: req.Metadata,
	}
}
