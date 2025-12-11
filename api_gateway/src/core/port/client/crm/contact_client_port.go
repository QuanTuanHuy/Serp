/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package crm

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IContactClientPort interface {
	CreateContact(ctx context.Context, customerId int64, payload map[string]any) (*response.BaseResponse, error)
	UpdateContact(ctx context.Context, customerId int64, contactId int64, payload map[string]any) (*response.BaseResponse, error)
	GetContactByID(ctx context.Context, customerId int64, contactId int64) (*response.BaseResponse, error)
	GetContactsByCustomer(ctx context.Context, customerId int64) (*response.BaseResponse, error)
	GetContacts(ctx context.Context, page, size int) (*response.BaseResponse, error)
	DeleteContact(ctx context.Context, customerId int64, contactId int64) (*response.BaseResponse, error)
}
