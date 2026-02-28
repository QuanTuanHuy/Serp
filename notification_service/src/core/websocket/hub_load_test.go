/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package websocket

import (
	"fmt"
	"net/http"
	"net/http/httptest"
	"sync"
	"sync/atomic"
	"testing"
	"time"

	"github.com/gorilla/websocket"
	"go.uber.org/zap"
)

func TestConcurrentConnections(t *testing.T) {
	concurrencyLevels := []int{100, 1000, 2000, 5000}

	for _, connections := range concurrencyLevels {
		t.Run(fmt.Sprintf("%d_connections", connections), func(t *testing.T) {
			// Fresh hub and server per sub-test to avoid connection accumulation
			logger := zap.NewNop()
			hub := NewHub(logger)
			hub.Start()
			defer hub.Stop()

			upgrader := websocket.Upgrader{
				CheckOrigin: func(r *http.Request) bool { return true },
			}

			server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
				conn, err := upgrader.Upgrade(w, r, nil)
				if err != nil {
					return
				}
				client := NewClient(hub, conn, 1, 1, "test-agent", "127.0.0.1", logger)
				hub.RegisterClient(client)
				go client.WritePump()
				go client.ReadPump()
			}))
			defer server.Close()

			url := "ws" + server.URL[4:]

			var wg sync.WaitGroup
			var connectedCount int32
			var errorCount int32

			// Track client connections for cleanup
			clientConns := make([]*websocket.Conn, 0, connections)
			var connMu sync.Mutex

			start := time.Now()

			for i := 0; i < connections; i++ {
				// Add a small delay to avoid overwhelming the OS network stack
				if i%10 == 0 {
					time.Sleep(1 * time.Millisecond)
				}
				wg.Add(1)
				go func() {
					defer wg.Done()
					c, _, err := websocket.DefaultDialer.Dial(url, nil)
					if err != nil {
						if atomic.AddInt32(&errorCount, 1) == 1 {
							t.Logf("First connection error: %v", err)
						}
						return
					}
					atomic.AddInt32(&connectedCount, 1)

					connMu.Lock()
					clientConns = append(clientConns, c)
					connMu.Unlock()

					// Read loop to handle ping/pong and close
					go func() {
						for {
							_, _, err := c.ReadMessage()
							if err != nil {
								return
							}
						}
					}()
				}()
			}

			// Wait for all connection attempts
			wg.Wait()

			// Give the hub a moment to register everyone
			time.Sleep(100 * time.Millisecond)

			duration := time.Since(start)

			hubCount := hub.GetTotalConnections()

			t.Logf("Level: %d, Connected: %d, Errors: %d, Hub Count: %d, Time: %v",
				connections, connectedCount, errorCount, hubCount, duration)

			if int64(connectedCount) != hubCount {
				t.Logf("Warning: Client connected count (%d) differs from Hub count (%d)", connectedCount, hubCount)
			}

			// Cleanup: close all client connections
			connMu.Lock()
			for _, c := range clientConns {
				c.Close()
			}
			connMu.Unlock()

			// Give the hub time to process unregistrations
			time.Sleep(200 * time.Millisecond)
		})
	}
}

func BenchmarkWebsocketBroadcast(b *testing.B) {
	logger := zap.NewNop()
	hub := NewHub(logger)
	hub.Start()
	defer hub.Stop()

	upgrader := websocket.Upgrader{
		CheckOrigin: func(r *http.Request) bool { return true },
	}

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		conn, err := upgrader.Upgrade(w, r, nil)
		if err != nil {
			return
		}
		client := NewClient(hub, conn, 1, 1, "test-agent", "127.0.0.1", logger)
		hub.RegisterClient(client)
		go client.WritePump()
		go client.ReadPump()
	}))
	defer server.Close()

	url := "ws" + server.URL[4:]

	// Connect 1000 clients
	numClients := 1000
	clients := make([]*websocket.Conn, numClients)

	for i := 0; i < numClients; i++ {
		c, _, err := websocket.DefaultDialer.Dial(url, nil)
		if err != nil {
			b.Fatal(err)
		}
		clients[i] = c

		// Consume messages
		go func(conn *websocket.Conn) {
			for {
				_, _, err := conn.ReadMessage()
				if err != nil {
					return
				}
			}
		}(c)
	}

	// Wait for registration
	time.Sleep(1 * time.Second)

	b.ResetTimer()

	msg := []byte(`{"type":"NOTIFICATION","payload":{"text":"hello"}}`)

	for i := 0; i < b.N; i++ {
		hub.BroadcastToAll(msg)
	}

	// Cleanup
	for _, c := range clients {
		c.Close()
	}
}
