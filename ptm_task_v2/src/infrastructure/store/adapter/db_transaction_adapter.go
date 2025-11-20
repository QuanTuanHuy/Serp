/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"fmt"

	"github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

type DBTransactionAdapter struct {
	db *gorm.DB
}

func NewDBTransactionAdapter(db *gorm.DB) store.IDBTransactionPort {
	return &DBTransactionAdapter{db: db}
}

// StartTransaction starts a new database transaction
func (a *DBTransactionAdapter) StartTransaction() *gorm.DB {
	return a.db.Begin()
}

// Commit commits the transaction
func (a *DBTransactionAdapter) Commit(tx *gorm.DB) error {
	if tx == nil {
		return fmt.Errorf("transaction is nil")
	}

	if err := tx.Commit().Error; err != nil {
		return fmt.Errorf("failed to commit transaction: %w", err)
	}

	return nil
}

// Rollback rolls back the transaction
func (a *DBTransactionAdapter) Rollback(tx *gorm.DB) error {
	if tx == nil {
		return fmt.Errorf("transaction is nil")
	}

	if err := tx.Rollback().Error; err != nil {
		return fmt.Errorf("failed to rollback transaction: %w", err)
	}

	return nil
}
