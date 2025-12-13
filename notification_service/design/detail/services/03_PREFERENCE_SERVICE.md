# PreferenceService - Chi Tiết Thiết Kế

**File:** `core/service/preference_service.go`  
**Responsibility:** User notification preferences management

---

## 1. Interface Definition

```go
package service

import (
    "context"
    
    "notification_service/src/core/domain/dto/request"
    "notification_service/src/core/domain/entity"
    "notification_service/src/core/domain/enum"
    "gorm.io/gorm"
)

type IPreferenceService interface {
    // CRUD
    GetOrCreate(ctx context.Context, tx *gorm.DB, userID int64) (*entity.NotificationPreferenceEntity, error)
    GetByUserID(ctx context.Context, userID int64) (*entity.NotificationPreferenceEntity, error)
    Update(ctx context.Context, tx *gorm.DB, userID int64, req *request.UpdatePreferenceRequest) (*entity.NotificationPreferenceEntity, error)
    
    // Channel Preferences
    IsChannelEnabled(ctx context.Context, userID int64, channel enum.NotificationChannel) (bool, error)
    GetEnabledChannels(ctx context.Context, userID int64) ([]enum.NotificationChannel, error)
    
    // Type Preferences
    IsTypeEnabled(ctx context.Context, userID int64, notificationType enum.NotificationType) (bool, error)
    GetMutedTypes(ctx context.Context, userID int64) ([]enum.NotificationType, error)
    
    // Quiet Hours
    IsQuietHours(ctx context.Context, userID int64) (bool, error)
    GetQuietHoursConfig(ctx context.Context, userID int64) (*entity.QuietHoursConfig, error)
    
    // Business Logic
    ShouldDeliver(ctx context.Context, userID int64, notification *entity.NotificationEntity, channel enum.NotificationChannel) (bool, error)
}
```

---

## 2. Struct Definition

```go
package service

import (
    "context"
    "encoding/json"
    "time"

    "notification_service/src/core/domain/dto/request"
    "notification_service/src/core/domain/entity"
    "notification_service/src/core/domain/enum"
    "notification_service/src/core/port/client"
    "notification_service/src/core/port/store"
    "gorm.io/gorm"
)

type PreferenceService struct {
    preferencePort store.IPreferencePort
    redisPort      client.IRedisPort
}

func NewPreferenceService(
    preferencePort store.IPreferencePort,
    redisPort client.IRedisPort,
) IPreferenceService {
    return &PreferenceService{
        preferencePort: preferencePort,
        redisPort:      redisPort,
    }
}
```

---

## 3. Get or Create Pattern

```go
func (s *PreferenceService) GetOrCreate(
    ctx context.Context,
    tx *gorm.DB,
    userID int64,
) (*entity.NotificationPreferenceEntity, error) {
    // 1. Try to get existing
    existing, err := s.preferencePort.GetByUserID(ctx, userID)
    if err != nil {
        return nil, err
    }
    
    if existing != nil {
        return existing, nil
    }
    
    // 2. Create default preferences
    defaultPref := s.createDefaultPreference(userID)
    
    created, err := s.preferencePort.Create(ctx, tx, defaultPref)
    if err != nil {
        return nil, err
    }
    
    return created, nil
}

func (s *PreferenceService) createDefaultPreference(userID int64) *entity.NotificationPreferenceEntity {
    now := time.Now().UnixMilli()
    
    return &entity.NotificationPreferenceEntity{
        UserID: userID,
        
        // Default: All channels enabled
        EmailEnabled:       true,
        PushEnabled:        true,
        InAppEnabled:       true,
        
        // Default: All notification types enabled
        TaskNotifications:      true,
        CommentNotifications:   true,
        MentionNotifications:   true,
        SystemNotifications:    true,
        MarketingNotifications: false, // Default off for marketing
        
        // Quiet hours disabled by default
        QuietHoursEnabled:      false,
        QuietHoursStart:        nil,
        QuietHoursEnd:          nil,
        QuietHoursTimezone:     "UTC",
        
        // Digest preferences
        DigestEnabled:          false,
        DigestFrequency:        enum.DigestFrequencyDaily,
        
        CreatedAt: &now,
        UpdatedAt: &now,
    }
}
```

---

## 4. Update Preferences

