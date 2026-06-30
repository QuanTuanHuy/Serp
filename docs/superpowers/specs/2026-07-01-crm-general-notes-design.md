# CRM Unified Notes System Design Specification

This document details the design for introducing a unified, polymorphic note-taking system in the CRM module to support **Leads, Accounts, Opportunities, and Activities**.

- **Author**: Antigravity
- **Date**: 2026-07-01

---

## 1. Database Schema Design (PostgreSQL)

We introduce a polymorphic table `notes` mapping one-to-many relationships across several entity types using `entity_type` and `entity_id`.

### Migration Script (`V1_11__init_notes_table.sql`)
Location: `crm/src/main/resources/db/migration/V1_11__init_notes_table.sql`

```sql
CREATE TABLE IF NOT EXISTS notes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    CONSTRAINT chk_notes_entity_type 
        CHECK (entity_type IN ('LEAD', 'ACCOUNT', 'OPPORTUNITY', 'ACTIVITY'))
);

CREATE INDEX IF NOT EXISTS idx_notes_tenant_entity 
    ON notes (tenant_id, entity_type, entity_id);
```

---

## 2. Backend Domain & Infrastructure Model

### Domain Entity (`NoteEntity.java`)
Location: `crm/src/main/java/serp/project/crm/core/domain/entity/NoteEntity.java`

```java
package serp.project.crm.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class NoteEntity extends BaseEntity {
    private String entityType; // "LEAD", "ACCOUNT", "OPPORTUNITY", "ACTIVITY"
    private Long entityId;
    private String content;
}
```

### Database Model (`NoteModel.java`)
Location: `crm/src/main/java/serp/project/crm/infrastructure/store/model/NoteModel.java`

```java
package serp.project.crm.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notes", indexes = {
        @Index(name = "idx_notes_tenant_entity", columnList = "tenant_id, entity_type, entity_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class NoteModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
}
```

---

## 3. Service, Use Case & REST Controller

### Port Layer (`INotePort.java` & `NoteAdapter.java`)
We define a port and adapter to interact with the repository.

#### `INotePort.java`
Location: `crm/src/main/java/serp/project/crm/core/port/store/INotePort.java`
```java
package serp.project.crm.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.NoteEntity;

import java.util.List;
import java.util.Optional;

public interface INotePort {
    NoteEntity save(NoteEntity note);
    Optional<NoteEntity> findById(Long id, Long tenantId);
    Pair<List<NoteEntity>, Long> findByEntity(String entityType, Long entityId, Long tenantId, PageRequest pageRequest);
    void delete(Long id, Long tenantId);
}
```

### Use Case Layer (`NoteUseCase.java`)
Location: `crm/src/main/java/serp/project/crm/core/usecase/NoteUseCase.java`

Provides transaction management and mappings between DTOs and Domain Entities.

* `createNote`
* `updateNote`
* `getNotesByEntity`
* `deleteNote`

### API Endpoint Map (`NoteController.java`)
Location: `crm/src/main/java/serp/project/crm/ui/controller/NoteController.java`

* `POST /api/v1/notes` - Create a note
* `GET /api/v1/notes` - Get paginated list of notes for a specific entity
* `PUT /api/v1/notes/{id}` - Update note content
* `DELETE /api/v1/notes/{id}` - Delete note

---

## 4. Frontend Integration

### RTK Query Endpoint (`noteApi.ts`)
Location: `serp_web/src/modules/crm/api/noteApi.ts`

Exposes:
- `useGetNotesQuery`
- `useCreateNoteMutation`
- `useUpdateNoteMutation`
- `useDeleteNoteMutation`

### Reusable UI Tab Component (`CRMNotesTab.tsx`)
Location: `serp_web/src/modules/crm/components/shared/CRMNotesTab.tsx`

Allows:
- Creating new notes.
- Viewing a chronological timeline of comments with mapped creator avatars and names (resolved via `useGetOrganizationUsersQuery`).
- Editing or deleting comments inline.

---

## 5. Next Steps

1. Execute the database migration SQL.
2. Code the backend files (Model, Port, Service, Controller).
3. Code the frontend files (API endpoint definitions, reusable component, detail views).
4. Verify end-to-end integration.
