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
	// Setup Hub
	logger := zap.NewNop()
	hub := NewHub(logger)

	// Setup HTTP Server
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

	// Convert http URL to ws URL
	url := "ws" + server.URL[4:]

	concurrencyLevels := []int{100, 1000, 2000, 5000}

	for _, connections := range concurrencyLevels {
		t.Run(fmt.Sprintf("%d_connections", connections), func(t *testing.T) {
			var wg sync.WaitGroup
			var connectedCount int32
			var errorCount int32

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

					// Keep connection open for a bit
					// In a real load test, we might want to hold these longer
					// For this test, we just want to see if we can establish them

					// Read loop to handle ping/pong and close
					go func() {
						defer c.Close()
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

			// Cleanup for next run (though we are reusing the same hub, so connections accumulate)
			// Ideally we should close connections, but for this test we just want to see if it handles the load.
			// If we want to test "concurrent" as in "active at the same time", we should keep them open.
			// If we want to test "requests per second", that's different.
			// The user asked "how many concurrent websocket requests".

			// To properly clean up, we would need to close the clients we created.
			// But since we are inside a loop, the previous connections are still active in the background (in the read loop).
			// So the hub count will increase.
		})
	}
}

func BenchmarkWebsocketBroadcast(b *testing.B) {
	// Setup Hub
	logger := zap.NewNop()
	hub := NewHub(logger)

	// Setup HTTP Server
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
