/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/dto/request"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"gorm.io/gorm"
)

type ITimeBubblesService interface {
	CreateTimeBubbles(ctx context.Context, tx *gorm.DB, userID int64, schedule map[int][]request.CreateTimeBubblesDTO) error
	UpdateTimeBubbles(ctx context.Context, tx *gorm.DB, timeBubbleID int64, timeBubbles *entity.TimeBubblesEntity) (*entity.TimeBubblesEntity, error)
	GetTimeBubblesByUserID(ctx context.Context, userID int64) (map[int][]*entity.TimeBubblesEntity, error)
}
