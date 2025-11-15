/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/account"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type DepartmentClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (d *DepartmentClientAdapter) CreateDepartment(ctx context.Context, organizationId int64, req any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/api/v1/organizations/%d/departments", organizationId)

	var httpResponse *utils.HTTPResponse
	err := d.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = d.apiClient.POST(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create department API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !d.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateDepartment API returned error status: %d", httpResponse.StatusCode))
	}
	var result response.BaseResponse
	if err := d.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create department response: %w", err)
	}
	return &result, nil
}

func (d *DepartmentClientAdapter) GetDepartments(ctx context.Context, organizationId int64, query map[string]string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/api/v1/organizations/%d/departments", organizationId)

	var httpResponse *utils.HTTPResponse
	err := d.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = d.apiClient.GETWithQuery(ctx, path, query, headers)
		if err != nil {
			return fmt.Errorf("failed to call get departments API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !d.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetDepartments API returned error status: %d", httpResponse.StatusCode))
	}
	var result response.BaseResponse
	if err := d.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get departments response: %w", err)
	}
	return &result, nil
}

func (d *DepartmentClientAdapter) GetDepartmentById(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/api/v1/organizations/%d/departments/%d", organizationId, departmentId)

	var httpResponse *utils.HTTPResponse
	err := d.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = d.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get department by id API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !d.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetDepartmentById API returned error status: %d", httpResponse.StatusCode))
	}
	var result response.BaseResponse
	if err := d.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get department by id response: %w", err)
	}
	return &result, nil
}

func (d *DepartmentClientAdapter) UpdateDepartment(ctx context.Context, organizationId int64, departmentId int64, req any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/api/v1/organizations/%d/departments/%d", organizationId, departmentId)

	var httpResponse *utils.HTTPResponse
	err := d.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = d.apiClient.PATCH(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update department API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !d.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateDepartment API returned error status: %d", httpResponse.StatusCode))
	}
	var result response.BaseResponse
	if err := d.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update department response: %w", err)
	}
	return &result, nil
}

func (d *DepartmentClientAdapter) DeleteDepartment(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/api/v1/organizations/%d/departments/%d", organizationId, departmentId)

	var httpResponse *utils.HTTPResponse
	err := d.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = d.apiClient.DELETE(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete department API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !d.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteDepartment API returned error status: %d", httpResponse.StatusCode))
	}
	var result response.BaseResponse
	if err := d.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete department response: %w", err)
	}
	return &result, nil
}

func (d *DepartmentClientAdapter) GetDepartmentTree(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/api/v1/organizations/%d/departments/%d/tree", organizationId, departmentId)

	var httpResponse *utils.HTTPResponse
	err := d.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = d.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get department tree API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !d.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetDepartmentTree API returned error status: %d", httpResponse.StatusCode))
	}
	var result response.BaseResponse
	if err := d.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get department tree response: %w", err)
	}
	return &result, nil
}

func (d *DepartmentClientAdapter) AssignUserToDepartment(ctx context.Context, organizationId int64, departmentId int64, req any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/api/v1/organizations/%d/departments/%d/users", organizationId, departmentId)

	var httpResponse *utils.HTTPResponse
	err := d.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = d.apiClient.POST(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call assign user to department API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !d.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("AssignUserToDepartment API returned error status: %d", httpResponse.StatusCode))
	}
	var result response.BaseResponse
	if err := d.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal assign user to department response: %w", err)
	}
	return &result, nil
}

func (d *DepartmentClientAdapter) BulkAssignUsersToDepartment(ctx context.Context, organizationId int64, departmentId int64, req any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/api/v1/organizations/%d/departments/%d/users/bulk", organizationId, departmentId)

	var httpResponse *utils.HTTPResponse
	err := d.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = d.apiClient.POST(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call bulk assign users to department API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !d.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("BulkAssignUsersToDepartment API returned error status: %d", httpResponse.StatusCode))
	}
	var result response.BaseResponse
	if err := d.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal bulk assign users to department response: %w", err)
	}
	return &result, nil
}

func (d *DepartmentClientAdapter) RemoveUserFromDepartment(ctx context.Context, organizationId int64, departmentId int64, userId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/api/v1/organizations/%d/departments/%d/users/%d", organizationId, departmentId, userId)

	var httpResponse *utils.HTTPResponse
	err := d.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = d.apiClient.DELETE(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call remove user from department API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !d.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("RemoveUserFromDepartment API returned error status: %d", httpResponse.StatusCode))
	}
	var result response.BaseResponse
	if err := d.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal remove user from department response: %w", err)
	}
	return &result, nil
}

func (d *DepartmentClientAdapter) GetDepartmentMembers(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/api/v1/organizations/%d/departments/%d/members", organizationId, departmentId)

	var httpResponse *utils.HTTPResponse
	err := d.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = d.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get department members API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !d.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetDepartmentMembers API returned error status: %d", httpResponse.StatusCode))
	}
	var result response.BaseResponse
	if err := d.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get department members response: %w", err)
	}
	return &result, nil
}

func NewDepartmentClientAdapter(authProps *properties.ExternalServiceProperties) port.IDepartmentClientPort {
	baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, authProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &DepartmentClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
