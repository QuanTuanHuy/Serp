/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"gorm.io/gorm"
)

type IDBTransactionPort interface {
	BeginTransaction(ctx context.Context) (*gorm.DB, error)
	CommitTransaction(tx *gorm.DB) error
	RollbackTransaction(tx *gorm.DB) error
}
