/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

type DatabaseProperties struct {
	Driver   string `mapstructure:"driver"`
	Host     string `mapstructure:"host"`
	Port     int    `mapstructure:"port"`
	Database string `mapstructure:"database"`
	Username string `mapstructure:"username"`
	Password string `mapstructure:"password"`
	LogLevel string `mapstructure:"logLevel"`
}

type RedisProperties struct {
	Host string `mapstructure:"host"`
	Port int    `mapstructure:"port"`
}

type LoggingProperties struct {
	Level string `mapstructure:"level"`
}

type AppProperties struct {
	Name       string             `mapstructure:"name"`
	Path       string             `mapstructure:"path"`
	Port       int                `mapstructure:"port"`
	Logging    LoggingProperties  `mapstructure:"logging"`
	Datasource DatabaseProperties `mapstructure:"datasource"`
	Redis      RedisProperties    `mapstructure:"redis"`
	Keycloak   KeycloakProperties `mapstructure:"keycloak"`
}
