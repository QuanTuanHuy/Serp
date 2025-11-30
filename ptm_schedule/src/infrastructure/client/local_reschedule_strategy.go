/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/core/port"
)

type LocalRescheduleStrategy struct{}

func NewLocalRescheduleStrategy() port.IRescheduleStrategyPort {
	return &LocalRescheduleStrategy{}
}

func (s *LocalRescheduleStrategy) RunRipple(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*port.RescheduleResult, error) {
	// TODO: Implement ripple effect algorithm
	// For now, return success without changes
	return &port.RescheduleResult{
		Success:         true,
		UpdatedEventIDs: []int64{},
		Strategy:        enum.StrategyRipple,
		DurationMs:      0,
	}, nil
}

func (s *LocalRescheduleStrategy) RunInsertion(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*port.RescheduleResult, error) {
	// TODO: Implement insertion heuristic algorithm
	return &port.RescheduleResult{
		Success:         true,
		UpdatedEventIDs: []int64{},
		Strategy:        enum.StrategyInsertion,
		DurationMs:      0,
	}, nil
}

func (s *LocalRescheduleStrategy) RunFullReplan(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*port.RescheduleResult, error) {
	// TODO: Implement full replan or delegate to ptm_optimization via Kafka
	return &port.RescheduleResult{
		Success:         true,
		UpdatedEventIDs: []int64{},
		Strategy:        enum.StrategyFullReplan,
		DurationMs:      0,
	}, nil
}
