# WebSocket Testing Guide

**Module:** notification_service/websocket  
**Ngày tạo:** 2025-12-13

---

## 1. Unit Tests

### 1.1. Hub Tests

```go
// infrastructure/websocket/hub_test.go
package websocket

import (
    "testing"
    "time"
    
    "github.com/stretchr/testify/assert"
    "github.com/stretchr/testify/require"
    "go.uber.org/zap"
)

func TestHub_RegisterClient(t *testing.T) {
    logger := zap.NewNop()
    hub := NewHub(logger)
    
    go hub.Run()
    defer hub.Stop()
    
    // Create mock client
    client := &Client{
        id:       "test-client-1",
        userID:   1,
        tenantID: 1,
        send:     make(chan []byte, 256),
    }
    
    // Register
    hub.Register(client)
    
    // Wait for processing
    time.Sleep(10 * time.Millisecond)
    
    // Verify
    assert.True(t, hub.IsUserConnected(1))
    assert.Equal(t, 1, hub.GetConnectedUserCount())
}

func TestHub_MultipleDevices(t *testing.T) {
    logger := zap.NewNop()
    hub := NewHub(logger)
    
    go hub.Run()
    defer hub.Stop()
    
    // Same user, different devices
    client1 := &Client{
        id:       "device-1",
        userID:   1,
        tenantID: 1,
        send:     make(chan []byte, 256),
    }
    client2 := &Client{
        id:       "device-2",
        userID:   1,
        tenantID: 1,
        send:     make(chan []byte, 256),
    }
    
    hub.Register(client1)
    hub.Register(client2)
    
    time.Sleep(10 * time.Millisecond)
    
    // Should have 2 connections but 1 unique user
    assert.Equal(t, int64(2), hub.GetTotalConnections())
    assert.Equal(t, 1, hub.GetConnectedUserCount())
}

func TestHub_SendToUser(t *testing.T) {
    logger := zap.NewNop()
    hub := NewHub(logger)
    
    go hub.Run()
    defer hub.Stop()
    
    client := &Client{
        id:       "test-client",
        userID:   1,
        tenantID: 1,
        send:     make(chan []byte, 256),
    }
    
    hub.Register(client)
    time.Sleep(10 * time.Millisecond)
    
    // Send message
    testMsg := []byte(`{"type":"TEST"}`)
    hub.SendToUser(1, testMsg)
    
    // Verify delivery
    select {
    case msg := <-client.send:
        assert.Equal(t, testMsg, msg)
    case <-time.After(100 * time.Millisecond):
        t.Fatal("Message not received")
    }
}

func TestHub_UnregisterClient(t *testing.T) {
    logger := zap.NewNop()
    hub := NewHub(logger)
    
    go hub.Run()
    defer hub.Stop()
    
    client := &Client{
        id:       "test-client",
        userID:   1,
        tenantID: 1,
        send:     make(chan []byte, 256),
    }
    
    hub.Register(client)
    time.Sleep(10 * time.Millisecond)
    
    assert.True(t, hub.IsUserConnected(1))
    
    hub.Unregister(client)
    time.Sleep(10 * time.Millisecond)
    
    assert.False(t, hub.IsUserConnected(1))
}
```

### 1.2. Message Builder Tests

```go
// infrastructure/websocket/message_builder_test.go
package websocket

import (
    "encoding/json"
    "testing"
    
    "github.com/stretchr/testify/assert"
    "github.com/stretchr/testify/require"
)

func TestMessageBuilder_NewNotification(t *testing.T) {
    builder := NewMessageBuilder()
    
    payload := &NotificationPayload{
        ID:       123,
        Title:    "Test Notification",
        Message:  "This is a test",
        Type:     "INFO",
        Category: "TASK",
    }
    
    data, err := builder.NewNotification(payload)
    require.NoError(t, err)
    
    var msg WSMessage
    err = json.Unmarshal(data, &msg)
    require.NoError(t, err)
    
    assert.Equal(t, "NEW_NOTIFICATION", msg.Type)
    assert.NotEmpty(t, msg.MessageID)
    assert.Greater(t, msg.Timestamp, int64(0))
}

func TestMessageBuilder_UnreadCountUpdate(t *testing.T) {
    builder := NewMessageBuilder()
    
    payload := &UnreadCountPayload{
        TotalUnread: 5,
        ByCategory: map[string]int64{
            "TASK": 3,
            "CRM":  2,
        },
        HasUrgent: true,
    }
    
    data, err := builder.UnreadCountUpdate(payload)
    require.NoError(t, err)
    
    var msg WSMessage
    err = json.Unmarshal(data, &msg)
    require.NoError(t, err)
    
    assert.Equal(t, "UNREAD_COUNT_UPDATE", msg.Type)
}
```

