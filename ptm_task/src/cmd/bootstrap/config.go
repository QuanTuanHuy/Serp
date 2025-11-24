/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"fmt"
	"os"
	"path/filepath"
	"regexp"
	"strings"

	"github.com/spf13/viper"
)

func NewConfig() (*viper.Viper, error) {
	v := viper.New()

	if configPaths := os.Getenv("APP_CONFIG_PATHS"); configPaths != "" {
		for _, p := range strings.Split(configPaths, string(os.PathListSeparator)) {
			if trimmed := strings.TrimSpace(p); trimmed != "" {
				v.AddConfigPath(trimmed)
			}
		}
	} else {
		v.AddConfigPath(filepath.FromSlash("./src/config"))
		v.AddConfigPath(".")
	}
	configFormat := os.Getenv("APP_CONFIG_FORMAT")
	if configFormat == "" {
		configFormat = "yaml"
	}
	v.SetConfigType(configFormat)
	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_", "-", "_"))
	v.AutomaticEnv()

	profile := os.Getenv("APP_PROFILES")
	if profile == "" {
		profile = "local"
	}
	v.SetConfigName("default")
	if err := v.MergeInConfig(); err != nil {
		fmt.Printf("No base config found: default.%s\n", configFormat)
	}
	v.SetConfigName(profile)
	if err := v.MergeInConfig(); err != nil {
		fmt.Printf("No profile config found for: %s\n", profile)
	}

	// Supports ${VAR} and ${VAR:-default} patterns.
	expandEnvVariables(v)

	return v, nil
}

func expandEnvVariables(v *viper.Viper) {
	settings := v.AllSettings()
	processed := expandValue(settings)
	if m, ok := processed.(map[string]any); ok {
		_ = v.MergeConfigMap(m)
	}
}

var defaultPattern = regexp.MustCompile(`\$\{([A-Za-z_][A-Za-z0-9_]*)\:-([^}]*)\}`)

// expandString expands ${VAR:-default} first, then ${VAR}/$VAR via os.ExpandEnv
func expandString(s string) string {
	// First handle ${VAR:-default}
	out := defaultPattern.ReplaceAllStringFunc(s, func(m string) string {
		sub := defaultPattern.FindStringSubmatch(m)
		if len(sub) != 3 {
			return m
		}
		name := sub[1]
		def := sub[2]
		if val, ok := os.LookupEnv(name); ok && val != "" {
			return val
		}
		return def
	})
	return os.ExpandEnv(out)
}

func expandValue(v any) any {
	switch t := v.(type) {
	case string:
		return expandString(t)
	case map[string]any:
		for k, val := range t {
			t[k] = expandValue(val)
		}
		return t
	case []any:
		for i := range t {
			t[i] = expandValue(t[i])
		}
		return t
	default:
		return v
	}
}
