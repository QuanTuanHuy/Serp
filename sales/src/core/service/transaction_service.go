/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	port "github.com/serp/sales/src/core/port/store"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type ITransactionService interface {
	ExecuteInTransaction(ctx context.Context, fn func(tx *gorm.DB) error) error
	ExecuteInTransactionWithResult(ctx context.Context, fn func(tx *gorm.DB) (any, error)) (any, error)
}

type TransactionService struct {
	txPort port.IDBTransactionPort
	logger *zap.Logger
}

func (t *TransactionService) ExecuteInTransaction(ctx context.Context, fn func(tx *gorm.DB) error) error {
	tx, err := t.txPort.BeginTransaction(ctx)
	if err != nil {
		t.logger.Error("Failed to begin transaction", zap.Error(err))
		return err
	}

	if err := fn(tx); err != nil {
		if rbErr := t.txPort.RollbackTransaction(tx); rbErr != nil {
			t.logger.Error("Failed to rollback transaction", zap.Error(rbErr))
		}
		return err
	}

	if err := t.txPort.CommitTransaction(tx); err != nil {
		t.logger.Error("Failed to commit transaction", zap.Error(err))
		return err
	}

	return nil
}

func (t *TransactionService) ExecuteInTransactionWithResult(ctx context.Context, fn func(tx *gorm.DB) (any, error)) (any, error) {
	tx, err := t.txPort.BeginTransaction(ctx)
	if err != nil {
		t.logger.Error("Failed to begin transaction", zap.Error(err))
		return nil, err
	}

	result, err := fn(tx)
	if err != nil {
		if rbErr := t.txPort.RollbackTransaction(tx); rbErr != nil {
			t.logger.Error("Failed to rollback transaction", zap.Error(rbErr))
		}
		return nil, err
	}

	if err := t.txPort.CommitTransaction(tx); err != nil {
		t.logger.Error("Failed to commit transaction", zap.Error(err))
		return nil, err
	}

	return result, nil
}

func NewTransactionService(txPort port.IDBTransactionPort, logger *zap.Logger) ITransactionService {
	return &TransactionService{
		txPort: txPort,
		logger: logger,
	}
}
