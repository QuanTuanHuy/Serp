/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

type KeycloakProperties struct {
	Url              string `mapstructure:"url"`
	JwkSetUri        string `mapstructure:"jwk-set-uri"`
	Realm            string `mapstructure:"realm"`
	ClientId         string `mapstructure:"client-id"`
	ClientSecret     string `mapstructure:"client-secret"`
	ExpectedIssuer   string `mapstructure:"expected-issuer"`
	ExpectedAudience string `mapstructure:"expected-audience"`
}
