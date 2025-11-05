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
	// Add your models here for auto-migration
	// Example:
	// err := db.AutoMigrate(
	// 	&model.TaskModel{},
	// 	&model.ProjectModel{},
	// )
	// if err != nil {
	// 	log.Fatal("Failed to run migrations: ", err)
	// }
	log.Println("Database migration skipped - add your models when ready")
}
