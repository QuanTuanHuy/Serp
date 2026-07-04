package utils

import (
	"context"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/golibs-starter/golib/log"
)

type benchmarkBaseResponse struct {
	Code   int    `json:"code"`
	Status string `json:"status"`
	Data   any    `json:"data"`
}

func init() {
	benchmarkLogger, err := log.NewZapLogger(&log.Options{
		LogLevel:       "error",
		JsonOutputMode: true,
		DisableCaller:  true,
	})
	if err == nil {
		log.ReplaceGlobal(benchmarkLogger)
	}
}

func newBenchmarkBaseAPIClient(b *testing.B, responseBody string) (*BaseAPIClient, func()) {
	b.Helper()

	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(responseBody))
	}))

	client := NewBaseAPIClient(upstream.URL, 5*time.Second)
	return client, upstream.Close
}

func benchmarkBaseAPIClientGET(b *testing.B, responseBody string) {
	apiClient, cleanup := newBenchmarkBaseAPIClient(b, responseBody)
	defer cleanup()

	ctx := context.Background()
	headers := BuildDefaultHeaders()

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		resp, err := apiClient.GET(ctx, "/api/v1/items", headers)
		if err != nil {
			b.Fatalf("GET failed: %v", err)
		}
		var decoded benchmarkBaseResponse
		if err := apiClient.UnmarshalResponse(ctx, resp, &decoded); err != nil {
			b.Fatalf("unmarshal failed: %v", err)
		}
		if decoded.Code != http.StatusOK {
			b.Fatalf("expected code %d, got %d", http.StatusOK, decoded.Code)
		}
	}
}

func benchmarkBaseAPIClientPOST(b *testing.B, requestBody map[string]any, responseBody string) {
	apiClient, cleanup := newBenchmarkBaseAPIClient(b, responseBody)
	defer cleanup()

	ctx := context.Background()
	headers := BuildDefaultHeaders()

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		resp, err := apiClient.POST(ctx, "/api/v1/items", requestBody, headers)
		if err != nil {
			b.Fatalf("POST failed: %v", err)
		}
		var decoded benchmarkBaseResponse
		if err := apiClient.UnmarshalResponse(ctx, resp, &decoded); err != nil {
			b.Fatalf("unmarshal failed: %v", err)
		}
		if decoded.Code != http.StatusOK {
			b.Fatalf("expected code %d, got %d", http.StatusOK, decoded.Code)
		}
	}
}

func BenchmarkBaseAPIClient_GET_SmallJSON(b *testing.B) {
	benchmarkBaseAPIClientGET(b, `{"code":200,"status":"success","data":{"id":1}}`)
}

func BenchmarkBaseAPIClient_GET_MediumJSON(b *testing.B) {
	item := `{"id":1,"name":"item","description":"` + strings.Repeat("x", 128) + `"}`
	body := `{"code":200,"status":"success","data":[` + strings.Repeat(item+",", 49) + item + `]}`
	benchmarkBaseAPIClientGET(b, body)
}

func BenchmarkBaseAPIClient_POST_SmallJSON(b *testing.B) {
	benchmarkBaseAPIClientPOST(
		b,
		map[string]any{"name": "item", "status": "active"},
		`{"code":200,"status":"success","data":{"id":1}}`,
	)
}

func BenchmarkBaseAPIClient_POST_MediumJSON(b *testing.B) {
	benchmarkBaseAPIClientPOST(
		b,
		map[string]any{"name": "item", "description": strings.Repeat("x", 4096)},
		`{"code":200,"status":"success","data":{"id":1}}`,
	)
}
