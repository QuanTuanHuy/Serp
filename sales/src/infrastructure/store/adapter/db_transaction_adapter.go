/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"

	port "github.com/serp/sales/src/core/port/store"
	"gorm.io/gorm"
)

type DBTransactionAdapter struct {
	db *gorm.DB
}

func (d *DBTransactionAdapter) BeginTransaction(ctx context.Context) (*gorm.DB, error) {
	return d.db.WithContext(ctx).Begin(), nil
}

func (d *DBTransactionAdapter) CommitTransaction(tx *gorm.DB) error {
	return tx.Commit().Error
}

func (d *DBTransactionAdapter) RollbackTransaction(tx *gorm.DB) error {
	return tx.Rollback().Error
}

func NewDBTransactionAdapter(db *gorm.DB) port.IDBTransactionPort {
	return &DBTransactionAdapter{db: db}
}
