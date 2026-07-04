/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import (
	"time"

	"github.com/golibs-starter/golib/config"
)

type TransportProperties struct {
	MaxIdleConns          int           `mapstructure:"maxIdleConns"`
	MaxIdleConnsPerHost   int           `mapstructure:"maxIdleConnsPerHost"`
	MaxConnsPerHost       int           `mapstructure:"maxConnsPerHost"`
	IdleConnTimeout       time.Duration `mapstructure:"idleConnTimeout"`
	DialTimeout           time.Duration `mapstructure:"dialTimeout"`
	TLSHandshakeTimeout   time.Duration `mapstructure:"tlsHandshakeTimeout"`
	ResponseHeaderTimeout time.Duration `mapstructure:"responseHeaderTimeout"`
	ExpectContinueTimeout time.Duration `mapstructure:"expectContinueTimeout"`
}

func (t TransportProperties) Prefix() string {
	return "app.transport"
}

func NewTransportProperties(loader config.Loader) (*TransportProperties, error) {
	props := NewDefaultTransportProperties()
	err := loader.Bind(props)
	return props, err
}

func NewDefaultTransportProperties() *TransportProperties {
	return &TransportProperties{
		MaxIdleConns:          200,
		MaxIdleConnsPerHost:   100,
		MaxConnsPerHost:       200,
		IdleConnTimeout:       90 * time.Second,
		DialTimeout:           5 * time.Second,
		TLSHandshakeTimeout:   5 * time.Second,
		ResponseHeaderTimeout: 15 * time.Second,
		ExpectContinueTimeout: 1 * time.Second,
	}
}
