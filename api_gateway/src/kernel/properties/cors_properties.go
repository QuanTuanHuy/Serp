/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import "github.com/golibs-starter/golib/config"

type CorsProperties struct {
	AllowedOrigins   []string
	AllowedMethods   []string
	AllowedHeaders   []string
	ExposedHeaders   []string
	AllowCredentials bool
	MaxAge           int
	AllowAllOrigins  bool
	AllowOriginFunc  string
	AllowWildcard    bool
	AllowBrowserExt  bool
	AllowWebSockets  bool
	AllowFiles       bool
}

func (c CorsProperties) Prefix() string {
	return "app.cors"
}

func NewCorsProperties(loader config.Loader) (*CorsProperties, error) {
	props := CorsProperties{}
	err := loader.Bind(&props)
	return &props, err
}

func NewDefaultCorsProperties() *CorsProperties {
	return &CorsProperties{
		AllowedOrigins: []string{
			"http://localhost:3000",
			"https://serp-soict.vercel.app",
			"https://serp.texkis.com",
		},
		AllowedMethods: []string{
			"GET",
			"POST",
			"PUT",
			"PATCH",
			"DELETE",
			"HEAD",
			"OPTIONS",
		},
		AllowedHeaders: []string{
			"Origin",
			"Content-Length",
			"Content-Type",
			"Authorization",
			"Accept",
			"X-Requested-With",
			"X-CSRF-Token",
			"X-Request-ID",
		},
		ExposedHeaders: []string{
			"Content-Length",
			"X-Request-ID",
		},
		AllowCredentials: true,
		MaxAge:           12 * 3600, // 12 hours
		AllowAllOrigins:  false,
		AllowWildcard:    false,
		AllowBrowserExt:  false,
		AllowWebSockets:  false,
		AllowFiles:       false,
	}
}
