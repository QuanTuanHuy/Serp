/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"sort"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/dto/request"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/core/domain/mapper"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"gorm.io/gorm"
)

type ITimeBubblesService interface {
	CreateTimeBubbles(ctx context.Context, tx *gorm.DB, userID int64, schedule map[int][]request.CreateTimeBubblesDTO) error
	UpdateTimeBubbles(ctx context.Context, tx *gorm.DB, timeBubbleID int64, timeBubbles *entity.TimeBubblesEntity) (*entity.TimeBubblesEntity, error)
	GetTimeBubblesByUserID(ctx context.Context, userID int64) (map[int][]*entity.TimeBubblesEntity, error)
}

type TimeBubblesService struct {
	timeBubblesPort port.ITimeBubblesPort
}

func (t *TimeBubblesService) CreateTimeBubbles(ctx context.Context, tx *gorm.DB, userID int64, schedule map[int][]request.CreateTimeBubblesDTO) error {
	var allTimeBubbles []*entity.TimeBubblesEntity
	for dayOfWeek, activities := range schedule {
		if err := t.timeBubblesPort.DeleteTimeBubbles(ctx, tx, userID, dayOfWeek, enum.Draft); err != nil {
			log.Error("Failed to delete existing time bubbles: ", err)
			return err
		}

		var timeBubblesForDay []*entity.TimeBubblesEntity
		for _, activity := range activities {
			timeBubble := mapper.CreateTimeBubblesMapper(activity, userID, dayOfWeek, string(enum.Draft))
			timeBubblesForDay = append(timeBubblesForDay, timeBubble)
		}
		allTimeBubbles = append(allTimeBubbles, timeBubblesForDay...)
	}
	_, err := t.timeBubblesPort.CreateTimeBubblesBatch(ctx, tx, allTimeBubbles)
	if err != nil {
		log.Error("Failed to create time bubbles batch: ", err)
		return err
	}
	log.Info("Successfully created time bubbles batch for user ID: ", userID)
	return nil
}

func (t *TimeBubblesService) GetTimeBubblesByUserID(ctx context.Context, userID int64) (map[int][]*entity.TimeBubblesEntity, error) {
	timeBubbles, err := t.timeBubblesPort.GetAllByUserID(ctx, userID)
	if err != nil {
		log.Error("Failed to get time bubbles by user ID: ", err)
		return nil, err
	}
	result := make(map[int][]*entity.TimeBubblesEntity)
	for _, tb := range timeBubbles {
		day := tb.DayOfWeek
		result[day] = append(result[day], tb)
	}
	for day, bubbles := range result {
		sort.Slice(bubbles, func(i, j int) bool {
			return bubbles[i].StartTime < bubbles[j].StartTime
		})
		result[day] = bubbles
	}
	return result, nil
}

func (t *TimeBubblesService) UpdateTimeBubbles(ctx context.Context, tx *gorm.DB, timeBubbleID int64, timeBubbles *entity.TimeBubblesEntity) (*entity.TimeBubblesEntity, error) {
	timeBubbles, err := t.timeBubblesPort.UpdateTimeBubbles(ctx, tx, timeBubbles.ID, timeBubbles)
	if err != nil {
		log.Error("Failed to update time bubble: ", err)
		return nil, err
	}
	return timeBubbles, nil
}

func NewTimeBubblesService(timeBubblePort port.ITimeBubblesPort) ITimeBubblesService {
	return &TimeBubblesService{
		timeBubblesPort: timeBubblePort,
	}
}
