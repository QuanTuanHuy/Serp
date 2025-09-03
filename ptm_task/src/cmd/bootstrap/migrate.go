package bootstrap

import (
	"log"

	"gorm.io/gorm"
)

func InitializeDB(db *gorm.DB) {
	err := db.AutoMigrate()
	if err != nil {
		log.Fatal("Failed to run migrations: ", err)
	}
}
