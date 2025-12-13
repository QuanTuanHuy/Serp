# TemplateService - Chi Tiết Thiết Kế

**File:** `core/service/template_service.go`  
**Responsibility:** Notification template management & rendering

---

## 1. Interface Definition

```go
package service

import (
    "context"
    
    "notification_service/src/core/domain/dto/request"
    "notification_service/src/core/domain/entity"
    "gorm.io/gorm"
)

type ITemplateService interface {
    // CRUD
    Create(ctx context.Context, tx *gorm.DB, req *request.CreateTemplateRequest) (*entity.NotificationTemplateEntity, error)
    GetByID(ctx context.Context, id int64) (*entity.NotificationTemplateEntity, error)
    GetByCode(ctx context.Context, code string) (*entity.NotificationTemplateEntity, error)
    GetList(ctx context.Context, params *request.GetTemplatesParams) ([]*entity.NotificationTemplateEntity, int64, error)
    Update(ctx context.Context, tx *gorm.DB, id int64, req *request.UpdateTemplateRequest) (*entity.NotificationTemplateEntity, error)
    Delete(ctx context.Context, tx *gorm.DB, id int64) error
    
    // Rendering
    RenderNotification(ctx context.Context, templateCode string, data map[string]interface{}) (*entity.RenderedNotification, error)
    RenderTitle(ctx context.Context, templateCode string, data map[string]interface{}) (string, error)
    RenderBody(ctx context.Context, templateCode string, data map[string]interface{}) (string, error)
    
    // Validation
    ValidateTemplate(template *entity.NotificationTemplateEntity) error
    ValidateTemplateData(templateCode string, data map[string]interface{}) error
}
```

---

## 2. Struct Definition

```go
package service

import (
    "bytes"
    "context"
    "errors"
    "fmt"
    "regexp"
    "text/template"
    "time"

    "notification_service/src/core/domain/dto/request"
    "notification_service/src/core/domain/entity"
    "notification_service/src/core/domain/enum"
    "notification_service/src/core/port/client"
    "notification_service/src/core/port/store"
    "gorm.io/gorm"
)

type TemplateService struct {
    templatePort  store.ITemplatePort
    redisPort     client.IRedisPort
    templateCache map[string]*template.Template
}

func NewTemplateService(
    templatePort store.ITemplatePort,
    redisPort client.IRedisPort,
) ITemplateService {
    return &TemplateService{
        templatePort:  templatePort,
        redisPort:     redisPort,
        templateCache: make(map[string]*template.Template),
    }
}
```

---

## 3. Template Entity

```go
// entity/notification_template_entity.go

type NotificationTemplateEntity struct {
    ID              int64
    Code            string   // Unique identifier: "TASK_ASSIGNED", "COMMENT_ADDED"
    Name            string   // Display name
    Description     string
    
    // Template content
    TitleTemplate   string   // "Task {{.TaskName}} assigned to you"
    BodyTemplate    string   // "{{.AssignerName}} assigned task..."
    
    // Defaults
    DefaultType     enum.NotificationType
    DefaultPriority enum.NotificationPriority
    Category        string
    
    // Icon/Action
    DefaultIcon     string
    DefaultAction   string   // Action URL template
    
    // Metadata
    RequiredFields  []string // ["TaskName", "AssignerName"]
    Active          bool
    
    CreatedAt       *int64
    UpdatedAt       *int64
}

// Rendered notification result
type RenderedNotification struct {
    Title     string
    Body      string
    Type      enum.NotificationType
    Priority  enum.NotificationPriority
    Category  string
    Icon      string
    ActionURL string
}
```

---

## 4. CRUD Operations

### 4.1. Create Template

