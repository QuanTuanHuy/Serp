/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"errors"
	"slices"
	"strings"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/kernel/properties"
)

type JWTUtils struct {
	keycloakProps     *properties.KeycloakProperties
	keycloakJwksUtils *KeycloakJwksUtils
}

func NewJWTUtils(keycloakProps *properties.KeycloakProperties, keycloakJwksUtils *KeycloakJwksUtils) *JWTUtils {
	return &JWTUtils{
		keycloakProps:     keycloakProps,
		keycloakJwksUtils: keycloakJwksUtils,
	}
}

type Claims struct {
	UserID            int64                  `json:"uid"`
	Email             string                 `json:"email"`
	FullName          string                 `json:"name"`
	PreferredUsername string                 `json:"preferred_username"`
	EmailVerified     bool                   `json:"email_verified"`
	RealmAccess       map[string]interface{} `json:"realm_access"`
	ResourceAccess    map[string]interface{} `json:"resource_access"`
	AuthorizedParty   string                 `json:"azp"`
	SessionId         string                 `json:"sid"`
	jwt.RegisteredClaims
}

func (j *JWTUtils) ValidateToken(tokenString string) (*Claims, error) {
	token, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodRSA); !ok {
			return nil, errors.New("unexpected signing method")
		}

		keyId, ok := token.Header["kid"].(string)
		if !ok {
			log.Warn("No key ID found in JWT header, skipping signature verification")
			return nil, errors.New("no key ID in JWT header")
		}

		publicKey, err := j.keycloakJwksUtils.GetPublicKey(keyId)
		if err != nil {
			log.Warn("Could not find public key for key ID: ", keyId, ", error: ", err)
			return nil, err
		}

		return publicKey, nil
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

		if j.keycloakProps.ExpectedIssuer != "" && claims.Issuer != j.keycloakProps.ExpectedIssuer {
			log.Error("JWT token issuer mismatch. Expected: ", j.keycloakProps.ExpectedIssuer, ", Actual: ", claims.Issuer)
			return nil, errors.New("token issuer mismatch")
		}

		if j.keycloakProps.ExpectedAudience != "" {
			audienceFound := slices.Contains(claims.Audience, j.keycloakProps.ExpectedAudience)
			if !audienceFound {
				log.Error("JWT token audience mismatch. Expected: ", j.keycloakProps.ExpectedAudience, ", Actual: ", claims.Audience)
				return nil, errors.New("token audience mismatch")
			}
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

	var roles []string

	if claims.RealmAccess != nil {
		if realmRoles, ok := claims.RealmAccess["roles"].([]interface{}); ok {
			for _, role := range realmRoles {
				if roleStr, ok := role.(string); ok {
					roles = append(roles, roleStr)
				}
			}
		}
	}

	if claims.ResourceAccess != nil {
		for _, clientAccess := range claims.ResourceAccess {
			if clientMap, ok := clientAccess.(map[string]interface{}); ok {
				if clientRoles, ok := clientMap["roles"].([]interface{}); ok {
					for _, role := range clientRoles {
						if roleStr, ok := role.(string); ok {
							roles = append(roles, roleStr)
						}
					}
				}
			}
		}
	}

	unique := make(map[string]bool)
	var result []string
	for _, role := range roles {
		if !unique[role] {
			unique[role] = true
			result = append(result, role)
		}
	}

	return result, nil
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

func (j *JWTUtils) IsAccessToken(tokenString string) bool {
	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		keyId, _ := token.Header["kid"].(string)
		return j.keycloakJwksUtils.GetPublicKey(keyId)
	})

	if err != nil {
		return false
	}

	if tokenType, ok := token.Header["typ"].(string); ok {
		if tokenType != "JWT" {
			return false
		}
	}

	if mapClaims, ok := token.Claims.(jwt.MapClaims); ok {
		if tokenType, ok := mapClaims["typ"].(string); ok {
			return tokenType == "Bearer"
		}
	}

	return true
}

func (j *JWTUtils) IsRefreshToken(tokenString string) bool {
	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		keyId, _ := token.Header["kid"].(string)
		return j.keycloakJwksUtils.GetPublicKey(keyId)
	})

	if err != nil {
		return false
	}

	if tokenType, ok := token.Header["typ"].(string); ok {
		if tokenType != "JWT" {
			return false
		}
	}

	if mapClaims, ok := token.Claims.(jwt.MapClaims); ok {
		if tokenType, ok := mapClaims["typ"].(string); ok {
			return tokenType == "Refresh"
		}
	}

	return false
}

func (j *JWTUtils) IsTokenExpired(tokenString string) bool {
	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodRSA); !ok {
			return nil, errors.New("unexpected signing method")
		}

		keyId, ok := token.Header["kid"].(string)
		if !ok {
			return nil, errors.New("no key ID in JWT header")
		}

		return j.keycloakJwksUtils.GetPublicKey(keyId)
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

// Keycloak-specific methods

func (j *JWTUtils) GetSubjectFromToken(tokenString string) (string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return "", err
	}
	return claims.Subject, nil
}

func (j *JWTUtils) GetEmailFromToken(tokenString string) (string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return "", err
	}
	return claims.Email, nil
}

func (j *JWTUtils) GetPreferredUsernameFromToken(tokenString string) (string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return "", err
	}
	return claims.PreferredUsername, nil
}

func (j *JWTUtils) GetFullNameFromToken(tokenString string) (string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return "", err
	}
	return claims.FullName, nil
}

func (j *JWTUtils) IsEmailVerifiedFromToken(tokenString string) (bool, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return false, err
	}
	return claims.EmailVerified, nil
}

func (j *JWTUtils) GetRealmRolesFromToken(tokenString string) ([]string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return nil, err
	}

	var roles []string
	if claims.RealmAccess != nil {
		if realmRoles, ok := claims.RealmAccess["roles"].([]interface{}); ok {
			for _, role := range realmRoles {
				if roleStr, ok := role.(string); ok {
					roles = append(roles, roleStr)
				}
			}
		}
	}

	return roles, nil
}

func (j *JWTUtils) GetResourceRolesFromToken(tokenString string, clientId string) ([]string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return nil, err
	}

	var roles []string
	if claims.ResourceAccess != nil {
		if clientAccess, ok := claims.ResourceAccess[clientId].(map[string]interface{}); ok {
			if clientRoles, ok := clientAccess["roles"].([]interface{}); ok {
				for _, role := range clientRoles {
					if roleStr, ok := role.(string); ok {
						roles = append(roles, roleStr)
					}
				}
			}
		}
	}

	return roles, nil
}

func (j *JWTUtils) HasRealmRole(tokenString string, roleName string) bool {
	roles, err := j.GetRealmRolesFromToken(tokenString)
	if err != nil {
		return false
	}
	return slices.Contains(roles, roleName)
}

func (j *JWTUtils) HasResourceRole(tokenString string, clientId string, roleName string) bool {
	roles, err := j.GetResourceRolesFromToken(tokenString, clientId)
	if err != nil {
		return false
	}
	return slices.Contains(roles, roleName)
}

func (j *JWTUtils) GetAuthorizedPartyFromToken(tokenString string) (string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return "", err
	}
	return claims.AuthorizedParty, nil
}

func (j *JWTUtils) GetSessionIdFromToken(tokenString string) (string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return "", err
	}
	return claims.SessionId, nil
}
