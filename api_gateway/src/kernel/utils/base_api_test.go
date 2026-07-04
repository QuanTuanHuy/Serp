package utils

import (
	"net/http"
	"testing"
	"time"

	"github.com/serp/api-gateway/src/kernel/properties"
)

func TestBaseAPIClientFactory_NewUsesConfiguredHTTPClient(t *testing.T) {
	factory := NewBaseAPIClientFactory(&properties.TransportProperties{
		MaxIdleConns:          10,
		MaxIdleConnsPerHost:   10,
		MaxConnsPerHost:       10,
		IdleConnTimeout:       time.Minute,
		DialTimeout:           time.Second,
		TLSHandshakeTimeout:   time.Second,
		ResponseHeaderTimeout: time.Second,
		ExpectContinueTimeout: time.Second,
	})

	apiClient := factory.New("http://example.test", 7*time.Second)
	httpClient, ok := apiClient.client.(*http.Client)
	if !ok {
		t.Fatalf("expected *http.Client, got %T", apiClient.client)
	}
	if httpClient.Timeout != 7*time.Second {
		t.Fatalf("expected timeout 7s, got %v", httpClient.Timeout)
	}
	if httpClient.Transport == nil {
		t.Fatalf("expected non-nil transport")
	}
}
