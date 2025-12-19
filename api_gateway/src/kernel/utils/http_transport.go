/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"net/http"
	"time"

	"github.com/sony/gobreaker/v2"
)

// CircuitBreakerTransport wraps http.RoundTripper with circuit breaker logic
type CircuitBreakerTransport struct {
	Base http.RoundTripper
	CB   *gobreaker.CircuitBreaker[*http.Response]
}

// NewCircuitBreakerTransport creates a new CircuitBreakerTransport
func NewCircuitBreakerTransport(base http.RoundTripper, cb *gobreaker.CircuitBreaker[*http.Response]) *CircuitBreakerTransport {
	if base == nil {
		base = http.DefaultTransport
	}
	return &CircuitBreakerTransport{
		Base: base,
		CB:   cb,
	}
}

func (t *CircuitBreakerTransport) RoundTrip(req *http.Request) (*http.Response, error) {
	resp, err := t.CB.Execute(func() (*http.Response, error) {
		resp, err := t.Base.RoundTrip(req)
		if err != nil {
			return nil, err
		}

		// Treat 5xx errors as failures for the Circuit Breaker
		if resp.StatusCode >= 500 {
			// Read body to allow connection reuse, then return error
			body, _ := io.ReadAll(resp.Body)
			resp.Body.Close()
			resp.Body = io.NopCloser(bytes.NewBuffer(body))
			return resp, fmt.Errorf("upstream error: %d", resp.StatusCode)
		}

		return resp, nil
	})

	// If CB returned an error but we have a response (5xx case), return the response
	if err != nil && resp != nil {
		return resp, nil
	}

	return resp, err
}

// RetryTransport wraps http.RoundTripper with retry logic
type RetryTransport struct {
	Next       http.RoundTripper
	MaxRetries int
	Delay      time.Duration
	MaxDelay   time.Duration
}

// NewRetryTransport creates a new RetryTransport with exponential backoff
func NewRetryTransport(next http.RoundTripper, maxRetries int, initialDelay, maxDelay time.Duration) *RetryTransport {
	return &RetryTransport{
		Next:       next,
		MaxRetries: maxRetries,
		Delay:      initialDelay,
		MaxDelay:   maxDelay,
	}
}

func (t *RetryTransport) RoundTrip(req *http.Request) (*http.Response, error) {
	var lastErr error
	var lastResp *http.Response

	// Clone the body for retries if necessary
	var bodyBytes []byte
	if req.Body != nil && req.GetBody == nil {
		bodyBytes, _ = io.ReadAll(req.Body)
		req.Body = io.NopCloser(bytes.NewBuffer(bodyBytes))
	}

	delay := t.Delay

	for attempt := 0; attempt <= t.MaxRetries; attempt++ {
		// Reset body for retry
		if attempt > 0 {
			if req.GetBody != nil {
				var err error
				req.Body, err = req.GetBody()
				if err != nil {
					return nil, err
				}
			} else if bodyBytes != nil {
				req.Body = io.NopCloser(bytes.NewBuffer(bodyBytes))
			}
		}

		resp, err := t.Next.RoundTrip(req)

		// Success or Client Error (4xx) - Return immediately
		if err == nil && resp.StatusCode < 500 {
			return resp, nil
		}

		// Circuit Breaker Open - Fail Fast (Don't retry)
		if err != nil && isCircuitBreakerOpen(err) {
			return nil, err
		}

		// Check Idempotency (Only retry GET, HEAD, PUT, DELETE, OPTIONS)
		if !isIdempotent(req.Method) {
			if resp != nil {
				return resp, err
			}
			return nil, err
		}

		// Store last error/response for final return
		lastErr = err
		if lastResp != nil && lastResp != resp {
			lastResp.Body.Close()
		}
		lastResp = resp

		// Don't wait after the last attempt
		if attempt == t.MaxRetries {
			break
		}

		// Wait before retrying with exponential backoff
		if !t.waitForRetry(req.Context(), delay) {
			// Context cancelled
			if lastResp != nil {
				lastResp.Body.Close()
			}
			return nil, req.Context().Err()
		}

		// Exponential backoff
		delay = delay * 2
		if delay > t.MaxDelay {
			delay = t.MaxDelay
		}
	}

	// Return the last response if we have one (e.g., 5xx response)
	if lastResp != nil {
		return lastResp, nil
	}

	return nil, lastErr
}

func (t *RetryTransport) waitForRetry(ctx context.Context, delay time.Duration) bool {
	timer := time.NewTimer(delay)
	defer timer.Stop()

	select {
	case <-ctx.Done():
		return false
	case <-timer.C:
		return true
	}
}

func isIdempotent(method string) bool {
	switch method {
	case http.MethodGet, http.MethodHead, http.MethodPut, http.MethodDelete, http.MethodOptions:
		return true
	}
	return false
}

func isCircuitBreakerOpen(err error) bool {
	return err == gobreaker.ErrOpenState || err == gobreaker.ErrTooManyRequests
}

// ResilientTransport combines retry and circuit breaker
// Chain: Retry -> CircuitBreaker -> Base Transport
type ResilientTransport struct {
	transport http.RoundTripper
}

// NewResilientTransport creates a transport with retry and circuit breaker
func NewResilientTransport(
	cb *gobreaker.CircuitBreaker[*http.Response],
	maxRetries int,
	initialDelay, maxDelay time.Duration,
) *ResilientTransport {
	cbTransport := NewCircuitBreakerTransport(http.DefaultTransport, cb)
	retryTransport := NewRetryTransport(cbTransport, maxRetries, initialDelay, maxDelay)

	return &ResilientTransport{
		transport: retryTransport,
	}
}

func (t *ResilientTransport) RoundTrip(req *http.Request) (*http.Response, error) {
	return t.transport.RoundTrip(req)
}
