/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"encoding/json"
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/serp/notification-service/src/core/domain/entity"
	"github.com/serp/notification-service/src/core/domain/enum"
	client "github.com/serp/notification-service/src/core/port/client"
	"github.com/serp/notification-service/src/core/websocket"
)

type IDeliveryService interface {
	Deliver(ctx context.Context, notification *entity.NotificationEntity) error
	DeliverToChannel(ctx context.Context, notification *entity.NotificationEntity, channel enum.DeliveryChannel) error

	DeliverInApp(ctx context.Context, notification *entity.NotificationEntity) error
	DeliverEmail(ctx context.Context, notification *entity.NotificationEntity) error
	DeliverPush(ctx context.Context, notification *entity.NotificationEntity) error

	DeliverBulk(ctx context.Context, notifications []*entity.NotificationEntity) error

	RetryFailed(ctx context.Context, notificationID int64) error
}

type DeliveryService struct {
	preferenceService IPreferenceService
	hub               websocket.IWebSocketHub
	redisPort         client.IRedisPort
	kafkaProducer     client.IKafkaProducerPort
}

func (d *DeliveryService) Deliver(ctx context.Context, notification *entity.NotificationEntity) error {
	channels, err := d.preferenceService.GetEnabledChannels(ctx, notification.UserID)
	if err != nil {
		return fmt.Errorf("failed to get user channels: %w", err)
	}

	var deliveryErrors []error
	for _, channel := range channels {
		shouldDeliver, _ := d.preferenceService.ShouldDeliver(ctx, notification.UserID, notification, channel)
		if !shouldDeliver {
			continue
		}
		if err := d.DeliverToChannel(ctx, notification, channel); err != nil {
			deliveryErrors = append(deliveryErrors, fmt.Errorf("channel %s: %w", channel, err))
		}
	}

	if len(deliveryErrors) > 0 {
		return fmt.Errorf("delivery errors: %v", deliveryErrors)
	}
	return nil
}

func (d *DeliveryService) DeliverBulk(ctx context.Context, notifications []*entity.NotificationEntity) error {
	for _, notification := range notifications {
		if err := d.Deliver(ctx, notification); err != nil {
			return fmt.Errorf("failed to deliver notification ID %d: %w", notification.ID, err)
		}
	}
	return nil
}

func (d *DeliveryService) DeliverEmail(ctx context.Context, notification *entity.NotificationEntity) error {
	// TODO: Integrate with mailservice
	fmt.Printf("Delivering email to user %d: %s\n", notification.UserID, notification.Message)
	return nil
}

func (d *DeliveryService) DeliverInApp(ctx context.Context, notification *entity.NotificationEntity) error {
	payload, err := json.Marshal(notification)
	if err != nil {
		return fmt.Errorf("failed to marshal notification: %w", err)
	}

	wsMessage := &websocket.WSMessage{
		Type:      "NEW_NOTIFICATION",
		Payload:   payload,
		Timestamp: time.Now().UnixMilli(),
		MessageID: uuid.NewString(),
	}
	message, err := json.Marshal(wsMessage)
	if err != nil {
		return fmt.Errorf("failed to marshal WS message: %w", err)
	}

	return d.hub.SendToUser(notification.UserID, message)
}

func (d *DeliveryService) DeliverPush(ctx context.Context, notification *entity.NotificationEntity) error {
	// TODO: Integrate with push notification service
	fmt.Printf("Delivering push notification to user %d: %s\n", notification.UserID, notification.Message)
	return nil
}

func (d *DeliveryService) DeliverToChannel(ctx context.Context, notification *entity.NotificationEntity, channel enum.DeliveryChannel) error {
	switch channel {
	case enum.ChannelInApp:
		return d.DeliverInApp(ctx, notification)
	case enum.ChannelEmail:
		return d.DeliverEmail(ctx, notification)
	case enum.ChannelPush:
		return d.DeliverPush(ctx, notification)
	default:
		return fmt.Errorf("unsupported delivery channel: %s", channel)
	}
}

func (d *DeliveryService) RetryFailed(ctx context.Context, notificationID int64) error {
	// TODO: Implement retry logic for failed deliveries
	return nil
}

func NewDeliveryService(
	preferenceService IPreferenceService,
	hub websocket.IWebSocketHub,
	redisPort client.IRedisPort,
	kafkaProducer client.IKafkaProducerPort,
) IDeliveryService {
	return &DeliveryService{
		preferenceService: preferenceService,
		hub:               hub,
		redisPort:         redisPort,
		kafkaProducer:     kafkaProducer,
	}
}
