/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/account"
)

type IKeycloakService interface {
	GetKeycloakClientSecret(ctx context.Context, clientId string) (*response.BaseResponse, error)
}

type KeycloakService struct {
	keycloakClient port.IKeycloakClientPort
}

func (k *KeycloakService) GetKeycloakClientSecret(ctx context.Context, clientId string) (*response.BaseResponse, error) {
	res, err := k.keycloakClient.GetKeycloakClientSecret(ctx, clientId)
	if err != nil {
		log.Error(ctx, "KeycloakService: GetKeycloakClientSecret error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewKeycloakService(keycloakClient port.IKeycloakClientPort) IKeycloakService {
	return &KeycloakService{
		keycloakClient: keycloakClient,
	}
}
