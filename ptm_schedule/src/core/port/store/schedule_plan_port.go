package port

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"gorm.io/gorm"
)

type ISchedulePlanPort interface {
	CreatePlan(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error)
	UpdatePlan(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error)
	UpdatePlanStatus(ctx context.Context, tx *gorm.DB, planID int64, status string) error
	DeletePlan(ctx context.Context, tx *gorm.DB, ID int64) error

	GetPlanByID(ctx context.Context, ID int64) (*entity.SchedulePlanEntity, error)
	GetActivePlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error)
	ListPlansByUserID(ctx context.Context, userID int64, filter *PlanFilter) ([]*entity.SchedulePlanEntity, error)
}

type PlanFilter struct {
	Statuses   []string
	Algorithms []string

	DateMs *int64

	SortBy    string
	SortOrder string

	Limit  int
	Offset int
}

func NewPlanFilter() *PlanFilter {
	return &PlanFilter{
		Statuses:   []string{},
		Algorithms: []string{},
		SortBy:     "id",
		SortOrder:  "DESC",
		Limit:      50,
		Offset:     0,
	}
}
