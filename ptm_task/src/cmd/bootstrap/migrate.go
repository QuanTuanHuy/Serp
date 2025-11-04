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
		&model.GroupTaskModel{},
		&model.TaskModel{},
		&model.CommentModel{},
		&model.NoteModel{},
		&model.TagModel{},
		&model.TaggedItemModel{},
	)
	if err != nil {
		log.Fatal("Failed to run migrations: ", err)
	}
}
