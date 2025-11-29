/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"log"

	"github.com/serp/ptm-schedule/src/infrastructure/store/model"
	"gorm.io/gorm"
)

func InitializeDB(db *gorm.DB) {
	err := db.AutoMigrate(
		&model.SchedulePlanModel{},
		&model.ScheduleTaskModel{},
		&model.AvailabilityCalendarModel{},
		&model.CalendarExceptionModel{},
		&model.ScheduleWindowModel{},
		&model.ScheduleEventModel{},
		&model.ProcessedEventModel{},
		&model.FailedEventModel{},
	)
	if err != nil {
		log.Fatal("Failed to run migrations: ", err)
	}
}
