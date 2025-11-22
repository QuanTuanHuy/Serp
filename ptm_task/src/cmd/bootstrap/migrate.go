/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"log"

	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/gorm"
)

func InitializeDB(db *gorm.DB) {
	err := db.AutoMigrate(
		&model.ProjectModel{},
		&model.TaskModel{},
		&model.NoteModel{},
		&model.TaskDependencyGraphModel{},
		&model.ActivityEventModel{},
	)
	if err != nil {
		log.Fatal("Failed to run migrations: ", err)
	}
	log.Println("Database migration skipped - add your models when ready")
}
