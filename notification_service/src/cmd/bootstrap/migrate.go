/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

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
	log.Println("Database migration skipped - add your models when ready")
}