---

## 2. Integration Tests

### 2.1. WebSocket Connection Test

```go
// ui/controller/websocket_controller_test.go
package controller

import (
    "net/http"
    "net/http/httptest"
    "strings"
    "testing"
    "time"
    
    "github.com/gin-gonic/gin"
    "github.com/gorilla/websocket"
    "github.com/stretchr/testify/assert"
    "github.com/stretchr/testify/require"
)

func TestWebSocket_Connection(t *testing.T) {
    // Setup
    gin.SetMode(gin.TestMode)
    router := gin.New()
    
    hub := NewHub(zap.NewNop())
    go hub.Run()
    defer hub.Stop()
    
    ctrl := NewWebSocketController(hub, mockJwtUtils(), zap.NewNop())
    router.GET("/ws", ctrl.HandleWebSocket)
    
    server := httptest.NewServer(router)
    defer server.Close()
    
    // Connect
    wsURL := "ws" + strings.TrimPrefix(server.URL, "http") + "/ws?token=valid-token"
    conn, resp, err := websocket.DefaultDialer.Dial(wsURL, nil)
    require.NoError(t, err)
    defer conn.Close()
    
    assert.Equal(t, http.StatusSwitchingProtocols, resp.StatusCode)
}

func TestWebSocket_InvalidToken(t *testing.T) {
    gin.SetMode(gin.TestMode)
    router := gin.New()
    
    hub := NewHub(zap.NewNop())
    go hub.Run()
    
    ctrl := NewWebSocketController(hub, mockJwtUtils(), zap.NewNop())
    router.GET("/ws", ctrl.HandleWebSocket)
    
    server := httptest.NewServer(router)
    defer server.Close()
    
    // Connect with invalid token
    wsURL := "ws" + strings.TrimPrefix(server.URL, "http") + "/ws?token=invalid"
    _, resp, err := websocket.DefaultDialer.Dial(wsURL, nil)
    
    assert.Error(t, err)
    assert.Equal(t, http.StatusUnauthorized, resp.StatusCode)
}

func TestWebSocket_MessageRoundTrip(t *testing.T) {
    // Setup and connect...
    conn, _ := setupWebSocketConnection(t)
    defer conn.Close()
    
    // Send ping
    pingMsg := WSMessage{
        Type:      "PING",
        Timestamp: time.Now().UnixMilli(),
    }
    err := conn.WriteJSON(pingMsg)
    require.NoError(t, err)
    
    // Read pong
    var pongMsg WSMessage
    err = conn.ReadJSON(&pongMsg)
    require.NoError(t, err)
    
    assert.Equal(t, "PONG", pongMsg.Type)
}
```

---

## 3. Load Testing

### 3.1. K6 Load Test Script

```javascript
// tests/load/websocket_load_test.js
import ws from 'k6/ws';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '30s', target: 100 },   // Ramp up to 100 users
    { duration: '1m', target: 100 },    // Stay at 100 users
    { duration: '30s', target: 500 },   // Ramp up to 500 users
    { duration: '2m', target: 500 },    // Stay at 500 users
    { duration: '30s', target: 0 },     // Ramp down
  ],
  thresholds: {
    errors: ['rate<0.01'],              // Error rate < 1%
    ws_connecting: ['p(95)<500'],       // 95% connect in < 500ms
  },
};

const WS_URL = __ENV.WS_URL || 'ws://localhost:8088/notification/ws';
const TOKEN = __ENV.TOKEN || 'test-token';

export default function () {
  const url = `${WS_URL}?token=${TOKEN}`;
  
  const res = ws.connect(url, {}, function (socket) {
    socket.on('open', () => {
      console.log('Connected');
      
      // Subscribe to categories
      socket.send(JSON.stringify({
        type: 'SUBSCRIBE',
        payload: { categories: ['TASK', 'CRM'] },
        timestamp: Date.now(),
      }));
    });
    
    socket.on('message', (data) => {
      const msg = JSON.parse(data);
      
      // Send ACK for notifications
      if (msg.type === 'NEW_NOTIFICATION' && msg.messageId) {
        socket.send(JSON.stringify({
          type: 'ACK',
          payload: { messageId: msg.messageId },
          timestamp: Date.now(),
        }));
      }
    });
    
    socket.on('error', (e) => {
      errorRate.add(1);
      console.log('Error:', e);
    });
    
    // Send periodic pings
    socket.setInterval(() => {
      socket.send(JSON.stringify({
        type: 'PING',
        timestamp: Date.now(),
      }));
    }, 30000);
    
    // Keep connection open
    socket.setTimeout(() => {
      socket.close();
    }, 120000);
  });
  
  check(res, {
    'Connected successfully': (r) => r && r.status === 101,
  });
  
  sleep(1);
}
```

