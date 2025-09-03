/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"crypto/rsa"
	"crypto/x509"
	"encoding/base64"
	"encoding/pem"
	"errors"
	"slices"
	"strings"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/kernel/properties"
)

type JWTUtils struct {
	jwtProperties *properties.JwtProperties
	publicKey     *rsa.PublicKey
}

func NewJWTUtils(jwtProperties *properties.JwtProperties) *JWTUtils {
	jwtUtils := &JWTUtils{
		jwtProperties: jwtProperties,
	}

	publicKey, err := jwtUtils.loadPublicKey(jwtProperties.PublicKey)
	if err != nil {
		log.Error("Failed to load JWT public key: ", err)
		panic("Failed to initialize JWT public key")
	}
	jwtUtils.publicKey = publicKey

	return jwtUtils
}

type Claims struct {
	UserID   int64  `json:"user_id"`
	Email    string `json:"email"`
	FullName string `json:"full_name"`
	Type     string `json:"type"`
	Scope    string `json:"scope"`
	jwt.RegisteredClaims
}

func (j *JWTUtils) ValidateToken(tokenString string) (*Claims, error) {
	token, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodRSA); !ok {
			return nil, errors.New("unexpected signing method")
		}
		return j.publicKey, nil
	})

	if err != nil {
		log.Error("Failed to parse JWT token: ", err)
		return nil, err
	}

	if claims, ok := token.Claims.(*Claims); ok && token.Valid {
		if claims.ExpiresAt != nil && time.Now().After(claims.ExpiresAt.Time) {
			log.Error("JWT token is expired")
			return nil, errors.New("token is expired")
		}
		return claims, nil
	}

	return nil, errors.New("invalid token")
}

func (j *JWTUtils) ExtractUserID(tokenString string) (int64, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return 0, err
	}
	return claims.UserID, nil
}

func (j *JWTUtils) ExtractEmail(tokenString string) (string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return "", err
	}
	return claims.Email, nil
}

func (j *JWTUtils) ExtractFullName(tokenString string) (string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return "", err
	}
	return claims.FullName, nil
}

func (j *JWTUtils) ExtractRoles(tokenString string) ([]string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return nil, err
	}

	if claims.Scope == "" {
		return []string{}, nil
	}

	var roles []string
	authorities := strings.Fields(claims.Scope)

	for _, authority := range authorities {
		if strings.HasPrefix(authority, "ROLE_") {
			role := strings.TrimPrefix(authority, "ROLE_")
			roles = append(roles, role)
		}
	}

	return roles, nil
}

func (j *JWTUtils) ExtractPermissions(tokenString string) ([]string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return nil, err
	}

	if claims.Scope == "" {
		return []string{}, nil
	}

	var permissions []string
	authorities := strings.Fields(claims.Scope)

	for _, authority := range authorities {
		if !strings.HasPrefix(authority, "ROLE_") {
			permissions = append(permissions, authority)
		}
	}

	return permissions, nil
}

func (j *JWTUtils) ExtractAuthorities(tokenString string) ([]string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return nil, err
	}

	if claims.Scope == "" {
		return []string{}, nil
	}

	return strings.Fields(claims.Scope), nil
}

func (j *JWTUtils) HasRole(tokenString string, roleName string) bool {
	roles, err := j.ExtractRoles(tokenString)
	if err != nil {
		return false
	}

	roleUpper := strings.ToUpper(roleName)
	for _, role := range roles {
		if strings.ToUpper(role) == roleUpper {
			return true
		}
	}
	return false
}

func (j *JWTUtils) HasPermission(tokenString string, permissionName string) bool {
	permissions, err := j.ExtractPermissions(tokenString)
	if err != nil {
		return false
	}

	return slices.Contains(permissions, permissionName)
}

func (j *JWTUtils) IsAccessToken(tokenString string) bool {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return false
	}
	return claims.Type == "access_token"
}

func (j *JWTUtils) IsRefreshToken(tokenString string) bool {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return false
	}
	return claims.Type == "refresh_token"
}

func (j *JWTUtils) IsTokenExpired(tokenString string) bool {
	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodRSA); !ok {
			return nil, errors.New("unexpected signing method")
		}
		return j.publicKey, nil
	})

	if err != nil {
		return true
	}

	if claims, ok := token.Claims.(jwt.MapClaims); ok {
		if exp, ok := claims["exp"].(float64); ok {
			return time.Now().Unix() > int64(exp)
		}
	}

	return true
}

func (j *JWTUtils) loadPublicKey(publicKeyString string) (*rsa.PublicKey, error) {
	// Clean the public key string
	cleanPublicKey := strings.ReplaceAll(publicKeyString, "-----BEGIN PUBLIC KEY-----", "")
	cleanPublicKey = strings.ReplaceAll(cleanPublicKey, "-----END PUBLIC KEY-----", "")
	cleanPublicKey = strings.ReplaceAll(cleanPublicKey, "\n", "")
	cleanPublicKey = strings.ReplaceAll(cleanPublicKey, "\r", "")
	cleanPublicKey = strings.TrimSpace(cleanPublicKey)

	// Decode base64
	keyBytes, err := base64.StdEncoding.DecodeString(cleanPublicKey)
	if err != nil {
		// Try with PEM format
		block, _ := pem.Decode([]byte(publicKeyString))
		if block == nil {
			return nil, errors.New("failed to parse PEM block containing the public key")
		}
		keyBytes = block.Bytes
	}

	// Parse the public key
	publicKey, err := x509.ParsePKIXPublicKey(keyBytes)
	if err != nil {
		return nil, err
	}

	rsaPublicKey, ok := publicKey.(*rsa.PublicKey)
	if !ok {
		return nil, errors.New("not an RSA public key")
	}

	return rsaPublicKey, nil
}
