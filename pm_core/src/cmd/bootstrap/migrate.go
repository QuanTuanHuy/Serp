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
		&model.ProjectModel{},
		&model.ProjectMemberModel{},
		&model.WorkItemModel{},
		&model.WorkItemAssignmentModel{},
		&model.WorkItemDependencyModel{},
		&model.SprintModel{},
		&model.MilestoneModel{},
		&model.BoardModel{},
		&model.BoardColumnModel{},
		&model.CommentModel{},
		&model.ActivityLogModel{},
		&model.LabelModel{},
		&model.WorkItemLabelModel{},
	)
	if err != nil {
		log.Fatal("Failed to run migrations: ", err)
	}
	log.Println("Database migration completed successfully")
}
