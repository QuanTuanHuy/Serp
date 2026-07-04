/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package common

import (
	"bytes"
	"io"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strings"
	"sync/atomic"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/kernel/properties"
)

func defaultResilienceProps() *properties.ResilienceProperties {
	p := properties.NewDefaultResilienceProperties()
	return &p
}

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

	controller := NewGenericProxyController(
		&properties.ExternalServiceProperties{
			CrmService: properties.ServiceProperty{Host: host, Port: port},
		},
		defaultResilienceProps(),
		properties.NewDefaultTransportProperties(),
	)

	r := gin.New()
	r.Any("/crm/api/v1/*proxyPath", controller.ProxyHandler("crm"))
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

func TestGenericProxyController_FirstMile_RewritePathAndForwardHeaders(t *testing.T) {
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

	controller := NewGenericProxyController(
		&properties.ExternalServiceProperties{
			FirstMileService: properties.ServiceProperty{Host: host, Port: port},
		},
		defaultResilienceProps(),
		properties.NewDefaultTransportProperties(),
	)

	r := gin.New()
	r.Any("/first-mile/api/v1/*proxyPath", controller.ProxyHandler("first-mile"))
	gateway := httptest.NewServer(r)
	defer gateway.Close()

	req, err := http.NewRequest(http.MethodGet, gateway.URL+"/first-mile/api/v1/orders?x=1", nil)
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
	if gotPath != "/api/v1/orders" {
		t.Fatalf("expected rewritten path %q, got %q", "/api/v1/orders", gotPath)
	}
	if gotQuery != "x=1" {
		t.Fatalf("expected query %q, got %q", "x=1", gotQuery)
	}
	if gotAuth != "Bearer test" {
		t.Fatalf("expected auth header %q, got %q", "Bearer test", gotAuth)
	}
}

func TestGenericProxyController_SecondMile_RewritePathAndForwardHeaders(t *testing.T) {
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

	controller := NewGenericProxyController(
		&properties.ExternalServiceProperties{
			SecondMileService: properties.ServiceProperty{Host: host, Port: port},
		},
		defaultResilienceProps(),
		properties.NewDefaultTransportProperties(),
	)

	r := gin.New()
	r.Any("/second-mile/api/v1/*proxyPath", controller.ProxyHandler("second-mile"))
	gateway := httptest.NewServer(r)
	defer gateway.Close()

	req, err := http.NewRequest(http.MethodGet, gateway.URL+"/second-mile/api/v1/orders?x=1", nil)
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
	if gotPath != "/api/v1/orders" {
		t.Fatalf("expected rewritten path %q, got %q", "/api/v1/orders", gotPath)
	}
	if gotQuery != "x=1" {
		t.Fatalf("expected query %q, got %q", "x=1", gotQuery)
	}
	if gotAuth != "Bearer test" {
		t.Fatalf("expected auth header %q, got %q", "Bearer test", gotAuth)
	}
}

