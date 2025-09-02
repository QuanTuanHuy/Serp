/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import (
	"time"

	"github.com/golibs-starter/golib/config"
)

type ServiceProperty struct {
	Host    string
	Port    string
	Timeout time.Duration
}

type ExternalServiceProperties struct {
	AccountService ServiceProperty
}

func (e ExternalServiceProperties) Prefix() string {
	return "external.services"
}

func NewExternalServicePropeties(loader config.Loader) (*ExternalServiceProperties, error) {
	props := ExternalServiceProperties{}
	err := loader.Bind(&props)
	return &props, err
}
