/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/serp/notification-service/src/core/domain/entity"
	"github.com/serp/notification-service/src/core/domain/enum"
	client "github.com/serp/notification-service/src/core/port/client"
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
	redisPort         client.IRedisPort
	kafkaProducer     client.IKafkaProducerPort
}

func (d *DeliveryService) Deliver(ctx context.Context, notification *entity.NotificationEntity) error {
	panic("unimplemented")
}

func (d *DeliveryService) DeliverBulk(ctx context.Context, notifications []*entity.NotificationEntity) error {
	panic("unimplemented")
}

func (d *DeliveryService) DeliverEmail(ctx context.Context, notification *entity.NotificationEntity) error {
	panic("unimplemented")
}

func (d *DeliveryService) DeliverInApp(ctx context.Context, notification *entity.NotificationEntity) error {
	panic("unimplemented")
}

func (d *DeliveryService) DeliverPush(ctx context.Context, notification *entity.NotificationEntity) error {
	panic("unimplemented")
}

func (d *DeliveryService) DeliverToChannel(ctx context.Context, notification *entity.NotificationEntity, channel enum.DeliveryChannel) error {
	panic("unimplemented")
}

func (d *DeliveryService) RetryFailed(ctx context.Context, notificationID int64) error {
	panic("unimplemented")
}

func NewDeliveryService(
	preferenceService IPreferenceService,
	redisPort client.IRedisPort,
	kafkaProducer client.IKafkaProducerPort,
) IDeliveryService {
	return &DeliveryService{
		preferenceService: preferenceService,
		redisPort:         redisPort,
		kafkaProducer:     kafkaProducer,
	}
}