func TestGenericProxyController_CRM_CircuitBreakerCountsLogicalRequestsAfterRetries(t *testing.T) {
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

	resProps := defaultResilienceProps()
	resProps.InitialDelay = time.Millisecond
	resProps.MaxDelay = time.Millisecond

	controller := NewGenericProxyController(
		&properties.ExternalServiceProperties{
			CrmService: properties.ServiceProperty{Host: host, Port: port},
		},
		resProps,
		properties.NewDefaultTransportProperties(),
	)

	r := gin.New()
	r.Any("/crm/api/v1/*proxyPath", controller.ProxyHandler("crm"))
	gateway := httptest.NewServer(r)
	defer gateway.Close()

	expectedHitsPerRequest := int32(resProps.MaxRetries + 1)

	for i := 1; i <= int(resProps.ConsecutiveFailures); i++ {
		resp, err := http.Get(gateway.URL + "/crm/api/v1/leads")
		if err != nil {
			t.Fatalf("request %d: %v", i, err)
		}
		body, _ := io.ReadAll(resp.Body)
		resp.Body.Close()
		if resp.StatusCode != http.StatusInternalServerError {
			t.Fatalf("expected request %d status %d, got %d. body=%s",
				i, http.StatusInternalServerError, resp.StatusCode, string(body))
		}

		expectedHits := int32(i) * expectedHitsPerRequest
		if hits := atomic.LoadInt32(&upstreamHits); hits != expectedHits {
			t.Fatalf("expected %d upstream hits after request %d, got %d", expectedHits, i, hits)
		}
	}

	hitsBeforeOpenRequest := atomic.LoadInt32(&upstreamHits)

	resp, err := http.Get(gateway.URL + "/crm/api/v1/leads")
	if err != nil {
		t.Fatalf("open circuit request: %v", err)
	}
	body, _ := io.ReadAll(resp.Body)
	resp.Body.Close()
	if resp.StatusCode != http.StatusServiceUnavailable {
		t.Fatalf("expected open circuit status %d, got %d. body=%s",
			http.StatusServiceUnavailable, resp.StatusCode, string(body))
	}

	if hits := atomic.LoadInt32(&upstreamHits); hits != hitsBeforeOpenRequest {
		t.Fatalf("expected no upstream hit after CB open, got %d -> %d", hitsBeforeOpenRequest, hits)
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

	controller := NewGenericProxyController(
		&properties.ExternalServiceProperties{
			CrmService: properties.ServiceProperty{Host: host, Port: port},
		},
		defaultResilienceProps(),
		properties.NewDefaultTransportProperties(),
	)

	r := gin.New()
	r.Any("/crm/api/v1/*proxyPath", controller.ProxyHandler("crm"))
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

func newBenchmarkGenericProxyGateway(b *testing.B, responseBody []byte) (*httptest.Server, *http.Client) {
	b.Helper()
	gin.SetMode(gin.TestMode)

	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write(responseBody)
	}))

	u, err := url.Parse(upstream.URL)
	if err != nil {
		upstream.Close()
		b.Fatalf("parse upstream URL: %v", err)
	}

	resProps := defaultResilienceProps()
	resProps.MaxRetries = 0

	controller := NewGenericProxyController(
		&properties.ExternalServiceProperties{
			CrmService: properties.ServiceProperty{Host: u.Hostname(), Port: u.Port()},
		},
		resProps,
		properties.NewDefaultTransportProperties(),
	)

	r := gin.New()
	r.Any("/crm/api/v1/*proxyPath", controller.ProxyHandler("crm"))

	gateway := httptest.NewServer(r)
	b.Cleanup(func() {
		gateway.Close()
		upstream.Close()
	})

	return gateway, &http.Client{}
}

func benchmarkGenericProxyGET(b *testing.B, responseBody []byte) {
	gateway, client := newBenchmarkGenericProxyGateway(b, responseBody)

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		resp, err := client.Get(gateway.URL + "/crm/api/v1/leads?stage=open")
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

func benchmarkGenericProxyPOST(b *testing.B, requestBody []byte, responseBody []byte) {
	gateway, client := newBenchmarkGenericProxyGateway(b, responseBody)

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		req, err := http.NewRequest(
			http.MethodPost,
			gateway.URL+"/crm/api/v1/leads",
			bytes.NewReader(requestBody),
		)
		if err != nil {
			b.Fatalf("new request: %v", err)
		}
		req.Header.Set("Content-Type", "application/json")

		resp, err := client.Do(req)
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

	controller := NewGenericProxyController(
		&properties.ExternalServiceProperties{
			CrmService: properties.ServiceProperty{Host: host, Port: port},
		},
		defaultResilienceProps(),
		properties.NewDefaultTransportProperties(),
	)

	r := gin.New()
	r.Any("/crm/api/v1/*proxyPath", controller.ProxyHandler("crm"))
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

func BenchmarkGenericProxyController_CRM_GET_SmallJSON(b *testing.B) {
	benchmarkGenericProxyGET(b, []byte(`{"code":200,"status":"success","data":{"id":1}}`))
}

func BenchmarkGenericProxyController_CRM_GET_MediumJSON(b *testing.B) {
	item := `{"id":1,"name":"lead","stage":"open","ownerId":1001,"amount":123456789}`
	body := []byte(`{"code":200,"status":"success","data":[` + strings.Repeat(item+",", 49) + item + `]}`)
	benchmarkGenericProxyGET(b, body)
}

func BenchmarkGenericProxyController_CRM_POST_SmallJSON(b *testing.B) {
	benchmarkGenericProxyPOST(
		b,
		[]byte(`{"name":"lead","stage":"open"}`),
		[]byte(`{"code":200,"status":"success","data":{"id":1}}`),
	)
}

func BenchmarkGenericProxyController_CRM_POST_MediumJSON(b *testing.B) {
	requestBody := []byte(`{"name":"lead","notes":"` + strings.Repeat("x", 4096) + `"}`)
	responseBody := []byte(`{"code":200,"status":"success","data":{"id":1}}`)
	benchmarkGenericProxyPOST(b, requestBody, responseBody)
}
