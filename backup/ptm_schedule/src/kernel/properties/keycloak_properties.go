/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import "github.com/golibs-starter/golib/config"

type KeycloakProperties struct {
	Url              string
	JwkSetUri        string
	Realm            string
	ClientId         string
	ClientSecret     string
	ExpectedIssuer   string
	ExpectedAudience string
}

func (k KeycloakProperties) Prefix() string {
	return "app.keycloak"
}

func NewKeycloakProperties(loader config.Loader) (*KeycloakProperties, error) {
	var props KeycloakProperties
	if err := loader.Bind(&props); err != nil {
		return nil, err
	}
	return &props, nil
}
