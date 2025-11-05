/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"

	"github.com/serp/ptm-task/src/core/domain/constant"
	"gorm.io/gorm"
)

type ITransactionService interface {
	ExecuteInTransaction(ctx context.Context, db *gorm.DB, fn func(tx *gorm.DB) error) error
	ExecuteInTransactionWithResult(ctx context.Context, db *gorm.DB, fn func(tx *gorm.DB) (interface{}, error)) (interface{}, error)
}

type transactionService struct{}

func NewTransactionService() ITransactionService {
	return &transactionService{}
}

func (s *transactionService) ExecuteInTransaction(ctx context.Context, db *gorm.DB, fn func(tx *gorm.DB) error) error {
	if db == nil {
		return errors.New(constant.DatabaseConnectionNil)
	}
	return db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		return fn(tx)
	})
}

func (s *transactionService) ExecuteInTransactionWithResult(ctx context.Context, db *gorm.DB, fn func(tx *gorm.DB) (interface{}, error)) (interface{}, error) {
	if db == nil {
		return nil, errors.New(constant.DatabaseConnectionNil)
	}
	var result interface{}
	err := db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		res, err := fn(tx)
		if err != nil {
			return err
		}
		result = res
		return nil
	})
	return result, err
}
