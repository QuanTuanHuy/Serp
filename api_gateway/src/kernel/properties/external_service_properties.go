/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import (
	"strings"
	"time"

	"github.com/golibs-starter/golib/config"
)

type ServiceProperty struct {
	Host          string
	Port          string
	Timeout       time.Duration
	WebSocketPath string
}

func (s ServiceProperty) BaseURL() string {
	host := s.Host
	if host != "" && !strings.HasPrefix(host, "http://") && !strings.HasPrefix(host, "https://") {
		host = "http://" + host
	}
	if s.Port != "" {
		return host + ":" + s.Port
	}
	return host
}

type ExternalServiceProperties struct {
	AccountService      ServiceProperty
	PTMTask             ServiceProperty
	PTMSchedule         ServiceProperty
	PurchaseService     ServiceProperty
	LogisticsService    ServiceProperty
	CrmService          ServiceProperty
	NotificationService ServiceProperty
	SalesService        ServiceProperty
	DiscussService      ServiceProperty
}

func (e ExternalServiceProperties) Prefix() string {
	return "external.services"
}

func NewExternalServicePropeties(loader config.Loader) (*ExternalServiceProperties, error) {
	props := ExternalServiceProperties{}
	err := loader.Bind(&props)
	return &props, err
}
