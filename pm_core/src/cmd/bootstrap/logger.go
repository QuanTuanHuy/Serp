/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"os"

	"go.uber.org/zap"
)

func NewLogger() (*zap.Logger, error) {
	env := os.Getenv("APP_PROFILES")
	if env == "" || env == "local" {
		return zap.NewDevelopment()
	}
	return zap.NewProduction()
}
