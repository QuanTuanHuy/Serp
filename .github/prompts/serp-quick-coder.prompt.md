---
name: serp-quick-coder
description: Quick code generation for Serp ERP with Clean Architecture patterns - Go, Java, Python services
agent: agent
---

# Serp Quick Coder

Fast, focused code generation for the **Serp ERP System**. Generate production-ready code following strict architectural patterns.

## Quick Reference

### Service Mapping
| Service | Lang | Port | Package/Module |
|---------|------|------|----------------|
| api_gateway | Go | 8080 | `src/` |
| account | Java | 8081 | `serp.project.account` |
| crm | Java | 8086 | `serp.project.crm` |
| sales | Go | 8087 | `src/` |
| ptm_task | Go | 8083 | `src/` |
| ptm_schedule | Go | 8084 | `src/` |
| serp_llm | Python | 8087 | `src/` |
| mailservice | Java | 8087 | `serp.project.mailservice` |

### Layer Paths
```
core/domain/entity/      # Business entities
core/domain/dto/         # Request/Response DTOs  
core/port/store/         # Repository interfaces
core/service/            # Business logic
core/usecase/            # Orchestration
infrastructure/store/    # DB adapters, models
ui/controller/           # HTTP handlers
```

---

## Go Snippets

### Entity
```go
type ${Name}Entity struct {
    ID          int64
    ${Fields}
    CreatedAt   *int64
    UpdatedAt   *int64
}
```

### Model (GORM)
```go
type ${Name}Model struct {
    ID        int64     `gorm:"primaryKey;autoIncrement"`
    ${Fields}
    CreatedAt time.Time `gorm:"not null;autoCreateTime"`
    UpdatedAt time.Time `gorm:"not null;autoUpdateTime"`
}

func (m *${Name}Model) TableName() string {
    return "${table_name}"
}
```

### Port Interface
```go
type I${Name}Port interface {
    Create(ctx context.Context, tx *gorm.DB, entity *entity.${Name}Entity) (*entity.${Name}Entity, error)
    GetByID(ctx context.Context, tx *gorm.DB, id int64) (*entity.${Name}Entity, error)
    Update(ctx context.Context, tx *gorm.DB, entity *entity.${Name}Entity) (*entity.${Name}Entity, error)
    Delete(ctx context.Context, tx *gorm.DB, id int64) error
}
```

### Adapter
```go
type ${Name}Adapter struct {
    db *gorm.DB
}

func New${Name}Adapter(db *gorm.DB) port.I${Name}Port {
    return &${Name}Adapter{db: db}
}
```

### Service
```go
type ${Name}Service struct {
    ${name}Port port.I${Name}Port
}

func New${Name}Service(port port.I${Name}Port) *${Name}Service {
    return &${Name}Service{${name}Port: port}
}
```

### UseCase
```go
type ${Name}UseCase struct {
    txService     *transaction.TransactionService
    ${name}Service *service.${Name}Service
}

func New${Name}UseCase(
    txService *transaction.TransactionService,
    ${name}Service *service.${Name}Service,
) *${Name}UseCase {
    return &${Name}UseCase{
        txService:     txService,
        ${name}Service: ${name}Service,
    }
}
```

### Controller
```go
type ${Name}Controller struct {
    ${name}UseCase *usecase.${Name}UseCase
}

func New${Name}Controller(uc *usecase.${Name}UseCase) *${Name}Controller {
    return &${Name}Controller{${name}UseCase: uc}
}
```

### FX Registration
```go
// In cmd/bootstrap/all.go
fx.Provide(adapter.New${Name}Adapter),
fx.Provide(service.New${Name}Service),
fx.Provide(usecase.New${Name}UseCase),
fx.Provide(controller.New${Name}Controller),
fx.Invoke(router.Register${Name}Routes),
```

---

## Java Snippets

### Entity
```java
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
public class ${Name}Entity extends BaseEntity {
    private Long id;
    ${fields}
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Model (JPA)
```java
@Entity
@Table(name = "${table_name}")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
public class ${Name}Model extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    ${fields}
}
```

### Port Interface
```java
public interface I${Name}Port {
    ${Name}Entity create(${Name}Entity entity);
    Optional<${Name}Entity> findById(Long id);
    ${Name}Entity update(${Name}Entity entity);
    void deleteById(Long id);
}
```

### Adapter
```java
@Component
@RequiredArgsConstructor
public class ${Name}Adapter implements I${Name}Port {
    private final ${Name}Repository repository;
    private final ${Name}ModelMapper mapper;
    
