package port

import "gorm.io/gorm"

type IDBTransactionPort interface {
	StartTransaction() *gorm.DB
	Commit(tx *gorm.DB) error
	Rollback(tx *gorm.DB) error
}
