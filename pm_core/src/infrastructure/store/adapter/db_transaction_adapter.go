/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"github.com/serp/pm-core/src/core/port/store"
	"gorm.io/gorm"
)

type DBTransactionAdapter struct {
	db *gorm.DB
}

func NewDBTransactionAdapter(db *gorm.DB) store.IDBTransactionPort {
	return &DBTransactionAdapter{db: db}
}

func (a *DBTransactionAdapter) StartTransaction() *gorm.DB {
	return a.db.Begin()
}

func (a *DBTransactionAdapter) Commit(tx *gorm.DB) error {
	return tx.Commit().Error
}

func (a *DBTransactionAdapter) Rollback(tx *gorm.DB) {
	tx.Rollback()
}
