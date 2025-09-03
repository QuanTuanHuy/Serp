/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package main

import (
	"github.com/serp/ptm-task/src/cmd/bootstrap"
	"go.uber.org/fx"
)

func main() {
	fx.New(bootstrap.All()).Run()
}
