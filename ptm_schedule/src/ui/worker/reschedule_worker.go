/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package worker

import (
	"context"
	"sync"
	"time"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	storePort "github.com/serp/ptm-schedule/src/core/port/store"
	"github.com/serp/ptm-schedule/src/core/service"
	"gorm.io/gorm"
)

type RescheduleWorker struct {
	queuePort       storePort.IRescheduleQueuePort
	strategyService service.IRescheduleStrategyService
	txService       service.ITransactionService

	stopCh     chan struct{}
	wg         sync.WaitGroup
	processing sync.Map
}

func NewRescheduleWorker(
	queuePort storePort.IRescheduleQueuePort,
	strategyService service.IRescheduleStrategyService,
	txService service.ITransactionService,
) *RescheduleWorker {
	return &RescheduleWorker{
		queuePort:       queuePort,
		strategyService: strategyService,
		txService:       txService,
		stopCh:          make(chan struct{}),
	}
}

func (w *RescheduleWorker) Start() {
	w.wg.Add(1)
	go w.run()
	log.Info("RescheduleWorker started")
}

func (w *RescheduleWorker) Stop() {
	close(w.stopCh)
	w.wg.Wait()
	log.Info("RescheduleWorker stopped")
}

func (w *RescheduleWorker) run() {
	defer w.wg.Done()
	ticker := time.NewTicker(constant.WorkerPollInterval)
	defer ticker.Stop()

	for {
		select {
		case <-w.stopCh:
			return
		case <-ticker.C:
			w.processPendingBatches()
		}
	}
}

func (w *RescheduleWorker) processPendingBatches() {
	ctx := context.Background()

	planIDs, err := w.queuePort.GetDirtyPlanIDs(ctx, constant.MaxPlansPerPoll)
	if err != nil {
		log.Errorf("GetDirtyPlanIDs error: %v", err)
		return
	}

	for _, planID := range planIDs {
		if _, exists := w.processing.LoadOrStore(planID, true); exists {
			continue
		}

		w.wg.Add(1)
		go func(pid int64) {
			defer w.wg.Done()
			defer w.processing.Delete(pid)
			w.processSinglePlan(ctx, pid)
		}(planID)
	}
}

func (w *RescheduleWorker) processSinglePlan(ctx context.Context, planID int64) {
	startTime := time.Now()

	err := w.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		items, err := w.queuePort.FetchAndLockBatch(ctx, tx, planID)
		if err != nil {
			return err
		}
		if len(items) == 0 {
			return nil
		}

		batch := &entity.RescheduleBatch{
			PlanID: planID,
			UserID: items[0].UserID,
			Items:  items,
		}
		batch.Strategy = batch.DetermineStrategy()

		ids := batch.ItemIDs()
		if err := w.queuePort.MarkProcessing(ctx, tx, ids); err != nil {
			return err
		}

		result, err := w.executeStrategy(ctx, batch)
		if err != nil {
			w.handleFailure(ctx, tx, ids, err)
			return nil
		}

		result.DurationMs = int(time.Since(startTime).Milliseconds())
		return w.queuePort.UpdateBatchStatus(ctx, tx, ids, string(enum.QueueCompleted), nil)
	})

	if err != nil {
		log.Errorf("processSinglePlan error for plan %d: %v", planID, err)
	}
}

func (w *RescheduleWorker) executeStrategy(ctx context.Context, batch *entity.RescheduleBatch) (*service.RescheduleResult, error) {
	switch batch.Strategy {
	case enum.StrategyRipple:
		return w.strategyService.RunRipple(ctx, batch.PlanID, batch)
	case enum.StrategyInsertion:
		return w.strategyService.RunInsertion(ctx, batch.PlanID, batch)
	case enum.StrategyFullReplan:
		return w.strategyService.RunFullReplan(ctx, batch.PlanID, batch)
	default:
		return w.strategyService.RunRipple(ctx, batch.PlanID, batch)
	}
}

func (w *RescheduleWorker) handleFailure(ctx context.Context, tx *gorm.DB, ids []int64, err error) {
	errMsg := err.Error()
	_ = w.queuePort.IncrementRetryCount(ctx, tx, ids)
	_ = w.queuePort.UpdateBatchStatus(ctx, tx, ids, string(enum.QueueFailed), &errMsg)
	log.Errorf("Reschedule failed for items %v: %v", ids, err)
}