**Run Load Test:**
```bash
k6 run --env WS_URL=ws://localhost:8088/notification/ws tests/load/websocket_load_test.js
```

---

## 4. Manual Testing with wscat

```bash
# Install wscat
npm install -g wscat

# Connect to WebSocket
wscat -c "ws://localhost:8088/notification/ws?token=YOUR_JWT_TOKEN"

# Send ping
{"type":"PING","timestamp":1702454400000}

# Subscribe
{"type":"SUBSCRIBE","payload":{"categories":["TASK","CRM"]},"timestamp":1702454400000}

# Unsubscribe
{"type":"UNSUBSCRIBE","payload":{"categories":["CRM"]},"timestamp":1702454400000}
```

---

## 5. Benchmark Tests

```go
// infrastructure/websocket/hub_benchmark_test.go
package websocket

import (
    "testing"
    
    "go.uber.org/zap"
)

func BenchmarkHub_SendToUser(b *testing.B) {
    logger := zap.NewNop()
    hub := NewHub(logger)
    go hub.Run()
    defer hub.Stop()
    
    // Setup 1000 clients
    for i := 0; i < 1000; i++ {
        client := &Client{
            id:       fmt.Sprintf("client-%d", i),
            userID:   int64(i),
            tenantID: 1,
            send:     make(chan []byte, 256),
        }
        hub.Register(client)
        
        // Drain channel
        go func(c *Client) {
            for range c.send {
            }
        }(client)
    }
    
    msg := []byte(`{"type":"TEST","payload":{}}`)
    
    b.ResetTimer()
    
    for i := 0; i < b.N; i++ {
        hub.SendToUser(int64(i%1000), msg)
    }
}

func BenchmarkHub_BroadcastToAll(b *testing.B) {
    logger := zap.NewNop()
    hub := NewHub(logger)
    go hub.Run()
    defer hub.Stop()
    
    // Setup 1000 clients
    for i := 0; i < 1000; i++ {
        client := &Client{
            id:       fmt.Sprintf("client-%d", i),
            userID:   int64(i),
            tenantID: 1,
            send:     make(chan []byte, 256),
        }
        hub.Register(client)
        
        go func(c *Client) {
            for range c.send {
            }
        }(client)
    }
    
    msg := []byte(`{"type":"TEST","payload":{}}`)
    
    b.ResetTimer()
    
    for i := 0; i < b.N; i++ {
        hub.BroadcastToAll(msg)
    }
}
```

**Run Benchmarks:**
```bash
go test -bench=. -benchmem ./infrastructure/websocket/
```

---

## 6. Test Utilities

```go
// test/utils/websocket_test_utils.go
package utils

import (
    "net/http/httptest"
    "strings"
    
    "github.com/gorilla/websocket"
)

type TestWSClient struct {
    Conn     *websocket.Conn
    Messages chan []byte
    Errors   chan error
}

func NewTestWSClient(serverURL, token string) (*TestWSClient, error) {
    wsURL := "ws" + strings.TrimPrefix(serverURL, "http") + "/ws?token=" + token
    
    conn, _, err := websocket.DefaultDialer.Dial(wsURL, nil)
    if err != nil {
        return nil, err
    }
    
    client := &TestWSClient{
        Conn:     conn,
        Messages: make(chan []byte, 100),
        Errors:   make(chan error, 10),
    }
    
    // Start reading messages
    go func() {
        for {
            _, msg, err := conn.ReadMessage()
            if err != nil {
                client.Errors <- err
                return
            }
            client.Messages <- msg
        }
    }()
    
    return client, nil
}

func (c *TestWSClient) SendJSON(v interface{}) error {
    return c.Conn.WriteJSON(v)
}

func (c *TestWSClient) Close() {
    c.Conn.Close()
}
```
