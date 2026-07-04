package properties

import (
	"testing"
	"time"
)

func TestTransportProperties_Defaults(t *testing.T) {
	props := NewDefaultTransportProperties()

	if props.MaxIdleConns != 200 {
		t.Fatalf("expected MaxIdleConns 200, got %d", props.MaxIdleConns)
	}
	if props.MaxIdleConnsPerHost != 100 {
		t.Fatalf("expected MaxIdleConnsPerHost 100, got %d", props.MaxIdleConnsPerHost)
	}
	if props.MaxConnsPerHost != 200 {
		t.Fatalf("expected MaxConnsPerHost 200, got %d", props.MaxConnsPerHost)
	}
	if props.IdleConnTimeout != 90*time.Second {
		t.Fatalf("expected IdleConnTimeout 90s, got %v", props.IdleConnTimeout)
	}
	if props.ResponseHeaderTimeout != 15*time.Second {
		t.Fatalf("expected ResponseHeaderTimeout 15s, got %v", props.ResponseHeaderTimeout)
	}
}

func TestTransportProperties_Prefix(t *testing.T) {
	if prefix := (TransportProperties{}).Prefix(); prefix != "app.transport" {
		t.Fatalf("expected prefix app.transport, got %s", prefix)
	}
}
