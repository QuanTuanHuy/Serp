/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package common

import (
	"bufio"
	"crypto/sha1"
	"encoding/base64"
	"fmt"
	"net"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strings"
	"sync"
	"sync/atomic"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/kernel/properties"
)

func defaultWSResilienceProps() *properties.ResilienceProperties {
	p := properties.NewDefaultResilienceProperties()
	return &p
}

func websocketAcceptKey(key string) string {
	h := sha1.New()
	h.Write([]byte(key))
	h.Write([]byte("258EAFA5-E914-47DA-95CA-C5AB0DC85B11"))
	return base64.StdEncoding.EncodeToString(h.Sum(nil))
}

func newMockWebSocketUpstream(tb testing.TB, expectedPath string, hits *int32) *httptest.Server {
	tb.Helper()

	return httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if expectedPath != "" && r.URL.Path != expectedPath {
			http.Error(w, "unexpected path", http.StatusNotFound)
			return
		}

		if !strings.EqualFold(r.Header.Get("Upgrade"), "websocket") ||
			!strings.Contains(strings.ToLower(r.Header.Get("Connection")), "upgrade") {
			http.Error(w, "missing websocket upgrade headers", http.StatusBadRequest)
			return
		}

		atomic.AddInt32(hits, 1)

		hijacker, ok := w.(http.Hijacker)
		if !ok {
			http.Error(w, "hijacker not supported", http.StatusInternalServerError)
			return
		}

		conn, buf, err := hijacker.Hijack()
		if err != nil {
			return
		}
		defer conn.Close()

		accept := websocketAcceptKey(r.Header.Get("Sec-WebSocket-Key"))
		_, _ = fmt.Fprintf(buf, "HTTP/1.1 101 Switching Protocols\r\n")
		_, _ = fmt.Fprintf(buf, "Upgrade: websocket\r\n")
		_, _ = fmt.Fprintf(buf, "Connection: Upgrade\r\n")
		_, _ = fmt.Fprintf(buf, "Sec-WebSocket-Accept: %s\r\n", accept)
		_, _ = fmt.Fprintf(buf, "\r\n")
		_ = buf.Flush()
	}))
}

func rawWebSocketHandshake(serverURL, path string, extraHeaders map[string]string) (net.Conn, error) {
	u, err := url.Parse(serverURL)
	if err != nil {
		return nil, fmt.Errorf("parse gateway URL: %w", err)
	}

	conn, err := net.DialTimeout("tcp", u.Host, 5*time.Second)
	if err != nil {
		return nil, fmt.Errorf("dial gateway: %w", err)
	}

	if err := conn.SetDeadline(time.Now().Add(5 * time.Second)); err != nil {
		conn.Close()
		return nil, fmt.Errorf("set deadline: %w", err)
	}

	secKey := "dGhlIHNhbXBsZSBub25jZQ=="
	request := fmt.Sprintf(
		"GET %s HTTP/1.1\r\n"+
			"Host: %s\r\n"+
			"Upgrade: websocket\r\n"+
			"Connection: Upgrade\r\n"+
			"Sec-WebSocket-Version: 13\r\n"+
			"Sec-WebSocket-Key: %s\r\n",
		path,
		u.Host,
		secKey,
	)

	for k, v := range extraHeaders {
		request += fmt.Sprintf("%s: %s\r\n", k, v)
	}
	request += "\r\n"

	if _, err := conn.Write([]byte(request)); err != nil {
		conn.Close()
		return nil, fmt.Errorf("write handshake request: %w", err)
	}

	reader := bufio.NewReader(conn)
	statusLine, err := reader.ReadString('\n')
	if err != nil {
		conn.Close()
		return nil, fmt.Errorf("read status line: %w", err)
	}
	if !strings.Contains(statusLine, " 101 ") {
		conn.Close()
		return nil, fmt.Errorf("unexpected status line: %s", strings.TrimSpace(statusLine))
	}

	headers := map[string]string{}
	for {
		line, err := reader.ReadString('\n')
		if err != nil {
			conn.Close()
			return nil, fmt.Errorf("read response header: %w", err)
		}

		if line == "\r\n" {
			break
		}

		parts := strings.SplitN(line, ":", 2)
		if len(parts) != 2 {
			continue
		}
		headers[strings.ToLower(strings.TrimSpace(parts[0]))] = strings.TrimSpace(parts[1])
	}

	if !strings.EqualFold(headers["upgrade"], "websocket") {
		conn.Close()
		return nil, fmt.Errorf("unexpected upgrade header: %q", headers["upgrade"])
	}

	if !strings.Contains(strings.ToLower(headers["connection"]), "upgrade") {
		conn.Close()
		return nil, fmt.Errorf("unexpected connection header: %q", headers["connection"])
	}

	expectedAccept := websocketAcceptKey(secKey)
	if headers["sec-websocket-accept"] != expectedAccept {
		conn.Close()
		return nil, fmt.Errorf("unexpected Sec-WebSocket-Accept header: %q", headers["sec-websocket-accept"])
	}

	if err := conn.SetDeadline(time.Time{}); err != nil {
		conn.Close()
		return nil, fmt.Errorf("clear deadline: %w", err)
	}

	return conn, nil
}

