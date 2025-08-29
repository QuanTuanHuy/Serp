package utils

import (
	"context"
	"net/http"
)

func BuildDefaultHeaders() http.Header {
	headers := http.Header{}
	headers.Set("Content-Type", "application/json")
	headers.Set("Accept", "application/json")
	return headers
}

func BuildAuthorizationHeaders(token string) http.Header {
	headers := BuildDefaultHeaders()
	headers.Set("Authorization", "Bearer "+token)
	return headers
}

func BuildHeadersFromContext(ctx context.Context) http.Header {
	authContext, ok := GetAuthContext(ctx)
	if !ok || authContext == nil || authContext.AccessToken == "" {
		return BuildDefaultHeaders()
	}
	return BuildAuthorizationHeaders(authContext.AccessToken)
}
