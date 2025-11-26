/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"crypto/rsa"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io"
	"math/big"
	"net/http"
	"strings"
	"time"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/kernel/properties"
)

type JWKSResponse struct {
	Keys []JWK `json:"keys"`
}

type JWK struct {
	Kty string `json:"kty"`
	Kid string `json:"kid"`
	Use string `json:"use"`
	N   string `json:"n"`
	E   string `json:"e"`
	Alg string `json:"alg"`
}

type KeycloakJwksUtils struct {
	keycloakProps *properties.KeycloakProperties
	keyCache      map[string]*rsa.PublicKey
	lastFetch     time.Time
	cacheTTL      time.Duration
}

func NewKeycloakJwksUtils(keycloakProps *properties.KeycloakProperties) *KeycloakJwksUtils {
	return &KeycloakJwksUtils{
		keycloakProps: keycloakProps,
		keyCache:      make(map[string]*rsa.PublicKey),
		cacheTTL:      5 * time.Minute,
	}
}

func (k *KeycloakJwksUtils) GetPublicKey(keyId string) (*rsa.PublicKey, error) {
	if key, exists := k.keyCache[keyId]; exists && time.Since(k.lastFetch) < k.cacheTTL {
		return key, nil
	}

	if err := k.fetchKeys(); err != nil {
		return nil, err
	}

	if key, exists := k.keyCache[keyId]; exists {
		return key, nil
	}

	return nil, fmt.Errorf("public key not found for key ID: %s", keyId)
}

func (k *KeycloakJwksUtils) fetchKeys() error {
	jwksUrl := k.keycloakProps.JwkSetUri
	if jwksUrl == "" {
		jwksUrl = fmt.Sprintf("%s/realms/%s/protocol/openid-connect/certs",
			k.keycloakProps.Url, k.keycloakProps.Realm)
	}

	log.Info("Fetching JWKS from: ", jwksUrl)

	resp, err := http.Get(jwksUrl)
	if err != nil {
		return fmt.Errorf("failed to fetch JWKS: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to fetch JWKS, status: %d", resp.StatusCode)
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read JWKS response: %w", err)
	}

	var jwksResponse JWKSResponse
	if err := json.Unmarshal(body, &jwksResponse); err != nil {
		return fmt.Errorf("failed to parse JWKS response: %w", err)
	}

	for _, jwk := range jwksResponse.Keys {
		if jwk.Kty == "RSA" && jwk.Use == "sig" {
			publicKey, err := k.parseRSAPublicKey(jwk)
			if err != nil {
				log.Warn("Failed to parse RSA public key for kid: ", jwk.Kid, ", error: ", err)
				continue
			}
			k.keyCache[jwk.Kid] = publicKey
		}
	}

	k.lastFetch = time.Now()
	log.Info("Successfully cached ", len(k.keyCache), " public keys")
	return nil
}

func (k *KeycloakJwksUtils) parseRSAPublicKey(jwk JWK) (*rsa.PublicKey, error) {
	nBytes, err := base64URLDecode(jwk.N)
	if err != nil {
		return nil, fmt.Errorf("failed to decode modulus: %w", err)
	}

	eBytes, err := base64URLDecode(jwk.E)
	if err != nil {
		return nil, fmt.Errorf("failed to decode exponent: %w", err)
	}

	n := new(big.Int).SetBytes(nBytes)
	e := new(big.Int).SetBytes(eBytes)

	publicKey := &rsa.PublicKey{
		N: n,
		E: int(e.Int64()),
	}

	return publicKey, nil
}

func base64URLDecode(s string) ([]byte, error) {
	s = strings.ReplaceAll(s, "-", "+")
	s = strings.ReplaceAll(s, "_", "/")

	switch len(s) % 4 {
	case 2:
		s += "=="
	case 3:
		s += "="
	}

	return base64.StdEncoding.DecodeString(s)
}
