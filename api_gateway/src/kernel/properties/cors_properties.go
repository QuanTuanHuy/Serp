package properties

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

func NewDefaultCorsProperties() *CorsProperties {
	return &CorsProperties{
		AllowedOrigins: []string{
			"http://localhost:3000",
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
