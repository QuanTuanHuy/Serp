/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"slices"

	"github.com/serp/pm-core/src/core/domain/constant"
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/core/domain/enum"
	"github.com/serp/pm-core/src/core/port/store"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type IProjectMemberService interface {
	GetMemberRole(ctx context.Context, projectID, userID int64) (*entity.ProjectMemberEntity, error)
	ValidateMemberPermission(ctx context.Context, projectID, userID int64, requiredRoles ...string) error
	AddMember(ctx context.Context, tx *gorm.DB, member *entity.ProjectMemberEntity) (*entity.ProjectMemberEntity, error)
}

type projectMemberService struct {
	projectMemberPort store.IProjectMemberPort
	logger            *zap.Logger
}

func NewProjectMemberService(
	projectMemberPort store.IProjectMemberPort,
	logger *zap.Logger,
) IProjectMemberService {
	return &projectMemberService{
		projectMemberPort: projectMemberPort,
		logger:            logger,
	}
}

func (s *projectMemberService) GetMemberRole(ctx context.Context, projectID, userID int64) (*entity.ProjectMemberEntity, error) {
	return s.projectMemberPort.GetMemberByProjectAndUser(ctx, projectID, userID)
}

func (s *projectMemberService) ValidateMemberPermission(ctx context.Context, projectID, userID int64, requiredRoles ...string) error {
	member, err := s.projectMemberPort.GetMemberByProjectAndUser(ctx, projectID, userID)
	if err != nil {
		return err
	}
	if member == nil {
		return errors.New(constant.GetProjectForbidden)
	}

	if len(requiredRoles) == 0 {
		return nil
	}

	memberRole := enum.ProjectMemberRole(member.Role)
	if slices.Contains(requiredRoles, string(memberRole)) {
		return nil
	}

	return errors.New(constant.InsufficientPermissions)
}

func (s *projectMemberService) AddMember(ctx context.Context, tx *gorm.DB, member *entity.ProjectMemberEntity) (*entity.ProjectMemberEntity, error) {
	existing, err := s.projectMemberPort.GetMemberByProjectAndUser(ctx, member.ProjectID, member.UserID)
	if err != nil {
		return nil, err
	}
	if existing != nil {
		return nil, errors.New(constant.MemberAlreadyExists)
	}
	return s.projectMemberPort.CreateMember(ctx, tx, member)
}
