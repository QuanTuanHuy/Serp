/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"errors"
	"fmt"
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"github.com/serp/ptm-schedule/src/infrastructure/store/mapper"
	"github.com/serp/ptm-schedule/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type ScheduleGroupStoreAdapter struct {
	db *gorm.DB
}

func (s *ScheduleGroupStoreAdapter) CreateScheduleGroup(ctx context.Context, tx *gorm.DB, scheduleGroup *entity.ScheduleGroupEntity) (*entity.ScheduleGroupEntity, error) {
	sg := mapper.ToScheduleGroupModel(scheduleGroup)
	if err := tx.WithContext(ctx).Create(sg).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleGroupEntity(sg), nil
}

func (s *ScheduleGroupStoreAdapter) DeleteScheduleGroup(ctx context.Context, tx *gorm.DB, ID int64) error {
	if err := tx.WithContext(ctx).Where("id = ?", ID).Delete(&model.ScheduleGroupModel{}).Error; err != nil {
		return err
	}
	return nil
}

func (s *ScheduleGroupStoreAdapter) GetBySchedulePlanID(ctx context.Context, schedulePlanID int64) ([]*entity.ScheduleGroupEntity, error) {
	var scheduleGroups []*model.ScheduleGroupModel
	if err := s.db.WithContext(ctx).Where("schedule_plan_id = ?", schedulePlanID).Find(&scheduleGroups).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleGroupEntities(scheduleGroups), nil
}

func (s *ScheduleGroupStoreAdapter) GetScheduleGroupByID(ctx context.Context, ID int64) (*entity.ScheduleGroupEntity, error) {
	var sg model.ScheduleGroupModel
	if err := s.db.WithContext(ctx).Where("id = ?", ID).First(&sg).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToScheduleGroupEntity(&sg), nil
}

func (s *ScheduleGroupStoreAdapter) GetScheduleGroupsToCreateTask(ctx context.Context, date time.Time, limit int32) ([]*entity.ScheduleGroupEntity, error) {
	startOfDay := time.Date(date.Year(), date.Month(), date.Day(), 0, 0, 0, 0, date.Location())
	weekDay := startOfDay.Weekday()
	var scheduleGroups []*model.ScheduleGroupModel
	if err := s.db.WithContext(ctx).
		Where("active_status = ?", enum.Active).
		Where("rotation_day < ?", startOfDay).
		Where("is_failed = ? OR is_failed IS NULL", false).
		Where("repeat LIKE ?", "%"+fmt.Sprintf("%d", int(weekDay))+"%").
		Limit(int(limit)).
		Find(&scheduleGroups).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleGroupEntities(scheduleGroups), nil
}

func (s *ScheduleGroupStoreAdapter) UpdateScheduleGroup(ctx context.Context, tx *gorm.DB, ID int64, scheduleGroup *entity.ScheduleGroupEntity) (*entity.ScheduleGroupEntity, error) {
	sg := mapper.ToScheduleGroupModel(scheduleGroup)
	if err := tx.WithContext(ctx).Where("id = ?", ID).Updates(sg).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleGroupEntity(sg), nil
}

func NewScheduleGroupStoreAdapter(db *gorm.DB) port.IScheduleGroupPort {
	return &ScheduleGroupStoreAdapter{
		db: db,
	}
}
