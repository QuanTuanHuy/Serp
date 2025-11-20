package service

import (
	"context"
	"errors"

	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

type IActivityEventService interface {
	CreateActivityEvent(ctx context.Context, tx *gorm.DB, activity *entity.ActivityEventEntity) error

	GetActivityEventByID(ctx context.Context, id int64) (*entity.ActivityEventEntity, error)
	GetActivityEventsByUserID(ctx context.Context, userID int64, filter *store.ActivityEventFilter) ([]*entity.ActivityEventEntity, error)

	ValidateActivityEventData(activity *entity.ActivityEventEntity) error
}

type ActivityEventService struct {
	activityEventPort store.IActivityEventStorePort
}

func (a *ActivityEventService) ValidateActivityEventData(activity *entity.ActivityEventEntity) error {
	if activity.UserID == 0 {
		return errors.New(constant.UserIDInvalid)
	}
	if !enum.ActivityEventType(activity.EventType).IsValid() {
		return errors.New(constant.ActivityEventTypeNotValid)
	}
	return nil
}

func (a *ActivityEventService) CreateActivityEvent(ctx context.Context, tx *gorm.DB, activity *entity.ActivityEventEntity) error {
	if err := a.ValidateActivityEventData(activity); err != nil {
		return err
	}
	return a.activityEventPort.CreateActivityEvent(ctx, tx, activity)
}

func (a *ActivityEventService) GetActivityEventByID(ctx context.Context, id int64) (*entity.ActivityEventEntity, error) {
	activity, err := a.activityEventPort.GetActivityEventByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if activity == nil {
		return nil, errors.New(constant.ActivityEventNotFound)
	}
	return activity, nil
}

func (a *ActivityEventService) GetActivityEventsByUserID(ctx context.Context, userID int64, filter *store.ActivityEventFilter) ([]*entity.ActivityEventEntity, error) {
	return a.activityEventPort.GetActivityEventsByUserID(ctx, userID, filter)
}

func NewActivityEventService(
	activityEventPort store.IActivityEventStorePort,
) IActivityEventService {
	return &ActivityEventService{
		activityEventPort: activityEventPort,
	}
}
