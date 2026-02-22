/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package main

import (
	"github.com/serp/pm-core/src/cmd/bootstrap"
	"go.uber.org/fx"
)

func main() {
	fx.New(bootstrap.All()).Run()
}