```go
func (s *PreferenceService) Update(
    ctx context.Context,
    tx *gorm.DB,
    userID int64,
    req *request.UpdatePreferenceRequest,
) (*entity.NotificationPreferenceEntity, error) {
    // 1. Get existing (or create)
    existing, err := s.GetOrCreate(ctx, tx, userID)
    if err != nil {
        return nil, err
    }
    
    // 2. Apply updates
    updated := s.applyUpdates(existing, req)
    
    // 3. Validate
    if err := s.validatePreference(updated); err != nil {
        return nil, err
    }
    
    // 4. Persist
    now := time.Now().UnixMilli()
    updated.UpdatedAt = &now
    
    if err := s.preferencePort.Update(ctx, tx, existing.ID, updated); err != nil {
        return nil, err
    }
    
    // 5. Invalidate cache
    s.invalidateCache(ctx, userID)
    
    return updated, nil
}

func (s *PreferenceService) applyUpdates(
    existing *entity.NotificationPreferenceEntity,
    req *request.UpdatePreferenceRequest,
) *entity.NotificationPreferenceEntity {
    // Channel preferences
    if req.EmailEnabled != nil {
        existing.EmailEnabled = *req.EmailEnabled
    }
    if req.PushEnabled != nil {
        existing.PushEnabled = *req.PushEnabled
    }
    if req.InAppEnabled != nil {
        existing.InAppEnabled = *req.InAppEnabled
    }
    
    // Type preferences
    if req.TaskNotifications != nil {
        existing.TaskNotifications = *req.TaskNotifications
    }
    if req.CommentNotifications != nil {
        existing.CommentNotifications = *req.CommentNotifications
    }
    if req.MentionNotifications != nil {
        existing.MentionNotifications = *req.MentionNotifications
    }
    
    // Quiet hours
    if req.QuietHoursEnabled != nil {
        existing.QuietHoursEnabled = *req.QuietHoursEnabled
    }
    if req.QuietHoursStart != nil {
        existing.QuietHoursStart = req.QuietHoursStart
    }
    if req.QuietHoursEnd != nil {
        existing.QuietHoursEnd = req.QuietHoursEnd
    }
    if req.QuietHoursTimezone != "" {
        existing.QuietHoursTimezone = req.QuietHoursTimezone
    }
    
    // Digest
    if req.DigestEnabled != nil {
        existing.DigestEnabled = *req.DigestEnabled
    }
    if req.DigestFrequency != "" {
        existing.DigestFrequency = req.DigestFrequency
    }
    
    return existing
}
```

---

## 5. Channel Preference Checks

```go
func (s *PreferenceService) IsChannelEnabled(
    ctx context.Context,
    userID int64,
    channel enum.NotificationChannel,
) (bool, error) {
    pref, err := s.getCachedPreference(ctx, userID)
    if err != nil {
        return false, err
    }
    
    switch channel {
    case enum.ChannelEmail:
        return pref.EmailEnabled, nil
    case enum.ChannelPush:
        return pref.PushEnabled, nil
    case enum.ChannelInApp:
        return pref.InAppEnabled, nil
    default:
        return false, nil
    }
}

func (s *PreferenceService) GetEnabledChannels(
    ctx context.Context,
    userID int64,
) ([]enum.NotificationChannel, error) {
    pref, err := s.getCachedPreference(ctx, userID)
    if err != nil {
        return nil, err
    }
    
    var channels []enum.NotificationChannel
    
    if pref.EmailEnabled {
        channels = append(channels, enum.ChannelEmail)
    }
    if pref.PushEnabled {
        channels = append(channels, enum.ChannelPush)
    }
    if pref.InAppEnabled {
        channels = append(channels, enum.ChannelInApp)
    }
    
    return channels, nil
}
```

---

## 6. Type Preference Checks

```go
func (s *PreferenceService) IsTypeEnabled(
    ctx context.Context,
    userID int64,
    notificationType enum.NotificationType,
) (bool, error) {
    pref, err := s.getCachedPreference(ctx, userID)
    if err != nil {
        return false, err
    }
    
    switch notificationType {
    case enum.TypeTaskAssigned, enum.TypeTaskUpdated, enum.TypeTaskCompleted:
        return pref.TaskNotifications, nil
    case enum.TypeComment:
        return pref.CommentNotifications, nil
    case enum.TypeMention:
        return pref.MentionNotifications, nil
    case enum.TypeSystem:
        return pref.SystemNotifications, nil
    case enum.TypeMarketing:
        return pref.MarketingNotifications, nil
    default:
        return true, nil // Default: allow unknown types
    }
}

func (s *PreferenceService) GetMutedTypes(
    ctx context.Context,
    userID int64,
) ([]enum.NotificationType, error) {
    pref, err := s.getCachedPreference(ctx, userID)
    if err != nil {
        return nil, err
    }
    
    var mutedTypes []enum.NotificationType
    
    if !pref.TaskNotifications {
        mutedTypes = append(mutedTypes, enum.TypeTaskAssigned, enum.TypeTaskUpdated, enum.TypeTaskCompleted)
    }
    if !pref.CommentNotifications {
        mutedTypes = append(mutedTypes, enum.TypeComment)
    }
    // ... etc
    
    return mutedTypes, nil
}
```

---

## 7. Quiet Hours Logic