func newDiscussGatewayForWebSocketTests(tb testing.TB) (*httptest.Server, *int32, func()) {
	tb.Helper()

	hits := new(int32)
	upstream := newMockWebSocketUpstream(tb, "/discuss/ws/discuss", hits)

	u, err := url.Parse(upstream.URL)
	if err != nil {
		upstream.Close()
		tb.Fatalf("parse upstream URL: %v", err)
	}

	controller := NewWebSocketProxyController(
		&properties.ExternalServiceProperties{
			DiscussService: properties.ServiceProperty{
				Host:          u.Hostname(),
				Port:          u.Port(),
				WebSocketPath: "/discuss/ws/discuss",
			},
		},
		defaultWSResilienceProps(),
	)

	r := gin.New()
	r.GET("/ws/discuss", controller.ProxyHandler("discuss"))

	gateway := httptest.NewServer(r)
	cleanup := func() {
		gateway.Close()
		upstream.Close()
	}

	return gateway, hits, cleanup
}

func TestWebSocketProxyController_Discuss_UpgradeAndPathRewrite(t *testing.T) {
	gin.SetMode(gin.TestMode)

	gateway, hits, cleanup := newDiscussGatewayForWebSocketTests(t)
	defer cleanup()

	conn, err := rawWebSocketHandshake(gateway.URL, "/ws/discuss", map[string]string{
		"Authorization": "Bearer test-token",
	})
	if err != nil {
		t.Fatalf("websocket handshake failed: %v", err)
	}
	_ = conn.Close()

	if got := atomic.LoadInt32(hits); got != 1 {
		t.Fatalf("expected 1 upstream websocket upgrade, got %d", got)
	}
}

func TestWebSocketProxyController_Load_ConcurrentUpgrades(t *testing.T) {
	if testing.Short() {
		t.Skip("skipping websocket load test in short mode")
	}

	gin.SetMode(gin.TestMode)

	gateway, hits, cleanup := newDiscussGatewayForWebSocketTests(t)
	defer cleanup()

	const totalConnections = 300
	const workerCount = 30

	start := time.Now()

	jobs := make(chan struct{}, totalConnections)
	var wg sync.WaitGroup
	var success int32
	var failed int32
	var firstErr string
	var firstErrOnce sync.Once

	for i := 0; i < workerCount; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for range jobs {
				conn, err := rawWebSocketHandshake(gateway.URL, "/ws/discuss", nil)
				if err != nil {
					atomic.AddInt32(&failed, 1)
					firstErrOnce.Do(func() {
						firstErr = err.Error()
					})
					continue
				}

				_ = conn.Close()
				atomic.AddInt32(&success, 1)
			}
		}()
	}

	for i := 0; i < totalConnections; i++ {
		jobs <- struct{}{}
	}
	close(jobs)
	wg.Wait()

	duration := time.Since(start)
	t.Logf(
		"websocket proxy load: total=%d success=%d failed=%d duration=%v req/s=%.2f",
		totalConnections,
		success,
		failed,
		duration,
		float64(totalConnections)/duration.Seconds(),
	)

	if failed > 0 {
		t.Fatalf("expected all websocket upgrades to succeed, failed=%d, firstError=%s", failed, firstErr)
	}

	if success != totalConnections {
		t.Fatalf("expected %d successful upgrades, got %d", totalConnections, success)
	}

	if got := atomic.LoadInt32(hits); got != success {
		t.Fatalf("expected upstream hits=%d, got %d", success, got)
	}
}

func BenchmarkWebSocketProxyController_Discuss_UpgradeParallel(b *testing.B) {
	gin.SetMode(gin.TestMode)

	gateway, _, cleanup := newDiscussGatewayForWebSocketTests(b)
	defer cleanup()

	var failed int64

	b.ReportAllocs()
	b.ResetTimer()

	b.RunParallel(func(pb *testing.PB) {
		for pb.Next() {
			conn, err := rawWebSocketHandshake(gateway.URL, "/ws/discuss", nil)
			if err != nil {
				atomic.AddInt64(&failed, 1)
				continue
			}
			_ = conn.Close()
		}
	})

	b.StopTimer()

	if failed > 0 {
		b.Fatalf("benchmark had %d failed websocket upgrades", failed)
	}
}
