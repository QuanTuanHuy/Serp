/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"gorm.io/gorm"
)

type DBTransactionAdapter struct {
	db *gorm.DB
}

func (d *DBTransactionAdapter) Commit(tx *gorm.DB) error {
	return tx.Commit().Error
}

func (d *DBTransactionAdapter) Rollback(tx *gorm.DB) error {
	return tx.Rollback().Error
}

func (d *DBTransactionAdapter) StartTransaction() *gorm.DB {
	return d.db.Begin()
}

func NewDBTransactionAdapter(db *gorm.DB) port.IDBTransactionPort {
	return &DBTransactionAdapter{db: db}
}
