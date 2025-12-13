# TransactionService - Chi Tiết Thiết Kế

**File:** `core/service/transaction_service.go`  
**Responsibility:** Database transaction management

---

## 1. Interface Definition

```go
package service

import (
    "context"
    
    "gorm.io/gorm"
)

type ITransactionService interface {
    // Execute function within transaction
    ExecuteInTransaction(ctx context.Context, fn func(tx *gorm.DB) error) error
    
    // Execute with result
    ExecuteInTransactionWithResult(ctx context.Context, fn func(tx *gorm.DB) (interface{}, error)) (interface{}, error)
    
    // Get database instance (for read-only operations)
    GetDB() *gorm.DB
}
```

---

## 2. Implementation

```go
package service

import (
    "context"
    "fmt"

    "gorm.io/gorm"
)

type TransactionService struct {
    db *gorm.DB
}

func NewTransactionService(db *gorm.DB) ITransactionService {
    return &TransactionService{db: db}
}

// ExecuteInTransaction executes fn within a transaction
// Automatically commits on success, rollbacks on error or panic
func (s *TransactionService) ExecuteInTransaction(
    ctx context.Context,
    fn func(tx *gorm.DB) error,
) error {
    return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
        return fn(tx)
    })
}

// ExecuteInTransactionWithResult executes fn and returns result
func (s *TransactionService) ExecuteInTransactionWithResult(
    ctx context.Context,
    fn func(tx *gorm.DB) (interface{}, error),
) (interface{}, error) {
    var result interface{}
    
    err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
        var fnErr error
        result, fnErr = fn(tx)
        return fnErr
    })
    
    if err != nil {
        return nil, err
    }
    
    return result, nil
}

func (s *TransactionService) GetDB() *gorm.DB {
    return s.db
}
```

---

## 3. Usage Pattern in UseCase

```go
// Example: NotificationUseCase using TransactionService
func (u *NotificationUseCase) CreateNotification(
    ctx context.Context,
    userID int64,
    req *request.CreateNotificationRequest,
) (*response.NotificationResponse, error) {
    
    result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (interface{}, error) {
        // 1. Create notification (within transaction)
        notification, err := u.notificationService.Create(ctx, tx, userID, req)
        if err != nil {
            return nil, err // Transaction will rollback
        }
        
        // 2. Update preference stats (within same transaction)
        if err := u.preferenceService.IncrementNotificationCount(ctx, tx, userID); err != nil {
            return nil, err // Transaction will rollback
        }
        
        return notification, nil
    })
    
    if err != nil {
        return nil, err
    }
    
    notification := result.(*entity.NotificationEntity)
    
    // 3. Deliver notification (after transaction commits)
    // This is outside transaction - if delivery fails, notification is still saved
    go u.deliveryService.Deliver(ctx, notification)
    
    return mapper.NotificationEntityToResponse(notification), nil
}
```

---

## 4. Nested Transaction Support

```go
// SavePoint for nested transactions
func (s *TransactionService) ExecuteWithSavepoint(
    ctx context.Context,
    tx *gorm.DB,
    name string,
    fn func(tx *gorm.DB) error,
) error {
    // Create savepoint
    if err := tx.SavePoint(name).Error; err != nil {
        return fmt.Errorf("failed to create savepoint: %w", err)
    }
    
    // Execute function
    if err := fn(tx); err != nil {
        // Rollback to savepoint on error
        if rbErr := tx.RollbackTo(name).Error; rbErr != nil {
            return fmt.Errorf("rollback failed: %w (original: %v)", rbErr, err)
        }
        return err
    }
    
    return nil
}
```

---

## 5. Transaction Options

```go
// ExecuteWithOptions allows custom transaction options
func (s *TransactionService) ExecuteWithOptions(
    ctx context.Context,
    opts *sql.TxOptions,
    fn func(tx *gorm.DB) error,
) error {
    return s.db.WithContext(ctx).Transaction(fn, opts)
}

// Usage: Read-committed isolation
// err := txService.ExecuteWithOptions(ctx, &sql.TxOptions{
//     Isolation: sql.LevelReadCommitted,
// }, func(tx *gorm.DB) error { ... })
```

---

## 6. FX Provider

```go
// cmd/bootstrap/all.go
fx.Provide(func(db *gorm.DB) service.ITransactionService {
    return service.NewTransactionService(db)
}),
```

---

## 7. Key Rules

| Rule | Description |
|------|-------------|
| **UseCase owns transaction** | Only UseCase layer starts/manages transactions |
| **Services receive tx** | Services receive `*gorm.DB` tx as parameter |
| **Kafka after commit** | Send Kafka events after transaction commits |
| **No nested tx in services** | Services should not start their own transactions |
