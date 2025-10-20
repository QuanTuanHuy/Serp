package utils

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"time"

	"github.com/golibs-starter/golib/log"
)

type HTTPClient interface {
	Do(req *http.Request) (*http.Response, error)
}

type BaseAPIClient struct {
	client  HTTPClient
	timeout time.Duration
	baseURL string
}

type HTTPRequestConfig struct {
	Method  string
	URL     string
	Body    any
	Headers http.Header
	Timeout time.Duration
}

type HTTPResponse struct {
	StatusCode int
	Body       []byte
	Headers    http.Header
}

func NewBaseAPIClient(baseURL string, timeout time.Duration) *BaseAPIClient {
	if timeout == 0 {
		timeout = 30 * time.Second
	}

	return &BaseAPIClient{
		client: &http.Client{
			Timeout: timeout,
		},
		timeout: timeout,
		baseURL: baseURL,
	}
}

func NewBaseAPIClientWithCustomClient(client HTTPClient, baseURL string, timeout time.Duration) *BaseAPIClient {
	return &BaseAPIClient{
		client:  client,
		timeout: timeout,
		baseURL: baseURL,
	}
}

func (api *BaseAPIClient) GET(ctx context.Context, path string, headers http.Header) (*HTTPResponse, error) {
	config := &HTTPRequestConfig{
		Method:  http.MethodGet,
		URL:     api.buildURL(path),
		Headers: headers,
		Timeout: api.timeout,
	}
	return api.makeRequest(ctx, config)
}

func (api *BaseAPIClient) GETWithQuery(ctx context.Context, path string, queryParams map[string]string, headers http.Header) (*HTTPResponse, error) {
	fullURL := api.buildURL(path)

	if len(queryParams) > 0 {
		q := url.Values{}
		for key, value := range queryParams {
			q.Add(key, value)
		}
		fullURL = fullURL + "?" + q.Encode()
	}

	config := &HTTPRequestConfig{
		Method:  http.MethodGet,
		URL:     fullURL,
		Headers: headers,
		Timeout: api.timeout,
	}
	return api.makeRequest(ctx, config)
}

func (api *BaseAPIClient) POST(ctx context.Context, path string, body any, headers http.Header) (*HTTPResponse, error) {
	config := &HTTPRequestConfig{
		Method:  http.MethodPost,
		URL:     api.buildURL(path),
		Body:    body,
		Headers: headers,
		Timeout: api.timeout,
	}
	return api.makeRequest(ctx, config)
}

func (api *BaseAPIClient) POSTWithQuery(ctx context.Context, path string, queryParams map[string]string, body any, headers http.Header) (*HTTPResponse, error) {
	fullURL := api.buildURL(path)

	if len(queryParams) > 0 {
		q := url.Values{}
		for key, value := range queryParams {
			q.Add(key, value)
		}
		fullURL = fullURL + "?" + q.Encode()
	}

	config := &HTTPRequestConfig{
		Method:  http.MethodPost,
		URL:     fullURL,
		Body:    body,
		Headers: headers,
		Timeout: api.timeout,
	}
	return api.makeRequest(ctx, config)
}

func (api *BaseAPIClient) PUT(ctx context.Context, path string, body any, headers http.Header) (*HTTPResponse, error) {
	config := &HTTPRequestConfig{
		Method:  http.MethodPut,
		URL:     api.buildURL(path),
		Body:    body,
		Headers: headers,
		Timeout: api.timeout,
	}
	return api.makeRequest(ctx, config)
}

func (api *BaseAPIClient) DELETE(ctx context.Context, path string, headers http.Header) (*HTTPResponse, error) {
	config := &HTTPRequestConfig{
		Method:  http.MethodDelete,
		URL:     api.buildURL(path),
		Headers: headers,
		Timeout: api.timeout,
	}
	return api.makeRequest(ctx, config)
}

func (api *BaseAPIClient) PATCH(ctx context.Context, path string, body any, headers http.Header) (*HTTPResponse, error) {
	config := &HTTPRequestConfig{
		Method:  http.MethodPatch,
		URL:     api.buildURL(path),
		Body:    body,
		Headers: headers,
		Timeout: api.timeout,
	}
	return api.makeRequest(ctx, config)
}

func (api *BaseAPIClient) makeRequest(ctx context.Context, config *HTTPRequestConfig) (*HTTPResponse, error) {
	var requestBody io.Reader

	if config.Body != nil {
		jsonData, err := json.Marshal(config.Body)
		if err != nil {
			log.Error(ctx, fmt.Sprintf("Failed to marshal request body: err %v, url %s", err, config.URL))
			return nil, fmt.Errorf("failed to marshal request body: %w", err)
		}
		requestBody = bytes.NewBuffer(jsonData)
	}

	req, err := http.NewRequestWithContext(ctx, config.Method, config.URL, requestBody)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Failed to create HTTP request: %v", err))
		return nil, fmt.Errorf("failed to create HTTP request: %w", err)
	}

	if config.Headers != nil {
		req.Header = config.Headers
	}

	startTime := time.Now()
	resp, err := api.client.Do(req)
	duration := time.Since(startTime)

	if err != nil {
		log.Error(ctx, fmt.Sprintf("HTTP request failed: method %s, url %s, duration %v, err: %v", config.Method, config.URL, duration, err))
		return nil, fmt.Errorf("HTTP request failed: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Failed to read response body: status_code %v, url %s, err: %v", resp.StatusCode, config.URL, err))
		return nil, fmt.Errorf("failed to read response body: %w", err)
	}

	log.Info(ctx, fmt.Sprintf("HTTP request completed: method %s, url %s, status_code %d, duration %v, response_size %d",
		config.Method, config.URL, resp.StatusCode, duration, len(body)))

	return &HTTPResponse{
		StatusCode: resp.StatusCode,
		Body:       body,
		Headers:    resp.Header,
	}, nil
}

func (api *BaseAPIClient) buildURL(path string) string {
	if api.baseURL == "" {
		return path
	}
	return api.baseURL + path
}

func (api *BaseAPIClient) UnmarshalResponse(ctx context.Context, response *HTTPResponse, target any) error {
	if response == nil || response.Body == nil || len(response.Body) == 0 {
		// special case for java spring
		if response.StatusCode != http.StatusUnauthorized {
			return fmt.Errorf("response or response body is nil")
		}
		response.Body = []byte("{\"code\":401,\"status\":\"error\",\"message\":\"Unauthorized\",\"data\":null}")
	}

	if err := json.Unmarshal(response.Body, target); err != nil {
		log.Error(ctx, fmt.Sprintf("Failed to unmarshal response: err %v, response_body: %s, status_code: %d",
			err, string(response.Body), response.StatusCode))
		return fmt.Errorf("failed to unmarshal response: %w", err)
	}

	return nil
}

func (api *BaseAPIClient) IsSuccessStatusCode(statusCode int) bool {
	return statusCode >= 200 && statusCode < 300
}

func (api *BaseAPIClient) SetTimeout(timeout time.Duration) {
	api.timeout = timeout
	if httpClient, ok := api.client.(*http.Client); ok {
		httpClient.Timeout = timeout
	}
}

func (api *BaseAPIClient) GetBaseURL() string {
	return api.baseURL
}

func (api *BaseAPIClient) SetBaseURL(baseURL string) {
	api.baseURL = baseURL
}
