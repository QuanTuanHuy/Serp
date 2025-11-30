/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
)

type RescheduleResult struct {
	Success         bool
	UpdatedEventIDs []int64
	Strategy        enum.RescheduleStrategy
	DurationMs      int
	Error           error
}

type IRescheduleStrategyPort interface {
	RunRipple(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*RescheduleResult, error)

	RunInsertion(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*RescheduleResult, error)

	RunFullReplan(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*RescheduleResult, error)
}
