/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	port "github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

type ITransactionService interface {
	ExecuteInTransaction(ctx context.Context, fn func(tx *gorm.DB) error) error
	ExecuteInTransactionWithResult(ctx context.Context, fn func(tx *gorm.DB) (any, error)) (any, error)
}

type TransactionService struct {
	dbTxPort port.IDBTransactionPort
}

func (t *TransactionService) ExecuteInTransaction(ctx context.Context, fn func(tx *gorm.DB) error) error {
	tx := t.dbTxPort.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			t.dbTxPort.Rollback(tx)
			panic(r)
		}
	}()

	if err := fn(tx); err != nil {
		if rollbackErr := t.dbTxPort.Rollback(tx); rollbackErr != nil {
			log.Error(ctx, "Failed to rollback transaction: ", "error ", rollbackErr)
		}
		log.Info(ctx, "Transaction rolled back due to error: ", err)
		return err
	}
	if err := t.dbTxPort.Commit(tx); err != nil {
		log.Error(ctx, "Failed to commit transaction: ", "error ", err)
		return err
	}
	return nil
}

func (t *TransactionService) ExecuteInTransactionWithResult(ctx context.Context, fn func(tx *gorm.DB) (any, error)) (any, error) {
	var result any
	tx := t.dbTxPort.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			t.dbTxPort.Rollback(tx)
			panic(r)
		}
	}()

	var err error
	result, err = fn(tx)
	if err != nil {
		if rollbackErr := t.dbTxPort.Rollback(tx); rollbackErr != nil {
			log.Error(ctx, "Failed to rollback transaction: ", "error ", rollbackErr)
		}
		log.Info(ctx, "Transaction rolled back due to error: ", err)
		return result, err
	}
	if err := t.dbTxPort.Commit(tx); err != nil {
		log.Error(ctx, "Failed to commit transaction: ", "error ", err)
		return result, err
	}
	return result, nil

}

func NewTransactionService(dbTxPort port.IDBTransactionPort) ITransactionService {
	return &TransactionService{dbTxPort: dbTxPort}
}
