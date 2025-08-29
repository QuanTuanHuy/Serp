package utils

import (
	"context"
	"net/http"
)

type AuthContext struct {
	AccessToken string
	Headers     http.Header
}

type contextKey string

const AuthContextKey contextKey = "authContext"

func SetAuthContext(ctx context.Context, authContext *AuthContext) context.Context {
	return context.WithValue(ctx, AuthContextKey, authContext)
}

func GetAuthContext(ctx context.Context) (*AuthContext, bool) {
	authContext, ok := ctx.Value(AuthContextKey).(*AuthContext)
	return authContext, ok
}
