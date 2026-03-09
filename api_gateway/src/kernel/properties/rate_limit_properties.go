/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import "github.com/golibs-starter/golib/config"

// RateLimitRule defines the limit and window for a single rate limiting tier.
type RateLimitRule struct {
	Limit      int `mapstructure:"limit"`
	WindowSecs int `mapstructure:"windowSecs"`
}

// RouteOverride defines per-route rate limit overrides.
type RouteOverride struct {
	Path   string         `mapstructure:"path"`
	Method string         `mapstructure:"method"`
	IP     *RateLimitRule `mapstructure:"ip"`
	User   *RateLimitRule `mapstructure:"user"`
}

// RateLimitProperties is the top-level rate limiting configuration.
type RateLimitProperties struct {
	Enabled        bool                     `mapstructure:"enabled"`
	DefaultIP      RateLimitRule            `mapstructure:"defaultIP"`
	DefaultUser    RateLimitRule            `mapstructure:"defaultUser"`
	RouteOverrides map[string]RouteOverride `mapstructure:"routeOverrides"`
}

func (r RateLimitProperties) Prefix() string {
	return "app.rateLimit"
}

func NewRateLimitProperties(loader config.Loader) (*RateLimitProperties, error) {
	props := RateLimitProperties{}
	err := loader.Bind(&props)
	return &props, err
}

func NewDefaultRateLimitProperties() *RateLimitProperties {
	return &RateLimitProperties{
		Enabled: true,
		DefaultIP: RateLimitRule{
			Limit:      100,
			WindowSecs: 60,
		},
		DefaultUser: RateLimitRule{
			Limit:      200,
			WindowSecs: 60,
		},
		RouteOverrides: map[string]RouteOverride{
			"login": {
				Path:   "/api/v1/auth/login",
				Method: "POST",
				IP:     &RateLimitRule{Limit: 10, WindowSecs: 60},
			},
			"register": {
				Path:   "/api/v1/auth/register",
				Method: "POST",
				IP:     &RateLimitRule{Limit: 5, WindowSecs: 60},
			},
		},
	}
}