```go
func (s *PreferenceService) IsQuietHours(ctx context.Context, userID int64) (bool, error) {
    pref, err := s.getCachedPreference(ctx, userID)
    if err != nil {
        return false, err
    }
    
    if !pref.QuietHoursEnabled {
        return false, nil
    }
    
    if pref.QuietHoursStart == nil || pref.QuietHoursEnd == nil {
        return false, nil
    }
    
    return s.isCurrentTimeInQuietHours(pref), nil
}

func (s *PreferenceService) isCurrentTimeInQuietHours(pref *entity.NotificationPreferenceEntity) bool {
    // Load user's timezone
    loc, err := time.LoadLocation(pref.QuietHoursTimezone)
    if err != nil {
        loc = time.UTC
    }
    
    now := time.Now().In(loc)
    currentMinutes := now.Hour()*60 + now.Minute()
    
    startMinutes := *pref.QuietHoursStart // e.g., 22:00 = 1320
    endMinutes := *pref.QuietHoursEnd     // e.g., 07:00 = 420
    
    // Handle overnight quiet hours (e.g., 22:00 - 07:00)
    if startMinutes > endMinutes {
        // Quiet hours span midnight
        return currentMinutes >= startMinutes || currentMinutes < endMinutes
    }
    
    // Normal range (e.g., 13:00 - 14:00)
    return currentMinutes >= startMinutes && currentMinutes < endMinutes
}

func (s *PreferenceService) GetQuietHoursConfig(
    ctx context.Context,
    userID int64,
) (*entity.QuietHoursConfig, error) {
    pref, err := s.getCachedPreference(ctx, userID)
    if err != nil {
        return nil, err
    }
    
    return &entity.QuietHoursConfig{
        Enabled:  pref.QuietHoursEnabled,
        Start:    pref.QuietHoursStart,
        End:      pref.QuietHoursEnd,
        Timezone: pref.QuietHoursTimezone,
    }, nil
}
```

---

## 8. Delivery Decision Logic

```go
// ShouldDeliver checks all preferences to determine if notification should be delivered
func (s *PreferenceService) ShouldDeliver(
    ctx context.Context,
    userID int64,
    notification *entity.NotificationEntity,
    channel enum.NotificationChannel,
) (bool, error) {
    pref, err := s.getCachedPreference(ctx, userID)
    if err != nil {
        return false, err
    }
    
    // 1. Check channel enabled
    channelEnabled, _ := s.IsChannelEnabled(ctx, userID, channel)
    if !channelEnabled {
        return false, nil
    }
    
    // 2. Check notification type enabled
    typeEnabled, _ := s.IsTypeEnabled(ctx, userID, notification.Type)
    if !typeEnabled {
        return false, nil
    }
    
    // 3. Check quiet hours (skip for high priority)
    if notification.Priority != enum.PriorityUrgent {
        isQuiet, _ := s.IsQuietHours(ctx, userID)
        if isQuiet {
            // During quiet hours, only allow in-app for later viewing
            if channel != enum.ChannelInApp {
                return false, nil
            }
        }
    }
    
    return true, nil
}
```

---

## 9. Caching Layer

```go
const (
    PreferenceCacheKey = "notification:preference:%d"
    PreferenceCacheTTL = 10 * time.Minute
)

func (s *PreferenceService) getCachedPreference(
    ctx context.Context,
    userID int64,
) (*entity.NotificationPreferenceEntity, error) {
    cacheKey := fmt.Sprintf(PreferenceCacheKey, userID)
    
    // Try cache
    cached, err := s.redisPort.Get(ctx, cacheKey)
    if err == nil && cached != "" {
        var pref entity.NotificationPreferenceEntity
        if json.Unmarshal([]byte(cached), &pref) == nil {
            return &pref, nil
        }
    }
    
    // Fallback to database
    pref, err := s.preferencePort.GetByUserID(ctx, userID)
    if err != nil {
        return nil, err
    }
    
    if pref == nil {
        // Return default if not exists
        return s.createDefaultPreference(userID), nil
    }
    
    // Cache result
    if data, err := json.Marshal(pref); err == nil {
        s.redisPort.Set(ctx, cacheKey, string(data), PreferenceCacheTTL)
    }
    
    return pref, nil
}

func (s *PreferenceService) invalidateCache(ctx context.Context, userID int64) {
    cacheKey := fmt.Sprintf(PreferenceCacheKey, userID)
    s.redisPort.Del(ctx, cacheKey)
}
```

---

## 10. Validation

```go
var (
    ErrInvalidTimezone      = errors.New("invalid timezone")
    ErrInvalidQuietHours    = errors.New("invalid quiet hours range")
    ErrInvalidDigestFreq    = errors.New("invalid digest frequency")
)

func (s *PreferenceService) validatePreference(pref *entity.NotificationPreferenceEntity) error {
    // Validate timezone
    if pref.QuietHoursTimezone != "" {
        if _, err := time.LoadLocation(pref.QuietHoursTimezone); err != nil {
            return ErrInvalidTimezone
        }
    }
    
    // Validate quiet hours range (0-1439 minutes)
    if pref.QuietHoursStart != nil {
        if *pref.QuietHoursStart < 0 || *pref.QuietHoursStart >= 1440 {
            return ErrInvalidQuietHours
        }
    }
    if pref.QuietHoursEnd != nil {
        if *pref.QuietHoursEnd < 0 || *pref.QuietHoursEnd >= 1440 {
            return ErrInvalidQuietHours
        }
    }
    
    // Validate digest frequency
    if pref.DigestFrequency != "" {
        if !enum.IsValidDigestFrequency(pref.DigestFrequency) {
            return ErrInvalidDigestFreq
        }
    }
    
    return nil
}
```
