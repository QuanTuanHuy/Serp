/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package crm

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/crm"
)

type IContactService interface {
	CreateContact(ctx context.Context, customerId int64, payload map[string]any) (*response.BaseResponse, error)
	UpdateContact(ctx context.Context, customerId int64, contactId int64, payload map[string]any) (*response.BaseResponse, error)
	GetContactByID(ctx context.Context, customerId int64, contactId int64) (*response.BaseResponse, error)
	GetContactsByCustomer(ctx context.Context, customerId int64) (*response.BaseResponse, error)
	GetContacts(ctx context.Context, page, size int) (*response.BaseResponse, error)
	DeleteContact(ctx context.Context, customerId int64, contactId int64) (*response.BaseResponse, error)
}

type ContactService struct {
	contactClient port.IContactClientPort
}

func (c *ContactService) CreateContact(ctx context.Context, customerId int64, payload map[string]any) (*response.BaseResponse, error) {
	res, err := c.contactClient.CreateContact(ctx, customerId, payload)
	if err != nil {
		log.Error(ctx, "ContactService: CreateContact error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *ContactService) UpdateContact(ctx context.Context, customerId int64, contactId int64, payload map[string]any) (*response.BaseResponse, error) {
	res, err := c.contactClient.UpdateContact(ctx, customerId, contactId, payload)
	if err != nil {
		log.Error(ctx, "ContactService: UpdateContact error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *ContactService) GetContactByID(ctx context.Context, customerId int64, contactId int64) (*response.BaseResponse, error) {
	res, err := c.contactClient.GetContactByID(ctx, customerId, contactId)
	if err != nil {
		log.Error(ctx, "ContactService: GetContactByID error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *ContactService) GetContactsByCustomer(ctx context.Context, customerId int64) (*response.BaseResponse, error) {
	res, err := c.contactClient.GetContactsByCustomer(ctx, customerId)
	if err != nil {
		log.Error(ctx, "ContactService: GetContactsByCustomer error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *ContactService) GetContacts(ctx context.Context, page, size int) (*response.BaseResponse, error) {
	res, err := c.contactClient.GetContacts(ctx, page, size)
	if err != nil {
		log.Error(ctx, "ContactService: GetContacts error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *ContactService) DeleteContact(ctx context.Context, customerId int64, contactId int64) (*response.BaseResponse, error) {
	res, err := c.contactClient.DeleteContact(ctx, customerId, contactId)
	if err != nil {
		log.Error(ctx, "ContactService: DeleteContact error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewContactService(contactClient port.IContactClientPort) IContactService {
	return &ContactService{contactClient: contactClient}
}
