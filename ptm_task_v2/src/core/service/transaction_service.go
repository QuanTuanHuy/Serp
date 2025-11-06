/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

type ITransactionService interface {
	ExecuteInTransaction(ctx context.Context, fn func(tx *gorm.DB) error) error
	ExecuteInTransactionWithResult(ctx context.Context, fn func(tx *gorm.DB) (any, error)) (any, error)
}

type transactionService struct {
	dbTxPort store.IDBTransactionPort
}

func NewTransactionService(dbTxPort store.IDBTransactionPort) ITransactionService {
	return &transactionService{
		dbTxPort: dbTxPort,
	}
}

func (s *transactionService) ExecuteInTransaction(ctx context.Context, fn func(tx *gorm.DB) error) error {
	tx := s.dbTxPort.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			s.dbTxPort.Rollback(tx)
			panic(r)
		}
	}()

	if err := fn(tx); err != nil {
		s.dbTxPort.Rollback(tx)
		return err
	}
	return s.dbTxPort.Commit(tx)
}

func (s *transactionService) ExecuteInTransactionWithResult(ctx context.Context, fn func(tx *gorm.DB) (any, error)) (any, error) {
	var result any
	tx := s.dbTxPort.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			s.dbTxPort.Rollback(tx)
			panic(r)
		}
	}()

	var err error
	result, err = fn(tx)
	if err != nil {
		s.dbTxPort.Rollback(tx)
		return result, err
	}
	if err := s.dbTxPort.Commit(tx); err != nil {
		return result, err
	}
	return result, nil
}