```go
func (s *TemplateService) Create(
    ctx context.Context,
    tx *gorm.DB,
    req *request.CreateTemplateRequest,
) (*entity.NotificationTemplateEntity, error) {
    // 1. Check code uniqueness
    existing, _ := s.templatePort.GetByCode(ctx, req.Code)
    if existing != nil {
        return nil, errors.New("template code already exists")
    }
    
    // 2. Map to entity
    template := &entity.NotificationTemplateEntity{
        Code:            req.Code,
        Name:            req.Name,
        Description:     req.Description,
        TitleTemplate:   req.TitleTemplate,
        BodyTemplate:    req.BodyTemplate,
        DefaultType:     req.DefaultType,
        DefaultPriority: req.DefaultPriority,
        Category:        req.Category,
        DefaultIcon:     req.DefaultIcon,
        DefaultAction:   req.DefaultAction,
        RequiredFields:  req.RequiredFields,
        Active:          true,
    }
    
    // 3. Validate template syntax
    if err := s.ValidateTemplate(template); err != nil {
        return nil, err
    }
    
    // 4. Set timestamps
    now := time.Now().UnixMilli()
    template.CreatedAt = &now
    template.UpdatedAt = &now
    
    // 5. Persist
    created, err := s.templatePort.Create(ctx, tx, template)
    if err != nil {
        return nil, err
    }
    
    // 6. Clear cache
    s.invalidateCache(ctx, req.Code)
    
    return created, nil
}
```

### 4.2. Get by Code (with caching)

```go
const (
    TemplateCacheKey = "notification:template:%s"
    TemplateCacheTTL = 30 * time.Minute
)

func (s *TemplateService) GetByCode(
    ctx context.Context,
    code string,
) (*entity.NotificationTemplateEntity, error) {
    cacheKey := fmt.Sprintf(TemplateCacheKey, code)
    
    // Try cache
    cached, err := s.redisPort.Get(ctx, cacheKey)
    if err == nil && cached != "" {
        var template entity.NotificationTemplateEntity
        if json.Unmarshal([]byte(cached), &template) == nil {
            return &template, nil
        }
    }
    
    // Query database
    template, err := s.templatePort.GetByCode(ctx, code)
    if err != nil {
        return nil, err
    }
    
    if template == nil {
        return nil, fmt.Errorf("template not found: %s", code)
    }
    
    // Cache result
    if data, err := json.Marshal(template); err == nil {
        s.redisPort.Set(ctx, cacheKey, string(data), TemplateCacheTTL)
    }
    
    return template, nil
}
```

---

## 5. Template Rendering

### 5.1. Main Render Function

```go
func (s *TemplateService) RenderNotification(
    ctx context.Context,
    templateCode string,
    data map[string]interface{},
) (*entity.RenderedNotification, error) {
    // 1. Get template
    tmpl, err := s.GetByCode(ctx, templateCode)
    if err != nil {
        return nil, err
    }
    
    if !tmpl.Active {
        return nil, fmt.Errorf("template is inactive: %s", templateCode)
    }
    
    // 2. Validate required fields
    if err := s.validateRequiredFields(tmpl, data); err != nil {
        return nil, err
    }
    
    // 3. Render title
    title, err := s.renderString(tmpl.TitleTemplate, data)
    if err != nil {
        return nil, fmt.Errorf("failed to render title: %w", err)
    }
    
    // 4. Render body
    body, err := s.renderString(tmpl.BodyTemplate, data)
    if err != nil {
        return nil, fmt.Errorf("failed to render body: %w", err)
    }
    
    // 5. Render action URL
    actionURL := ""
    if tmpl.DefaultAction != "" {
        actionURL, _ = s.renderString(tmpl.DefaultAction, data)
    }
    
    return &entity.RenderedNotification{
        Title:     title,
        Body:      body,
        Type:      tmpl.DefaultType,
        Priority:  tmpl.DefaultPriority,
        Category:  tmpl.Category,
        Icon:      tmpl.DefaultIcon,
        ActionURL: actionURL,
    }, nil
}
```

### 5.2. String Rendering with Caching

