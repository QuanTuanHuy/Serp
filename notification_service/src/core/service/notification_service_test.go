/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/
package service

import (
	"context"
	"errors"
	"sync"
	"testing"

	"github.com/serp/notification-service/src/core/domain/constant"
	"github.com/serp/notification-service/src/core/domain/dto/request"
	"github.com/serp/notification-service/src/core/domain/entity"
	"github.com/serp/notification-service/src/core/domain/enum"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type notificationPortStub struct {
	createFn          func(ctx context.Context, tx *gorm.DB, n *entity.NotificationEntity) (*entity.NotificationEntity, error)
	createBatchFn     func(ctx context.Context, tx *gorm.DB, ns []*entity.NotificationEntity) ([]*entity.NotificationEntity, error)
	getByIDFn         func(ctx context.Context, id int64) (*entity.NotificationEntity, error)
	getListFn         func(ctx context.Context, params *request.GetNotificationParams) ([]*entity.NotificationEntity, int64, error)
	countUnreadFn     func(ctx context.Context, userID int64) (int64, error)
	updateFn          func(ctx context.Context, tx *gorm.DB, id int64, n *entity.NotificationEntity) (*entity.NotificationEntity, error)
	updateAllUnreadFn func(ctx context.Context, tx *gorm.DB, userID int64) error
	deleteFn          func(ctx context.Context, tx *gorm.DB, id int64) error
}

func (s *notificationPortStub) Create(ctx context.Context, tx *gorm.DB, n *entity.NotificationEntity) (*entity.NotificationEntity, error) {
	if s.createFn != nil {
		return s.createFn(ctx, tx, n)
	}
	return n, nil
}
func (s *notificationPortStub) CreateBatch(ctx context.Context, tx *gorm.DB, ns []*entity.NotificationEntity) ([]*entity.NotificationEntity, error) {
	if s.createBatchFn != nil {
		return s.createBatchFn(ctx, tx, ns)
	}
	return ns, nil
}
func (s *notificationPortStub) GetByID(ctx context.Context, id int64) (*entity.NotificationEntity, error) {
	if s.getByIDFn != nil {
		return s.getByIDFn(ctx, id)
	}
	return nil, nil
}
func (s *notificationPortStub) GetList(ctx context.Context, params *request.GetNotificationParams) ([]*entity.NotificationEntity, int64, error) {
	if s.getListFn != nil {
		return s.getListFn(ctx, params)
	}
	return nil, 0, nil
}
func (s *notificationPortStub) CountUnread(ctx context.Context, userID int64) (int64, error) {
	if s.countUnreadFn != nil {
		return s.countUnreadFn(ctx, userID)
	}
	return 0, nil
}
func (s *notificationPortStub) Update(ctx context.Context, tx *gorm.DB, id int64, n *entity.NotificationEntity) (*entity.NotificationEntity, error) {
	if s.updateFn != nil {
		return s.updateFn(ctx, tx, id, n)
	}
	return n, nil
}
func (s *notificationPortStub) UpdateBatch(ctx context.Context, tx *gorm.DB, ns []*entity.NotificationEntity) ([]*entity.NotificationEntity, error) {
	return ns, nil
}
func (s *notificationPortStub) UpdateAllUnread(ctx context.Context, tx *gorm.DB, userID int64) error {
	if s.updateAllUnreadFn != nil {
		return s.updateAllUnreadFn(ctx, tx, userID)
	}
	return nil
}
func (s *notificationPortStub) Delete(ctx context.Context, tx *gorm.DB, id int64) error {
	if s.deleteFn != nil {
		return s.deleteFn(ctx, tx, id)
	}
	return nil
}

type redisPortStub struct {
	wg             *sync.WaitGroup
	setCalls       []string
	setTTL         []int
	setValues      []any
	deleteCalls    []string
	decrementCalls []string
	getErr         error
	getValue       any
}

func (r *redisPortStub) SetToRedis(ctx context.Context, key string, value any, ttl int) error {
	r.setCalls = append(r.setCalls, key)
	r.setTTL = append(r.setTTL, ttl)
	r.setValues = append(r.setValues, value)
	if r.wg != nil {
		r.wg.Done()
	}
	return nil
}
func (r *redisPortStub) GetFromRedis(ctx context.Context, key string, dest any) error {
	if r.getErr != nil {
		return r.getErr
	}
	if v, ok := r.getValue.(int64); ok {
		if p, ok := dest.(*int64); ok {
			*p = v
		}
	}
	return nil
}
func (r *redisPortStub) SetHSetToRedis(ctx context.Context, key string, mapData map[string]any, ttl int) error {
	return nil
}
func (r *redisPortStub) GetHSetFromRedis(ctx context.Context, key string) (map[string]string, error) {
	return nil, nil
}
func (r *redisPortStub) DeleteKeyFromRedis(ctx context.Context, key string) error {
	r.deleteCalls = append(r.deleteCalls, key)
	if r.wg != nil {
		r.wg.Done()
	}
	return nil
}
func (r *redisPortStub) GetKeysFromRedis(ctx context.Context, pattern string) ([]string, error) {
	return nil, nil
}
func (r *redisPortStub) ExistsInRedis(ctx context.Context, key string) (bool, error) {
	return false, nil
}
func (r *redisPortStub) DecrementInRedis(ctx context.Context, key string) (int64, error) {
	r.decrementCalls = append(r.decrementCalls, key)
	if r.wg != nil {
		r.wg.Done()
	}
	return 0, nil
}

func newService(nPort *notificationPortStub, rPort *redisPortStub) *NotificationService {
	return &NotificationService{
		notificationPort: nPort,
		redisPort:        rPort,
		logger:           zap.NewNop(),
	}
}

func TestValidateNotification(t *testing.T) {
	svc := newService(&notificationPortStub{}, &redisPortStub{})

	if err := svc.ValidateNotification(&entity.NotificationEntity{Title: "", Type: enum.TypeInfo}); err == nil || err.Error() != constant.ErrEmptyTitle {
		t.Fatalf("expected empty title error, got %v", err)
	}

	longTitle := make([]byte, 260)
	if err := svc.ValidateNotification(&entity.NotificationEntity{Title: string(longTitle), Type: enum.TypeInfo}); err == nil || err.Error() != constant.ErrTitleTooLong {
		t.Fatalf("expected title too long error, got %v", err)
	}

	if err := svc.ValidateNotification(&entity.NotificationEntity{Title: "ok", Type: "BAD"}); err == nil || err.Error() != constant.ErrInvalidType {
		t.Fatalf("expected invalid type error, got %v", err)
	}

	if err := svc.ValidateNotification(&entity.NotificationEntity{Title: "ok", Type: enum.TypeInfo}); err != nil {
		t.Fatalf("expected nil error, got %v", err)
	}
}

func TestCreateSetsUserStatusAndInvalidates(t *testing.T) {
	wg := &sync.WaitGroup{}
	wg.Add(1) // invalidate cache goroutine
	rStub := &redisPortStub{wg: wg}
	var captured *entity.NotificationEntity
	nStub := &notificationPortStub{
		createFn: func(ctx context.Context, tx *gorm.DB, n *entity.NotificationEntity) (*entity.NotificationEntity, error) {
			captured = n
			return n, nil
		},
	}
	svc := newService(nStub, rStub)

	req := &request.CreateNotificationRequest{
		Title:         "hello",
		Message:       "msg",
		Type:          string(enum.TypeInfo),
		Category:      string(enum.NotificationCategory("CAT")),
		Priority:      string(enum.NotificationPriority("LOW")),
		SourceService: "svc",
	}
	_, err := svc.Create(context.Background(), nil, 42, req)
	wg.Wait()

	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if captured.UserID != 42 || captured.Status != enum.NotificationUnread {
		t.Fatalf("unexpected captured notification: %+v", captured)
	}
	if len(rStub.deleteCalls) != 1 {
		t.Fatalf("expected cache invalidation once, got %d", len(rStub.deleteCalls))
	}
}

func TestCreateBulkValidatesAndSetsUnread(t *testing.T) {
	rStub := &redisPortStub{}
	nStub := &notificationPortStub{}
	svc := newService(nStub, rStub)

	notifications := []*entity.NotificationEntity{
		{UserID: 1, Title: "ok", Type: enum.TypeInfo},
		{UserID: 2, Title: "", Type: enum.TypeInfo}, // invalid
	}
	if err := svc.CreateBulk(context.Background(), nil, notifications); err == nil {
		t.Fatalf("expected validation error")
	}

	wg := &sync.WaitGroup{}
	wg.Add(2) // two unique users
	rStub = &redisPortStub{wg: wg}
	createdCount := 0
	nStub = &notificationPortStub{
		createBatchFn: func(ctx context.Context, tx *gorm.DB, ns []*entity.NotificationEntity) ([]*entity.NotificationEntity, error) {
			createdCount = len(ns)
			for _, n := range ns {
				if n.Status != enum.NotificationUnread {
					t.Fatalf("expected unread status, got %v", n.Status)
				}
			}
			return ns, nil
		},
	}
	svc = newService(nStub, rStub)

	notifications = []*entity.NotificationEntity{
		{UserID: 1, Title: "a", Type: enum.TypeInfo},
		{UserID: 2, Title: "b", Type: enum.TypeInfo},
	}
	if err := svc.CreateBulk(context.Background(), nil, notifications); err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	wg.Wait()
	if createdCount != 2 {
		t.Fatalf("expected 2 created, got %d", createdCount)
	}
	if len(rStub.deleteCalls) != 2 {
		t.Fatalf("expected cache invalidation for 2 users, got %d", len(rStub.deleteCalls))
	}
}

func TestMarkAsRead(t *testing.T) {
	wg := &sync.WaitGroup{}
	wg.Add(1) // decrement cache goroutine
	rStub := &redisPortStub{wg: wg}
	updateCalled := false
	nStub := &notificationPortStub{
		getByIDFn: func(ctx context.Context, id int64) (*entity.NotificationEntity, error) {
			return &entity.NotificationEntity{UserID: 7, Status: enum.NotificationUnread}, nil
		},
		updateFn: func(ctx context.Context, tx *gorm.DB, id int64, n *entity.NotificationEntity) (*entity.NotificationEntity, error) {
			updateCalled = true
			if !n.IsRead || n.Status != enum.NotificationRead || n.ReadAt == nil {
				t.Fatalf("expected notification marked read")
			}
			return n, nil
		},
	}
	svc := newService(nStub, rStub)
	if err := svc.MarkAsRead(context.Background(), nil, 5, 7); err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	wg.Wait()
	if !updateCalled {
		t.Fatalf("expected update called")
	}
	if len(rStub.decrementCalls) != 1 {
		t.Fatalf("expected cache decrement once, got %d", len(rStub.decrementCalls))
	}
}

func TestMarkAsReadAlreadyRead(t *testing.T) {
	rStub := &redisPortStub{}
	nStub := &notificationPortStub{
		getByIDFn: func(ctx context.Context, id int64) (*entity.NotificationEntity, error) {
			return &entity.NotificationEntity{UserID: 7, IsRead: true}, nil
		},
		updateFn: func(ctx context.Context, tx *gorm.DB, id int64, n *entity.NotificationEntity) (*entity.NotificationEntity, error) {
			t.Fatalf("update should not be called")
			return n, nil
		},
	}
	svc := newService(nStub, rStub)
	if err := svc.MarkAsRead(context.Background(), nil, 5, 7); err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
}

func TestMarkAllAsRead(t *testing.T) {
	wg := &sync.WaitGroup{}
	wg.Add(1) // cache set goroutine
	rStub := &redisPortStub{wg: wg}
	updateAllCalled := false
	nStub := &notificationPortStub{
		updateAllUnreadFn: func(ctx context.Context, tx *gorm.DB, userID int64) error {
			updateAllCalled = true
			return nil
		},
	}
	svc := newService(nStub, rStub)
	if err := svc.MarkAllAsRead(context.Background(), nil, 9); err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	wg.Wait()
	if !updateAllCalled {
		t.Fatalf("expected update all unread called")
	}
	if len(rStub.setCalls) != 1 {
		t.Fatalf("expected cache set once, got %d", len(rStub.setCalls))
	}
}

func TestGetUnreadCountCacheHitAndMiss(t *testing.T) {
	// cache hit
	rStub := &redisPortStub{getValue: int64(3)}
	nStub := &notificationPortStub{}
	svc := newService(nStub, rStub)
	cnt, err := svc.GetUnreadCount(context.Background(), 10)
	if err != nil || cnt != 3 {
		t.Fatalf("expected cache hit count 3, got %d err %v", cnt, err)
	}

	// cache miss
	wg := &sync.WaitGroup{}
	wg.Add(1) // set cache goroutine
	rStub = &redisPortStub{getErr: errors.New("miss"), wg: wg}
	nStub = &notificationPortStub{
		countUnreadFn: func(ctx context.Context, userID int64) (int64, error) { return 5, nil },
	}
	svc = newService(nStub, rStub)
	cnt, err = svc.GetUnreadCount(context.Background(), 11)
	wg.Wait()
	if err != nil || cnt != 5 {
		t.Fatalf("expected count 5, got %d err %v", cnt, err)
	}
	if len(rStub.setCalls) != 1 {
		t.Fatalf("expected cache set once on miss, got %d", len(rStub.setCalls))
	}
}

func TestDeleteRespectsUser(t *testing.T) {
	nStub := &notificationPortStub{
		getByIDFn: func(ctx context.Context, id int64) (*entity.NotificationEntity, error) {
			return &entity.NotificationEntity{BaseEntity: entity.BaseEntity{ID: 1}, UserID: 99}, nil
		},
		deleteFn: func(ctx context.Context, tx *gorm.DB, id int64) error {
			t.Fatalf("delete should not be called")
			return nil
		},
	}
	svc := newService(nStub, &redisPortStub{})
	if err := svc.Delete(context.Background(), nil, 1, 100); err == nil {
		t.Fatalf("expected not found error")
	}
}

func TestGetByIDNotFound(t *testing.T) {
	nStub := &notificationPortStub{
		getByIDFn: func(ctx context.Context, id int64) (*entity.NotificationEntity, error) {
			return nil, nil
		},
	}
	svc := newService(nStub, &redisPortStub{})
	if _, err := svc.GetByID(context.Background(), 1); err == nil {
		t.Fatalf("expected not found error")
	}
}

func TestGetListInjectsUserID(t *testing.T) {
	var gotUserID int64
	nStub := &notificationPortStub{
		getListFn: func(ctx context.Context, params *request.GetNotificationParams) ([]*entity.NotificationEntity, int64, error) {
			if params.UserID == nil {
				t.Fatalf("expected userID injected")
			}
			gotUserID = *params.UserID
			return []*entity.NotificationEntity{}, 0, nil
		},
	}
	svc := newService(nStub, &redisPortStub{})
	_, _, err := svc.GetList(context.Background(), 77, &request.GetNotificationParams{})
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if gotUserID != 77 {
		t.Fatalf("expected userID 77, got %d", gotUserID)
	}
}