    @Override
    public ${Name}Entity create(${Name}Entity entity) {
        ${Name}Model model = mapper.toModel(entity);
        return mapper.toEntity(repository.save(model));
    }
}
```

### Service
```java
@Service
@RequiredArgsConstructor
public class ${Name}Service {
    private final I${Name}Port ${name}Port;
    
    public ${Name}Entity create(Create${Name}Request request) {
        ${Name}Entity entity = ${Name}Entity.builder()
            // .field(request.getField())
            .build();
        return ${name}Port.create(entity);
    }
}
```

### UseCase
```java
@Component
@RequiredArgsConstructor
public class ${Name}UseCase {
    private final ${Name}Service ${name}Service;
    private final ${Name}Mapper mapper;
    
    @Transactional
    public ${Name}Response create(Create${Name}Request request) {
        ${Name}Entity entity = ${name}Service.create(request);
        return mapper.toResponse(entity);
    }
}
```

### Controller
```java
@RestController
@RequestMapping("/api/v1/${path}")
@RequiredArgsConstructor
public class ${Name}Controller {
    private final ${Name}UseCase ${name}UseCase;
    
    @PostMapping
    public ResponseEntity<ApiResponse<${Name}Response>> create(
            @Valid @RequestBody Create${Name}Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(${name}UseCase.create(request)));
    }
}
```

---

## Python Snippets

### Entity (Pydantic)
```python
class ${Name}Entity(BaseModel):
    id: Optional[int] = None
    ${fields}
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    
    class Config:
        from_attributes = True
```

### Model (SQLAlchemy)
```python
class ${Name}Model(Base):
    __tablename__ = "${table_name}"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    ${fields}
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, onupdate=func.now())
```

### Port (Protocol)
```python
class I${Name}Port(Protocol):
    async def create(self, entity: ${Name}Entity) -> ${Name}Entity: ...
    async def get_by_id(self, id: int) -> Optional[${Name}Entity]: ...
    async def update(self, entity: ${Name}Entity) -> ${Name}Entity: ...
    async def delete(self, id: int) -> bool: ...
```

### FastAPI Router
```python
router = APIRouter(prefix="/api/v1/${path}", tags=["${tag}"])

@router.post("", response_model=${Name}Response, status_code=status.HTTP_201_CREATED)
async def create_${name}(
    request: Create${Name}Request,
    user_id: int = Depends(get_current_user_id),
    usecase: ${Name}UseCase = Depends()
):
    return await usecase.create(user_id, request)
```

---

## Common Patterns

### Transaction with Kafka (Go)
```go
result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
    entity, err := u.service.Create(ctx, tx, request)
    if err != nil {
        return nil, err
    }
    return entity, nil
})

if err == nil {
    u.kafkaProducer.SendMessageAsync(ctx, topic, entity.ID, entity)
}
```

### Extract User from Context (Go)
```go
userID, err := utils.GetUserIDFromContext(ctx)
if err != nil {
    utils.ResponseError(ctx, http.StatusUnauthorized, "unauthorized")
    return
}
```

### Extract User from Context (Java)
```java
Long userId = AuthUtils.getCurrentUserId();
Long orgId = AuthUtils.getCurrentOrganizationId();
```

### Response Utils (Go)
```go
utils.ResponseSuccess(ctx, http.StatusOK, data)
utils.ResponseError(ctx, http.StatusBadRequest, "error message")
utils.ResponsePagination(ctx, http.StatusOK, data, total, page, size)
```

---

## File Headers

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/
```

```java
/*
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */
```

```python
"""
Author: QuanTuanHuy
Description: Part of Serp Project
"""
```

---

## Quick Commands

```bash
# Run Go service
cd ptm_task && ./run-dev.sh

# Run Java service  
cd crm && ./run-dev.sh

# Run Python service
cd serp_llm && ./run-dev.sh

# Run frontend
cd serp_web && npm run dev

# Start infrastructure
docker-compose -f docker-compose.dev.yml up -d
```
