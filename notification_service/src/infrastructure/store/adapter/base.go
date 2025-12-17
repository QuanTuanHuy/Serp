package adapter

import "gorm.io/gorm"

type BaseStoreAdapter struct {
	db *gorm.DB
}

func (a *BaseStoreAdapter) WithTx(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
