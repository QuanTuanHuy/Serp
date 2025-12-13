# PreferenceUseCase - Chi Tiết Thiết Kế

**File:** `core/usecase/preference_usecase.go`  
**Responsibility:** User preference orchestration

---

## 1. Interface Definition

```go
package usecase

import (
    "context"
    
    "notification_service/src/core/domain/dto/request"
    "notification_service/src/core/domain/dto/response"
)

type IPreferenceUseCase interface {
    // User operations
    GetPreferences(ctx context.Context, userID int64) (*response.PreferenceResponse, error)
    UpdatePreferences(ctx context.Context, userID int64, req *request.UpdatePreferenceRequest) (*response.PreferenceResponse, error)
    
    // Channel toggles
    ToggleChannel(ctx context.Context, userID int64, channel string, enabled bool) error
    
    // Quiet hours
    SetQuietHours(ctx context.Context, userID int64, req *request.QuietHoursRequest) error
    
    // Digest settings
    SetDigestPreferences(ctx context.Context, userID int64, req *request.DigestRequest) error
}
```

---

## 2. Struct Definition

```go
package usecase

import (
    "context"

    "notification_service/src/core/domain/dto/request"
    "notification_service/src/core/domain/dto/response"
    "notification_service/src/core/mapper"
    "notification_service/src/core/service"
    "gorm.io/gorm"
)

type PreferenceUseCase struct {
    txService         service.ITransactionService
    preferenceService service.IPreferenceService
}

func NewPreferenceUseCase(
    txService service.ITransactionService,
    preferenceService service.IPreferenceService,
) IPreferenceUseCase {
    return &PreferenceUseCase{
        txService:         txService,
        preferenceService: preferenceService,
    }
}
```

---

## 3. Get Preferences

```go
func (u *PreferenceUseCase) GetPreferences(
    ctx context.Context,
    userID int64,
) (*response.PreferenceResponse, error) {
    // Get or create with defaults
    var pref *entity.NotificationPreferenceEntity
    var err error
    
    err = u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
        pref, err = u.preferenceService.GetOrCreate(ctx, tx, userID)
        return err
    })
    
    if err != nil {
        return nil, err
    }
    
    return mapper.PreferenceEntityToResponse(pref), nil
}
```

---

## 4. Update Preferences

```go
func (u *PreferenceUseCase) UpdatePreferences(
    ctx context.Context,
    userID int64,
    req *request.UpdatePreferenceRequest,
) (*response.PreferenceResponse, error) {
    var pref *entity.NotificationPreferenceEntity
    
    err := u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
        var err error
        pref, err = u.preferenceService.Update(ctx, tx, userID, req)
        return err
    })
    
    if err != nil {
        return nil, err
    }
    
    return mapper.PreferenceEntityToResponse(pref), nil
}
```

---

## 5. Toggle Channel

```go
func (u *PreferenceUseCase) ToggleChannel(
    ctx context.Context,
    userID int64,
    channel string,
    enabled bool,
) error {
    req := &request.UpdatePreferenceRequest{}
    
    switch channel {
    case "email":
        req.EmailEnabled = &enabled
    case "push":
        req.PushEnabled = &enabled
    case "in_app":
        req.InAppEnabled = &enabled
    default:
        return fmt.Errorf("invalid channel: %s", channel)
    }
    
    return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
        _, err := u.preferenceService.Update(ctx, tx, userID, req)
        return err
    })
}
```

---

## 6. Set Quiet Hours

```go
func (u *PreferenceUseCase) SetQuietHours(
    ctx context.Context,
    userID int64,
    req *request.QuietHoursRequest,
) error {
    updateReq := &request.UpdatePreferenceRequest{
        QuietHoursEnabled:  &req.Enabled,
        QuietHoursStart:    req.Start,
        QuietHoursEnd:      req.End,
        QuietHoursTimezone: req.Timezone,
    }
    
    return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
        _, err := u.preferenceService.Update(ctx, tx, userID, updateReq)
        return err
    })
}
```

---

## 7. Set Digest Preferences

```go
func (u *PreferenceUseCase) SetDigestPreferences(
    ctx context.Context,
    userID int64,
    req *request.DigestRequest,
) error {
    updateReq := &request.UpdatePreferenceRequest{
        DigestEnabled:   &req.Enabled,
        DigestFrequency: req.Frequency,
    }
    
    return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
        _, err := u.preferenceService.Update(ctx, tx, userID, updateReq)
        return err
    })
}
```

---

## 8. Response DTO

```go
// dto/response/preference_response.go

type PreferenceResponse struct {
    // Channel preferences
    EmailEnabled  bool `json:"emailEnabled"`
    PushEnabled   bool `json:"pushEnabled"`
    InAppEnabled  bool `json:"inAppEnabled"`
    
    // Type preferences
    TaskNotifications      bool `json:"taskNotifications"`
    CommentNotifications   bool `json:"commentNotifications"`
    MentionNotifications   bool `json:"mentionNotifications"`
    SystemNotifications    bool `json:"systemNotifications"`
    MarketingNotifications bool `json:"marketingNotifications"`
    
    // Quiet hours
    QuietHours *QuietHoursDTO `json:"quietHours"`
    
    // Digest
    Digest *DigestDTO `json:"digest"`
}

type QuietHoursDTO struct {
    Enabled  bool   `json:"enabled"`
    Start    string `json:"start"`    // "22:00"
    End      string `json:"end"`      // "07:00"
    Timezone string `json:"timezone"` // "Asia/Ho_Chi_Minh"
}

type DigestDTO struct {
    Enabled   bool   `json:"enabled"`
    Frequency string `json:"frequency"` // "daily", "weekly"
}
```

---

## 9. Mapper

```go
// mapper/preference_mapper.go

func PreferenceEntityToResponse(e *entity.NotificationPreferenceEntity) *response.PreferenceResponse {
    resp := &response.PreferenceResponse{
        EmailEnabled:           e.EmailEnabled,
        PushEnabled:            e.PushEnabled,
        InAppEnabled:           e.InAppEnabled,
        TaskNotifications:      e.TaskNotifications,
        CommentNotifications:   e.CommentNotifications,
        MentionNotifications:   e.MentionNotifications,
        SystemNotifications:    e.SystemNotifications,
        MarketingNotifications: e.MarketingNotifications,
    }
    
    // Quiet hours
    resp.QuietHours = &response.QuietHoursDTO{
        Enabled:  e.QuietHoursEnabled,
        Timezone: e.QuietHoursTimezone,
    }
    if e.QuietHoursStart != nil {
        resp.QuietHours.Start = minutesToTimeString(*e.QuietHoursStart)
    }
    if e.QuietHoursEnd != nil {
        resp.QuietHours.End = minutesToTimeString(*e.QuietHoursEnd)
    }
    
    // Digest
    resp.Digest = &response.DigestDTO{
        Enabled:   e.DigestEnabled,
        Frequency: string(e.DigestFrequency),
    }
    
    return resp
}

func minutesToTimeString(minutes int) string {
    h := minutes / 60
    m := minutes % 60
    return fmt.Sprintf("%02d:%02d", h, m)
}
```

---

## 10. FX Registration

```go
// cmd/bootstrap/all.go
fx.Provide(usecase.NewPreferenceUseCase),
```
