package utils

import (
	"context"
	"errors"
	"sync"
	"time"

	"github.com/serp/api-gateway/src/kernel/properties"
)

type CircuitBreakerState int

const (
	Closed CircuitBreakerState = iota
	Open
	HalfOpen
)

type CircuitBreaker struct {
	maxFailures  int
	resetTimeout time.Duration
	timeout      time.Duration

	mu              sync.RWMutex
	state           CircuitBreakerState
	failures        int
	lastFailureTime time.Time
	lastSuccessTime time.Time

	onStateChange func(from, to CircuitBreakerState)
}

type CircuitBreakerOption func(*CircuitBreaker)

func NewCircuitBreaker(props *properties.CircuitBreakerProperties, onStateChange func(from, to CircuitBreakerState)) *CircuitBreaker {
	return &CircuitBreaker{
		maxFailures:   props.MaxFailures,
		resetTimeout:  props.ResetTimeout,
		timeout:       props.Timeout,
		state:         Closed,
		onStateChange: onStateChange,
	}
}

func NewDefaultCircuitBreaker() *CircuitBreaker {
	return NewCircuitBreaker(&properties.CircuitBreakerProperties{
		MaxFailures:  5,
		ResetTimeout: 30 * time.Second,
		Timeout:      5 * time.Second,
	}, nil)
}

func (cb *CircuitBreaker) SetStateChangeCallback(fn func(from, to CircuitBreakerState)) {
	cb.onStateChange = fn
}

func (cb *CircuitBreaker) Execute(ctx context.Context, fn func(context.Context) error) error {
	if !cb.canExecute() {
		return errors.New("circuit breaker is open")
	}

	timeOutCtx, cancel := context.WithTimeout(ctx, cb.timeout)
	defer cancel()

	err := fn(timeOutCtx)

	if err != nil {
		cb.recordFailure()
		return err
	}

	cb.recordSuccess()
	return nil
}

// Use this when the function already has proper timeout handling
func (cb *CircuitBreaker) ExecuteWithoutTimeout(ctx context.Context, fn func(context.Context) error) error {
	if !cb.canExecute() {
		return errors.New("circuit breaker is open")
	}

	err := fn(ctx)

	if err != nil {
		cb.recordFailure()
		return err
	}

	cb.recordSuccess()
	return nil
}

func (cb *CircuitBreaker) recordFailure() {
	cb.mu.Lock()
	defer cb.mu.Unlock()

	cb.failures++
	cb.lastFailureTime = time.Now()

	if cb.failures >= cb.maxFailures {
		cb.setState(Open)
	}
}

func (cb *CircuitBreaker) recordSuccess() {
	cb.mu.Lock()
	defer cb.mu.Unlock()

	cb.lastSuccessTime = time.Now()

	if cb.state == HalfOpen {
		cb.setState(Closed)
		cb.failures = 0
	} else if cb.state == Closed {
		cb.failures = 0
	}
}

func (cb *CircuitBreaker) canExecute() bool {
	cb.mu.RLock()
	state := cb.state
	lastFailureTime := cb.lastFailureTime
	cb.mu.RUnlock()

	switch state {
	case Closed:
		return true
	case Open:
		if time.Since(lastFailureTime) > cb.resetTimeout {
			cb.mu.Lock()
			if cb.state == Open { // Double-check pattern
				cb.setState(HalfOpen)
			}
			cb.mu.Unlock()
			return true
		}
		return false
	case HalfOpen:
		return true
	default:
		return false
	}
}

func (cb *CircuitBreaker) setState(newState CircuitBreakerState) {
	oldState := cb.state
	cb.state = newState

	if cb.onStateChange != nil {
		go cb.onStateChange(oldState, newState)
	}
}
