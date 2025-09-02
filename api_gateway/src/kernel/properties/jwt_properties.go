/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import "github.com/golibs-starter/golib/config"

type JwtProperties struct {
	PublicKey string
}

func (j JwtProperties) Prefix() string {
	return "app.security.jwt"
}

func NewJwtProperties(loader config.Loader) (*JwtProperties, error) {
	var props JwtProperties
	if err := loader.Bind(&props); err != nil {
		return nil, err
	}
	return &props, nil
}