```go
func (s *TemplateService) renderString(tmplStr string, data map[string]interface{}) (string, error) {
    // Check compiled template cache
    compiled, exists := s.templateCache[tmplStr]
    if !exists {
        var err error
        compiled, err = template.New("notification").
            Funcs(s.templateFuncs()).
            Parse(tmplStr)
        if err != nil {
            return "", err
        }
        s.templateCache[tmplStr] = compiled
    }
    
    // Execute template
    var buf bytes.Buffer
    if err := compiled.Execute(&buf, data); err != nil {
        return "", err
    }
    
    return buf.String(), nil
}

// Custom template functions
func (s *TemplateService) templateFuncs() template.FuncMap {
    return template.FuncMap{
        "truncate": func(s string, length int) string {
            if len(s) <= length {
                return s
            }
            return s[:length] + "..."
        },
        "formatDate": func(ts int64) string {
            t := time.UnixMilli(ts)
            return t.Format("Jan 02, 2006")
        },
        "formatTime": func(ts int64) string {
            t := time.UnixMilli(ts)
            return t.Format("15:04")
        },
        "lower": strings.ToLower,
        "upper": strings.ToUpper,
        "title": strings.Title,
    }
}
```

---

## 6. Validation

### 6.1. Template Syntax Validation

```go
var (
    ErrEmptyCode          = errors.New("template code cannot be empty")
    ErrEmptyTitle         = errors.New("title template cannot be empty")
    ErrInvalidCode        = errors.New("invalid template code format")
    ErrInvalidTemplate    = errors.New("invalid template syntax")
)

// Regex for template code: UPPER_SNAKE_CASE
var templateCodeRegex = regexp.MustCompile(`^[A-Z][A-Z0-9_]{2,50}$`)

func (s *TemplateService) ValidateTemplate(tmpl *entity.NotificationTemplateEntity) error {
    // Code validation
    if tmpl.Code == "" {
        return ErrEmptyCode
    }
    if !templateCodeRegex.MatchString(tmpl.Code) {
        return ErrInvalidCode
    }
    
    // Title template validation
    if tmpl.TitleTemplate == "" {
        return ErrEmptyTitle
    }
    if _, err := template.New("test").Parse(tmpl.TitleTemplate); err != nil {
        return fmt.Errorf("title template: %w", ErrInvalidTemplate)
    }
    
    // Body template validation
    if tmpl.BodyTemplate != "" {
        if _, err := template.New("test").Parse(tmpl.BodyTemplate); err != nil {
            return fmt.Errorf("body template: %w", ErrInvalidTemplate)
        }
    }
    
    // Action template validation
    if tmpl.DefaultAction != "" {
        if _, err := template.New("test").Parse(tmpl.DefaultAction); err != nil {
            return fmt.Errorf("action template: %w", ErrInvalidTemplate)
        }
    }
    
    return nil
}
```

### 6.2. Template Data Validation

```go
func (s *TemplateService) ValidateTemplateData(
    templateCode string,
    data map[string]interface{},
) error {
    tmpl, err := s.GetByCode(context.Background(), templateCode)
    if err != nil {
        return err
    }
    
    return s.validateRequiredFields(tmpl, data)
}

func (s *TemplateService) validateRequiredFields(
    tmpl *entity.NotificationTemplateEntity,
    data map[string]interface{},
) error {
    for _, field := range tmpl.RequiredFields {
        if _, exists := data[field]; !exists {
            return fmt.Errorf("missing required field: %s", field)
        }
    }
    return nil
}
```

---

## 7. Built-in Templates

