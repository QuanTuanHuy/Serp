/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"log"

	"github.com/serp/notification-service/src/infrastructure/store/model"
	"gorm.io/gorm"
)

func InitializeDB(db *gorm.DB) {
	err := db.AutoMigrate(
		&model.NotificationModel{},
		&model.NotificationPreferenceModel{},
		&model.ProcessedEventModel{},
		&model.FailedEventModel{},
	)
	if err != nil {
		log.Fatal("Failed to run migrations: ", err)
	}
	log.Println("Database migration skipped - add your models when ready")
}
