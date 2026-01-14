/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package common

import (
	"io"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strings"
	"sync/atomic"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/kernel/properties"
)

func TestGenericProxyController_CRM_RewritePathAndForwardHeaders(t *testing.T) {
	gin.SetMode(gin.TestMode)

	var gotPath string
	var gotQuery string
	var gotAuth string

	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotPath = r.URL.Path
		gotQuery = r.URL.RawQuery
		gotAuth = r.Header.Get("Authorization")
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("ok"))
	}))
	defer upstream.Close()

	u, err := url.Parse(upstream.URL)
	if err != nil {
		t.Fatalf("parse upstream url: %v", err)
	}
	host := u.Hostname()
	port := u.Port()
	if port == "" {
		t.Fatalf("expected upstream port")
	}

	controller := NewGenericProxyController(&properties.ExternalServiceProperties{
		CrmService: properties.ServiceProperty{Host: host, Port: port},
	})

	r := gin.New()
	r.Any("/crm/api/v1/*proxyPath", controller.ProxyToCRM)
	gateway := httptest.NewServer(r)
	defer gateway.Close()

	req, err := http.NewRequest(http.MethodGet, gateway.URL+"/crm/api/v1/leads?x=1", nil)
	if err != nil {
		t.Fatalf("new request: %v", err)
	}
	req.Header.Set("Authorization", "Bearer test")
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		t.Fatalf("do request: %v", err)
	}
	defer resp.Body.Close()
	_, _ = io.ReadAll(resp.Body)

	if resp.StatusCode != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, resp.StatusCode)
	}
	if gotPath != "/crm/api/v1/leads" {
		t.Fatalf("expected rewritten path %q, got %q", "/crm/api/v1/leads", gotPath)
	}
	if gotQuery != "x=1" {
		t.Fatalf("expected query %q, got %q", "x=1", gotQuery)
	}
	if gotAuth != "Bearer test" {
		t.Fatalf("expected auth header %q, got %q", "Bearer test", gotAuth)
	}
}

func TestGenericProxyController_CRM_CircuitBreakerOpensAfter5xxWithRetries(t *testing.T) {
	gin.SetMode(gin.TestMode)

	var upstreamHits int32

	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		atomic.AddInt32(&upstreamHits, 1)
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte("boom"))
	}))
	defer upstream.Close()

	u, err := url.Parse(upstream.URL)
	if err != nil {
		t.Fatalf("parse upstream url: %v", err)
	}
	host := u.Hostname()
	port := u.Port()

	controller := NewGenericProxyController(&properties.ExternalServiceProperties{
		CrmService: properties.ServiceProperty{Host: host, Port: port},
	})

	r := gin.New()
	r.Any("/crm/api/v1/*proxyPath", controller.ProxyToCRM)
	gateway := httptest.NewServer(r)
	defer gateway.Close()

	// First request: upstream returns 500; due to retries, CB likely accumulates failures quickly.
	resp1, err := http.Get(gateway.URL + "/crm/api/v1/leads")
	if err != nil {
		t.Fatalf("first request: %v", err)
	}
	_, _ = io.ReadAll(resp1.Body)
	resp1.Body.Close()
	if resp1.StatusCode != http.StatusInternalServerError {
		t.Fatalf("expected first status %d, got %d", http.StatusInternalServerError, resp1.StatusCode)
	}
	firstHits := atomic.LoadInt32(&upstreamHits)
	if firstHits <= 0 {
		t.Fatalf("expected upstream to be called at least once")
	}

	// Circuit opens after 5 consecutive failures. With maxRetries=3, the first request
	// typically causes 4 upstream hits (1 + 3 retries). The second request may still
	// hit upstream once (the 5th failure) before the circuit opens.
	resp2, err := http.Get(gateway.URL + "/crm/api/v1/leads")
	if err != nil {
		t.Fatalf("second request: %v", err)
	}
	_, _ = io.ReadAll(resp2.Body)
	resp2.Body.Close()

	secondHits := atomic.LoadInt32(&upstreamHits)
	if secondHits < firstHits || secondHits > firstHits+1 {
		t.Fatalf("expected upstream hits to stay same or increase by 1 on second request, got %d -> %d", firstHits, secondHits)
	}

	// Third request must be blocked by open circuit breaker and return 503 from ErrorHandler.
	resp3, err := http.Get(gateway.URL + "/crm/api/v1/leads")
	if err != nil {
		t.Fatalf("third request: %v", err)
	}
	body3, _ := io.ReadAll(resp3.Body)
	resp3.Body.Close()
	if resp3.StatusCode != http.StatusServiceUnavailable {
		t.Fatalf("expected third status %d, got %d. body=%s", http.StatusServiceUnavailable, resp3.StatusCode, string(body3))
	}

	thirdHits := atomic.LoadInt32(&upstreamHits)
	if thirdHits != secondHits {
		t.Fatalf("expected no upstream hit after CB open on third request, got %d -> %d", secondHits, thirdHits)
	}
}

func TestGenericProxyController_CRM_POSTDoesNotRetry(t *testing.T) {
	gin.SetMode(gin.TestMode)

	var upstreamHits int32

	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		atomic.AddInt32(&upstreamHits, 1)
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte("boom"))
	}))
	defer upstream.Close()

	u, err := url.Parse(upstream.URL)
	if err != nil {
		t.Fatalf("parse upstream url: %v", err)
	}
	host := u.Hostname()
	port := u.Port()

	controller := NewGenericProxyController(&properties.ExternalServiceProperties{
		CrmService: properties.ServiceProperty{Host: host, Port: port},
	})

	r := gin.New()
	r.Any("/crm/api/v1/*proxyPath", controller.ProxyToCRM)
	gateway := httptest.NewServer(r)
	defer gateway.Close()

	req, err := http.NewRequest(http.MethodPost, gateway.URL+"/crm/api/v1/leads", strings.NewReader(`{"a":1}`))
	if err != nil {
		t.Fatalf("new request: %v", err)
	}
	req.Header.Set("Content-Type", "application/json")
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		t.Fatalf("do request: %v", err)
	}
	_, _ = io.ReadAll(resp.Body)
	resp.Body.Close()
	if resp.StatusCode != http.StatusInternalServerError {
		t.Fatalf("expected status %d, got %d", http.StatusInternalServerError, resp.StatusCode)
	}

	if hits := atomic.LoadInt32(&upstreamHits); hits != 1 {
		t.Fatalf("expected 1 upstream hit for POST (no retry), got %d", hits)
	}
}

func BenchmarkGenericProxyController_CRM_GET_200(b *testing.B) {
	gin.SetMode(gin.TestMode)

	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("ok"))
	}))
	defer upstream.Close()

	u, _ := url.Parse(upstream.URL)
	host := u.Hostname()
	port := u.Port()

	controller := NewGenericProxyController(&properties.ExternalServiceProperties{
		CrmService: properties.ServiceProperty{Host: host, Port: port},
	})

	r := gin.New()
	r.Any("/crm/api/v1/*proxyPath", controller.ProxyToCRM)
	gateway := httptest.NewServer(r)
	defer gateway.Close()

	client := &http.Client{}

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		resp, err := client.Get(gateway.URL + "/crm/api/v1/leads")
		if err != nil {
			b.Fatalf("request failed: %v", err)
		}
		_, _ = io.ReadAll(resp.Body)
		resp.Body.Close()
		if resp.StatusCode != http.StatusOK {
			b.Fatalf("expected %d, got %d", http.StatusOK, resp.StatusCode)
		}
	}
}