```go
// Predefined templates (seeded at startup)
var BuiltInTemplates = []entity.NotificationTemplateEntity{
    {
        Code:            "TASK_ASSIGNED",
        Name:            "Task Assigned",
        TitleTemplate:   "Task assigned: {{.TaskName}}",
        BodyTemplate:    "{{.AssignerName}} assigned you to task \"{{.TaskName}}\" in project {{.ProjectName}}",
        DefaultType:     enum.TypeTaskAssigned,
        DefaultPriority: enum.PriorityNormal,
        Category:        "task",
        DefaultAction:   "/tasks/{{.TaskID}}",
        RequiredFields:  []string{"TaskName", "AssignerName", "TaskID"},
    },
    {
        Code:            "TASK_COMPLETED",
        Name:            "Task Completed",
        TitleTemplate:   "Task completed: {{.TaskName}}",
        BodyTemplate:    "{{.CompletedBy}} marked task \"{{.TaskName}}\" as complete",
        DefaultType:     enum.TypeTaskCompleted,
        DefaultPriority: enum.PriorityLow,
        Category:        "task",
        DefaultAction:   "/tasks/{{.TaskID}}",
        RequiredFields:  []string{"TaskName", "CompletedBy", "TaskID"},
    },
    {
        Code:            "COMMENT_ADDED",
        Name:            "New Comment",
        TitleTemplate:   "New comment on {{.TaskName}}",
        BodyTemplate:    "{{.AuthorName}}: \"{{.CommentPreview | truncate 100}}\"",
        DefaultType:     enum.TypeComment,
        DefaultPriority: enum.PriorityNormal,
        Category:        "comment",
        DefaultAction:   "/tasks/{{.TaskID}}#comment-{{.CommentID}}",
        RequiredFields:  []string{"TaskName", "AuthorName", "CommentPreview", "TaskID", "CommentID"},
    },
    {
        Code:            "MENTION",
        Name:            "You were mentioned",
        TitleTemplate:   "{{.MentionerName}} mentioned you",
        BodyTemplate:    "{{.MentionerName}} mentioned you in {{.Context}}: \"{{.Preview | truncate 100}}\"",
        DefaultType:     enum.TypeMention,
        DefaultPriority: enum.PriorityHigh,
        Category:        "mention",
        DefaultAction:   "{{.ActionURL}}",
        RequiredFields:  []string{"MentionerName", "Context", "Preview", "ActionURL"},
    },
    {
        Code:            "SYSTEM_ANNOUNCEMENT",
        Name:            "System Announcement",
        TitleTemplate:   "{{.Subject}}",
        BodyTemplate:    "{{.Message}}",
        DefaultType:     enum.TypeSystem,
        DefaultPriority: enum.PriorityNormal,
        Category:        "system",
        DefaultAction:   "{{.ActionURL}}",
        RequiredFields:  []string{"Subject", "Message"},
    },
}
```

---

## 8. Template Seeding

```go
// Called during service initialization
func (s *TemplateService) SeedBuiltInTemplates(ctx context.Context, tx *gorm.DB) error {
    for _, tmpl := range BuiltInTemplates {
        existing, _ := s.templatePort.GetByCode(ctx, tmpl.Code)
        if existing != nil {
            continue // Skip existing
        }
        
        tmplCopy := tmpl
        now := time.Now().UnixMilli()
        tmplCopy.CreatedAt = &now
        tmplCopy.UpdatedAt = &now
        tmplCopy.Active = true
        
        if _, err := s.templatePort.Create(ctx, tx, &tmplCopy); err != nil {
            return fmt.Errorf("failed to seed template %s: %w", tmpl.Code, err)
        }
    }
    return nil
}
```

---

## 9. Cache Management

```go
func (s *TemplateService) invalidateCache(ctx context.Context, code string) {
    // Invalidate specific template
    cacheKey := fmt.Sprintf(TemplateCacheKey, code)
    s.redisPort.Del(ctx, cacheKey)
    
    // Clear compiled template cache
    delete(s.templateCache, code)
}

func (s *TemplateService) InvalidateAllCache(ctx context.Context) {
    // Clear Redis cache by pattern
    s.redisPort.DelByPattern(ctx, "notification:template:*")
    
    // Clear in-memory compiled cache
    s.templateCache = make(map[string]*template.Template)
}
```
