package main

import (
	"github.com/serp/api-gateway/src/cmd/bootstrap"
	"go.uber.org/fx"
)

func main() {
	fx.New(bootstrap.All()).Run()
}
