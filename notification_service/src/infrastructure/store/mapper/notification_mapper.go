/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"encoding/json"

	"github.com/serp/notification-service/src/core/domain/entity"
	"github.com/serp/notification-service/src/core/domain/enum"
	m "github.com/serp/notification-service/src/infrastructure/store/model"
	"gorm.io/datatypes"
)

type NotificationMapper struct{}

func NewNotificationMapper() *NotificationMapper {
	return &NotificationMapper{}
}

func (NotificationMapper) ToModel(e *entity.NotificationEntity) *m.NotificationModel {
	if e == nil {
		return nil
	}

	deliveryChannels, _ := json.Marshal([]string{})
	if len(e.DeliveryChannels) > 0 {
		chs := make([]string, len(e.DeliveryChannels))
		for i, c := range e.DeliveryChannels {
			chs[i] = c.String()
		}
		deliveryChannels, _ = json.Marshal(chs)
	}

	var metadata datatypes.JSONMap
	if e.Metadata != nil {
		metadata = datatypes.JSONMap(e.Metadata)
	}

	return &m.NotificationModel{
		BaseModel:        m.BaseModel{ID: e.ID},
		UserID:           e.UserID,
		TenantID:         e.TenantID,
		Title:            e.Title,
		Message:          e.Message,
		Type:             string(e.Type),
		Category:         string(e.Category),
		Priority:         string(e.Priority),
		SourceService:    e.SourceService,
		SourceEventID:    e.SourceEventID,
		ActionURL:        e.ActionURL,
		ActionType:       e.ActionType,
		EntityType:       e.EntityType,
		EntityID:         e.EntityID,
		IsRead:           e.IsRead,
		ReadAt:           TimePtrFromMs(e.ReadAt),
		IsArchived:       e.IsArchived,
		Status:           string(e.Status),
		DeliveryChannels: datatypes.JSON(deliveryChannels),
		DeliveryAt:       TimePtrFromMs(e.DeliveryAt),
		ExpireAt:         TimePtrFromMs(e.ExpireAt),
		Metadata:         metadata,
	}
}

func (NotificationMapper) ToEntity(mo *m.NotificationModel) *entity.NotificationEntity {
	if mo == nil {
		return nil
	}

	var deliveryChannels []enum.DeliveryChannel
	if len(mo.DeliveryChannels) > 0 {
		var raw []string
		_ = json.Unmarshal(mo.DeliveryChannels, &raw)
		deliveryChannels = make([]enum.DeliveryChannel, 0, len(raw))
		for _, c := range raw {
			deliveryChannels = append(deliveryChannels, enum.DeliveryChannel(c))
		}
	}

	var metadata map[string]any
	if mo.Metadata != nil {
		metadata = map[string]any(mo.Metadata)
	}

	return &entity.NotificationEntity{
		BaseEntity:       entity.BaseEntity{ID: mo.ID, CreatedAt: TimeMsFromTime(mo.CreatedAt), UpdatedAt: TimeMsFromTime(mo.UpdatedAt)},
		UserID:           mo.UserID,
		TenantID:         mo.TenantID,
		Title:            mo.Title,
		Message:          mo.Message,
		Type:             enum.NotificationType(mo.Type),
		Category:         enum.NotificationCategory(mo.Category),
		Priority:         enum.NotificationPriority(mo.Priority),
		SourceService:    mo.SourceService,
		SourceEventID:    mo.SourceEventID,
		ActionURL:        mo.ActionURL,
		ActionType:       mo.ActionType,
		EntityType:       mo.EntityType,
		EntityID:         mo.EntityID,
		IsRead:           mo.IsRead,
		ReadAt:           MsPtrFromTime(mo.ReadAt),
		IsArchived:       mo.IsArchived,
		Status:           enum.NotificationStatus(mo.Status),
		DeliveryChannels: deliveryChannels,
		DeliveryAt:       MsPtrFromTime(mo.DeliveryAt),
		ExpireAt:         MsPtrFromTime(mo.ExpireAt),
		Metadata:         metadata,
	}
}

func (n NotificationMapper) ToModels(list []*entity.NotificationEntity) []*m.NotificationModel {
	out := make([]*m.NotificationModel, 0, len(list))
	for _, it := range list {
		out = append(out, n.ToModel(it))
	}
	return out
}

func (n NotificationMapper) ToEntities(list []*m.NotificationModel) []*entity.NotificationEntity {
	out := make([]*entity.NotificationEntity, 0, len(list))
	for _, it := range list {
		out = append(out, n.ToEntity(it))
	}
	return out
}
