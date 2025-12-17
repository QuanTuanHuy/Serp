package mapper

import (
	"github.com/serp/notification-service/src/core/domain/dto/request"
	"github.com/serp/notification-service/src/core/domain/dto/response"
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

func NotificationEntityToResponse(entity *entity.NotificationEntity) *response.NotificationResponse {
	if entity == nil {
		return nil
	}

	return &response.NotificationResponse{
		ID:            entity.ID,
		Title:         entity.Title,
		Message:       entity.Message,
		Type:          string(entity.Type),
		Category:      string(entity.Category),
		Priority:      string(entity.Priority),
		SourceService: entity.SourceService,
		ActionURL:     entity.ActionURL,
		ActionType:    entity.ActionType,
		EntityType:    entity.EntityType,
		EntityID:      entity.EntityID,
		IsRead:        entity.IsRead,
		ReadAt:        entity.ReadAt,
		IsArchived:    entity.IsArchived,
		CreatedAt:     entity.CreatedAt,
		Metadata:      entity.Metadata,
	}
}

func NotificationEntitiesToResponses(entities []*entity.NotificationEntity) []*response.NotificationResponse {
	responses := make([]*response.NotificationResponse, 0, len(entities))
	for _, entity := range entities {
		responses = append(responses, NotificationEntityToResponse(entity))
	}
	return responses
}
