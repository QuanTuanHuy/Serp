package utils

import (
	"net/http"
	"testing"
	"time"

	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/sony/gobreaker/v2"
)

func TestNewUpstreamTransport_UsesProperties(t *testing.T) {
	props := &properties.TransportProperties{
		MaxIdleConns:          11,
		MaxIdleConnsPerHost:   12,
		MaxConnsPerHost:       13,
		IdleConnTimeout:       14 * time.Second,
		DialTimeout:           15 * time.Second,
		TLSHandshakeTimeout:   16 * time.Second,
		ResponseHeaderTimeout: 17 * time.Second,
		ExpectContinueTimeout: 18 * time.Second,
	}

	transport := NewUpstreamTransport(props)

	if transport.MaxIdleConns != 11 {
		t.Fatalf("expected MaxIdleConns 11, got %d", transport.MaxIdleConns)
	}
	if transport.MaxIdleConnsPerHost != 12 {
		t.Fatalf("expected MaxIdleConnsPerHost 12, got %d", transport.MaxIdleConnsPerHost)
	}
	if transport.MaxConnsPerHost != 13 {
		t.Fatalf("expected MaxConnsPerHost 13, got %d", transport.MaxConnsPerHost)
	}
	if transport.ResponseHeaderTimeout != 17*time.Second {
		t.Fatalf("expected ResponseHeaderTimeout 17s, got %v", transport.ResponseHeaderTimeout)
	}
}

func TestNewResilientTransportWithBase_UsesProvidedBaseTransport(t *testing.T) {
	base := http.DefaultTransport
	cb := gobreaker.NewCircuitBreaker[*http.Response](gobreaker.Settings{Name: "test"})

	transport := NewResilientTransportWithBase(base, cb, 0, time.Millisecond, time.Millisecond)
	rt, ok := transport.transport.(*CircuitBreakerTransport)
	if !ok {
		t.Fatalf("expected CircuitBreakerTransport, got %T", transport.transport)
	}
	retry, ok := rt.Base.(*RetryTransport)
	if !ok {
		t.Fatalf("expected RetryTransport, got %T", rt.Base)
	}
	if retry.Next != base {
		t.Fatalf("expected retry transport to use provided base transport")
	}
}
