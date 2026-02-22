/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"log"

	"github.com/serp/pm-core/src/infrastructure/store/model"
	"gorm.io/gorm"
)

func InitializeDB(db *gorm.DB) {
	err := db.AutoMigrate(
		&model.LabelModel{},
		&model.PriorityModel{},
		&model.IssueTypeModel{},
		&model.PrioritySchemeModel{},
		&model.PrioritySchemeItemModel{},
		&model.IssueTypeSchemeModel{},
		&model.IssueTypeSchemeItemModel{},
	)
	if err != nil {
		log.Fatal("Failed to run migrations: ", err)
	}
	log.Println("Database migration completed successfully")
}
