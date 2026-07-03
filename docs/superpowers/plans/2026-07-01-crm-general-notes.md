# CRM Unified Notes System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a unified, polymorphic note-taking system across Leads, Accounts, Opportunities, and Activities on both the backend and frontend.

**Architecture:** Create a `notes` table with `entity_type` and `entity_id` columns to genericize relationship associations. Implement standard clean-architecture layers (Controller, UseCase, Service, Port, Adapter, Repository) on the backend, and write a reusable timeline React component on the frontend integrated with Next.js page slots.

**Tech Stack:** Spring Boot, Spring Data JPA, Flyway, RTK Query, React 19, Tailwind CSS.

---

### Task 1: Database Migration
Create the Flyway SQL migration script to define the polymorphic `notes` table.

**Files:**
- Create: `crm/src/main/resources/db/migration/V1_11__init_notes_table.sql`

- [ ] **Step 1: Write SQL migration file**
  Create `crm/src/main/resources/db/migration/V1_11__init_notes_table.sql` with the following content:
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

- [ ] **Step 2: Commit migration script**
  Commit this migration file to Git.

---

### Task 2: Backend Domain Entity, Model & DB Mapper
Create the backend model classes and mapping definitions.

**Files:**
- Create: `crm/src/main/java/serp/project/crm/core/domain/entity/NoteEntity.java`
- Create: `crm/src/main/java/serp/project/crm/infrastructure/store/model/NoteModel.java`
- Create: `crm/src/main/java/serp/project/crm/infrastructure/store/mapper/NoteMapper.java`

- [ ] **Step 1: Create NoteEntity.java**
  Write the domain entity representation:
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
      private String entityType;
      private Long entityId;
      private String content;
  }
  ```

- [ ] **Step 2: Create NoteModel.java**
  Write the JPA persistence model:
  ```java
  package serp.project.crm.infrastructure.store.model;

  import jakarta.persistence.*;
  import lombok.AllArgsConstructor;
  import lombok.Getter;
  import lombok.NoArgsConstructor;
  import lombok.Setter;
  import lombok.experimental.SuperBuilder;

  @Entity
  @Table(name = "notes")
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

- [ ] **Step 3: Create NoteMapper.java**
  Create the mapper class to convert between the entity and model:
  ```java
  package serp.project.crm.infrastructure.store.mapper;

  import org.springframework.stereotype.Component;
  import serp.project.crm.core.domain.entity.NoteEntity;
  import serp.project.crm.infrastructure.store.model.NoteModel;
  import lombok.RequiredArgsConstructor;
  import java.util.List;

  @Component
  @RequiredArgsConstructor
  public class NoteMapper extends BaseMapper {

      public NoteEntity toEntity(NoteModel model) {
          if (model == null) {
              return null;
          }
          return NoteEntity.builder()
                  .id(model.getId())
                  .tenantId(model.getTenantId())
                  .entityType(model.getEntityType())
                  .entityId(model.getEntityId())
                  .content(model.getContent())
                  .createdAt(toTimestamp(model.getCreatedAt()))
                  .updatedAt(toTimestamp(model.getUpdatedAt()))
                  .createdBy(model.getCreatedBy())
                  .updatedBy(model.getUpdatedBy())
                  .build();
      }

      public NoteModel toModel(NoteEntity entity) {
          if (entity == null) {
              return null;
          }
          return NoteModel.builder()
                  .id(entity.getId())
                  .tenantId(entity.getTenantId())
                  .entityType(entity.getEntityType())
                  .entityId(entity.getEntityId())
                  .content(entity.getContent())
                  .createdAt(toLocalDateTime(entity.getCreatedAt()))
                  .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                  .createdBy(entity.getCreatedBy())
                  .updatedBy(entity.getUpdatedBy())
                  .build();
      }

      public List<NoteEntity> toEntityList(List<NoteModel> models) {
          if (models == null) {
              return null;
          }
          return models.stream().map(this::toEntity).toList();
      }
  }
  ```

- [ ] **Step 4: Commit DB mapping files**
  Commit the three new Java files.

---

### Task 3: JPA Repository, Port & Adapter
Create the database persistence interfaces and their implementations.

**Files:**
- Create: `crm/src/main/java/serp/project/crm/infrastructure/store/repository/NoteRepository.java`
- Create: `crm/src/main/java/serp/project/crm/core/port/store/INotePort.java`
- Create: `crm/src/main/java/serp/project/crm/infrastructure/store/adapter/NoteAdapter.java`

- [ ] **Step 1: Create NoteRepository.java**
  Define the standard JPA repository interface:
  ```java
  package serp.project.crm.infrastructure.store.repository;

  import org.springframework.data.domain.Page;
  import org.springframework.data.domain.Pageable;
  import org.springframework.data.jpa.repository.JpaRepository;
  import org.springframework.stereotype.Repository;
  import serp.project.crm.infrastructure.store.model.NoteModel;

  import java.util.Optional;

  @Repository
  public interface NoteRepository extends JpaRepository<NoteModel, Long> {
      Optional<NoteModel> findByIdAndTenantId(Long id, Long tenantId);

      Page<NoteModel> findByTenantIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
              Long tenantId, String entityType, Long entityId, Pageable pageable);
  }
  ```

- [ ] **Step 2: Create INotePort.java**
  Create the store port boundary:
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

- [ ] **Step 3: Create NoteAdapter.java**
  Implement the adapter which wraps the repository and mapper:
  ```java
  package serp.project.crm.infrastructure.store.adapter;

  import lombok.RequiredArgsConstructor;
  import org.springframework.data.util.Pair;
  import org.springframework.stereotype.Component;
  import serp.project.crm.core.domain.dto.PageRequest;
  import serp.project.crm.core.domain.entity.NoteEntity;
  import serp.project.crm.core.port.store.INotePort;
  import serp.project.crm.infrastructure.store.mapper.NoteMapper;
  import serp.project.crm.infrastructure.store.repository.NoteRepository;

  import java.util.List;
  import java.util.Optional;

  @Component
  @RequiredArgsConstructor
  public class NoteAdapter implements INotePort {

      private final NoteRepository noteRepository;
      private final NoteMapper noteMapper;

      @Override
      public NoteEntity save(NoteEntity noteEntity) {
          var model = noteMapper.toModel(noteEntity);
          return noteMapper.toEntity(noteRepository.save(model));
      }

      @Override
      public Optional<NoteEntity> findById(Long id, Long tenantId) {
          return noteRepository.findByIdAndTenantId(id, tenantId)
                  .map(noteMapper::toEntity);
      }

      @Override
      public Pair<List<NoteEntity>, Long> findByEntity(String entityType, Long entityId, Long tenantId,
              PageRequest pageRequest) {
          var pageable = noteMapper.toPageable(pageRequest);
          var page = noteRepository.findByTenantIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
                  tenantId, entityType, entityId, pageable)
                  .map(noteMapper::toEntity);
          return noteMapper.pageToPair(page);
      }

      @Override
      public void delete(Long id, Long tenantId) {
          noteRepository.findByIdAndTenantId(id, tenantId)
                  .ifPresent(noteRepository::delete);
      }
  }
  ```

- [ ] **Step 4: Commit persistence files**
  Commit the three newly created repository/port files.

---

### Task 4: Service Layer & Implementation
Create the business service interface and concrete implementation.

**Files:**
- Create: `crm/src/main/java/serp/project/crm/core/service/INoteService.java`
- Create: `crm/src/main/java/serp/project/crm/core/service/impl/NoteService.java`

- [ ] **Step 1: Create INoteService.java**
  ```java
  package serp.project.crm.core.service;

  import org.springframework.data.util.Pair;
  import serp.project.crm.core.domain.dto.PageRequest;
  import serp.project.crm.core.domain.entity.NoteEntity;

  import java.util.List;

  public interface INoteService {
      NoteEntity createNote(NoteEntity note, Long tenantId);
      NoteEntity updateNote(Long id, String content, Long userId, Long tenantId);
      Pair<List<NoteEntity>, Long> getNotesByEntity(String entityType, Long entityId, Long tenantId, PageRequest pageRequest);
      void deleteNote(Long id, Long tenantId);
  }
  ```

- [ ] **Step 2: Create NoteService.java**
  Implement the business service methods:
  ```java
  package serp.project.crm.core.service.impl;

  import lombok.RequiredArgsConstructor;
  import org.springframework.data.util.Pair;
  import org.springframework.stereotype.Service;
  import org.springframework.transaction.annotation.Transactional;
  import serp.project.crm.core.domain.entity.NoteEntity;
  import serp.project.crm.core.port.store.INotePort;
  import serp.project.crm.core.service.INoteService;
  import serp.project.crm.core.exception.AppException;
  import serp.project.crm.core.domain.constant.ErrorMessage;

  import java.util.List;

  @Service
  @RequiredArgsConstructor
  public class NoteService implements INoteService {

      private final INotePort notePort;

      @Override
      @Transactional
      public NoteEntity createNote(NoteEntity note, Long tenantId) {
          note.setTenantId(tenantId);
          return notePort.save(note);
      }

      @Override
      @Transactional
      public NoteEntity updateNote(Long id, String content, Long userId, Long tenantId) {
          NoteEntity existing = notePort.findById(id, tenantId)
                  .orElseThrow(() -> new AppException("Note not found"));
          existing.setContent(content);
          existing.setUpdatedBy(userId);
          return notePort.save(existing);
      }

      @Override
      @Transactional(readOnly = true)
      public Pair<List<NoteEntity>, Long> getNotesByEntity(String entityType, Long entityId, Long tenantId,
              PageRequest pageRequest) {
          pageRequest.validate();
          return notePort.findByEntity(entityType, entityId, tenantId, pageRequest);
      }

      @Override
      @Transactional
      public void deleteNote(Long id, Long tenantId) {
          notePort.delete(id, tenantId);
      }
  }
  ```

- [ ] **Step 3: Commit service files**
  Commit the service interface and implementation class.

---

### Task 5: Request/Response DTOs & Mapper
Create the API DTOs and mapper component.

**Files:**
- Create: `crm/src/main/java/serp/project/crm/core/domain/dto/request/CreateNoteRequest.java`
- Create: `crm/src/main/java/serp/project/crm/core/domain/dto/request/UpdateNoteRequest.java`
- Create: `crm/src/main/java/serp/project/crm/core/domain/dto/response/NoteResponse.java`
- Create: `crm/src/main/java/serp/project/crm/core/mapper/NoteDtoMapper.java`

- [ ] **Step 1: Create CreateNoteRequest.java**
  ```java
  package serp.project.crm.core.domain.dto.request;

  import jakarta.validation.constraints.NotBlank;
  import jakarta.validation.constraints.NotNull;
  import lombok.AllArgsConstructor;
  import lombok.Builder;
  import lombok.Data;
  import lombok.NoArgsConstructor;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public class CreateNoteRequest {
      @NotBlank(message = "Entity type is required")
      private String entityType;

      @NotNull(message = "Entity ID is required")
      private Long entityId;

      @NotBlank(message = "Content cannot be empty")
      private String content;
  }
  ```

- [ ] **Step 2: Create UpdateNoteRequest.java**
  ```java
  package serp.project.crm.core.domain.dto.request;

  import jakarta.validation.constraints.NotBlank;
  import lombok.AllArgsConstructor;
  import lombok.Builder;
  import lombok.Data;
  import lombok.NoArgsConstructor;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public class UpdateNoteRequest {
      @NotBlank(message = "Content cannot be empty")
      private String content;
  }
  ```

- [ ] **Step 3: Create NoteResponse.java**
  ```java
  package serp.project.crm.core.domain.dto.response;

  import lombok.AllArgsConstructor;
  import lombok.Builder;
  import lombok.Data;
  import lombok.NoArgsConstructor;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public class NoteResponse {
      private Long id;
      private Long tenantId;
      private String entityType;
      private Long entityId;
      private String content;
      private Long createdAt;
      private Long updatedAt;
      private Long createdBy;
      private Long updatedBy;
  }
  ```

- [ ] **Step 4: Create NoteDtoMapper.java**
  ```java
  package serp.project.crm.core.mapper;

  import org.springframework.stereotype.Component;
  import serp.project.crm.core.domain.dto.request.CreateNoteRequest;
  import serp.project.crm.core.domain.dto.response.NoteResponse;
  import serp.project.crm.core.domain.entity.NoteEntity;

  @Component
  public class NoteDtoMapper {

      public NoteEntity toEntity(CreateNoteRequest request) {
          if (request == null) {
              return null;
          }
          return NoteEntity.builder()
                  .entityType(request.getEntityType())
                  .entityId(request.getEntityId())
                  .content(request.getContent())
                  .build();
      }

      public NoteResponse toResponse(NoteEntity entity) {
          if (entity == null) {
              return null;
          }
          return NoteResponse.builder()
                  .id(entity.getId())
                  .tenantId(entity.getTenantId())
                  .entityType(entity.getEntityType())
                  .entityId(entity.getEntityId())
                  .content(entity.getContent())
                  .createdAt(entity.getCreatedAt())
                  .updatedAt(entity.getUpdatedAt())
                  .createdBy(entity.getCreatedBy())
                  .updatedBy(entity.getUpdatedBy())
                  .build();
      }
  }
  ```

- [ ] **Step 5: Commit DTOs and DtoMapper**
  Commit the 4 files.

---

### Task 6: Use Case & REST Controller
Create the business use case and controller endpoints mapping requests to Use Case actions.

**Files:**
- Create: `crm/src/main/java/serp/project/crm/core/usecase/NoteUseCase.java`
- Create: `crm/src/main/java/serp/project/crm/ui/controller/NoteController.java`

- [ ] **Step 1: Create NoteUseCase.java**
  ```java
  package serp.project.crm.core.usecase;

  import lombok.RequiredArgsConstructor;
  import org.springframework.stereotype.Service;
  import org.springframework.transaction.annotation.Transactional;
  import serp.project.crm.core.domain.dto.GeneralResponse;
  import serp.project.crm.core.domain.dto.PageRequest;
  import serp.project.crm.core.domain.dto.PageResponse;
  import serp.project.crm.core.domain.dto.request.CreateNoteRequest;
  import serp.project.crm.core.domain.dto.response.NoteResponse;
  import serp.project.crm.core.domain.entity.NoteEntity;
  import serp.project.crm.core.mapper.NoteDtoMapper;
  import serp.project.crm.core.service.INoteService;
  import serp.project.crm.kernel.utils.ResponseUtils;

  import java.util.List;

  @Service
  @RequiredArgsConstructor
  public class NoteUseCase {

      private final INoteService noteService;
      private final NoteDtoMapper noteDtoMapper;
      private final ResponseUtils responseUtils;

      @Transactional(rollbackFor = Exception.class)
      public GeneralResponse<?> createNote(CreateNoteRequest request, Long userId, Long tenantId) {
          NoteEntity noteEntity = noteDtoMapper.toEntity(request);
          noteEntity.setCreatedBy(userId);
          NoteEntity saved = noteService.createNote(noteEntity, tenantId);
          return responseUtils.success(noteDtoMapper.toResponse(saved), "Note created successfully");
      }

      @Transactional(rollbackFor = Exception.class)
      public GeneralResponse<?> updateNote(Long id, String content, Long userId, Long tenantId) {
          NoteEntity updated = noteService.updateNote(id, content, userId, tenantId);
          return responseUtils.success(noteDtoMapper.toResponse(updated), "Note updated successfully");
      }

      @Transactional(readOnly = true)
      public GeneralResponse<?> getNotesByEntity(String entityType, Long entityId, Long tenantId, PageRequest pageRequest) {
          var result = noteService.getNotesByEntity(entityType, entityId, tenantId, pageRequest);
          List<NoteResponse> responses = result.getFirst().stream()
                  .map(noteDtoMapper::toResponse)
                  .toList();
          return responseUtils.success(PageResponse.of(responses, pageRequest, result.getSecond()));
      }

      @Transactional(rollbackFor = Exception.class)
      public GeneralResponse<?> deleteNote(Long id, Long tenantId) {
          noteService.deleteNote(id, tenantId);
          return responseUtils.status("Note deleted successfully");
      }
  }
  ```

- [ ] **Step 2: Create NoteController.java**
  ```java
  package serp.project.crm.ui.controller;

  import jakarta.validation.Valid;
  import lombok.RequiredArgsConstructor;
  import org.springframework.http.ResponseEntity;
  import org.springframework.web.bind.annotation.*;
  import serp.project.crm.core.domain.dto.PageRequest;
  import serp.project.crm.core.domain.dto.request.CreateNoteRequest;
  import serp.project.crm.core.domain.dto.request.UpdateNoteRequest;
  import serp.project.crm.core.usecase.NoteUseCase;
  import serp.project.crm.kernel.utils.AuthUtils;

  @RestController
  @RequestMapping("/api/v1/notes")
  @RequiredArgsConstructor
  public class NoteController {

      private final NoteUseCase noteUseCase;
      private final AuthUtils authUtils;

      @PostMapping
      public ResponseEntity<?> createNote(@Valid @RequestBody CreateNoteRequest request) {
          Long userId = authUtils.getCurrentUserId().orElse(null);
          Long tenantId = authUtils.getCurrentTenantId().orElse(null);
          if (userId == null || tenantId == null) {
              return ResponseEntity.status(401).build();
          }

          var response = noteUseCase.createNote(request, userId, tenantId);
          return ResponseEntity.status(response.getCode()).body(response);
      }

      @PutMapping("/{id}")
      public ResponseEntity<?> updateNote(
              @PathVariable Long id,
              @Valid @RequestBody UpdateNoteRequest request) {
          Long userId = authUtils.getCurrentUserId().orElse(null);
          Long tenantId = authUtils.getCurrentTenantId().orElse(null);
          if (userId == null || tenantId == null) {
              return ResponseEntity.status(401).build();
          }

          var response = noteUseCase.updateNote(id, request.getContent(), userId, tenantId);
          return ResponseEntity.status(response.getCode()).body(response);
      }

      @GetMapping
      public ResponseEntity<?> getNotes(
              @RequestParam String entityType,
              @RequestParam Long entityId,
              @RequestParam(defaultValue = "1") Integer page,
              @RequestParam(defaultValue = "10") Integer size) {
          Long tenantId = authUtils.getCurrentTenantId().orElse(null);
          if (tenantId == null) {
              return ResponseEntity.status(401).build();
          }

          PageRequest pageRequest = PageRequest.builder().page(page).size(size).build();
          var response = noteUseCase.getNotesByEntity(entityType, entityId, tenantId, pageRequest);
          return ResponseEntity.status(response.getCode()).body(response);
      }

      @DeleteMapping("/{id}")
      public ResponseEntity<?> deleteNote(@PathVariable Long id) {
          Long tenantId = authUtils.getCurrentTenantId().orElse(null);
          if (tenantId == null) {
              return ResponseEntity.status(401).build();
          }

          var response = noteUseCase.deleteNote(id, tenantId);
          return ResponseEntity.status(response.getCode()).body(response);
      }
  }
  ```

- [ ] **Step 3: Commit REST controller and UseCase**
  Commit these backend endpoints.

---

### Task 7: Backend Build & Unit Verification
Build and compile the Spring Boot application to verify there are no compilation or DI issues.

**Files:**
- Test: Run Maven clean and compile

- [ ] **Step 1: Run compile command**
  Run: `mvnw.cmd clean compile` (from the `crm/` directory)
  Expected: BUILD SUCCESS

- [ ] **Step 2: Start backend integration test**
  Run: `mvnw.cmd test` (from the `crm/` directory)
  Expected: BUILD SUCCESS and all tests pass

---

### Task 8: Frontend API Integration
Register the notes API endpoints with RTK Query.

**Files:**
- Create: `serp_web/src/modules/crm/api/noteApi.ts`
- Modify: `serp_web/src/modules/crm/api/crmApi.ts:1-14`

- [ ] **Step 1: Create noteApi.ts**
  Create `serp_web/src/modules/crm/api/noteApi.ts` with the following RTK Query setup:
  ```typescript
  import { api } from '@/lib/store/api';
  import type { APIResponse, PaginatedResponse } from '../types';

  export interface Note {
    id: string;
    tenantId: string;
    entityType: 'LEAD' | 'ACCOUNT' | 'OPPORTUNITY' | 'ACTIVITY';
    entityId: string;
    content: string;
    createdAt: string;
    updatedAt: string;
    createdBy: string;
    updatedBy: string;
  }

  export interface CreateNoteRequest {
    entityType: 'LEAD' | 'ACCOUNT' | 'OPPORTUNITY' | 'ACTIVITY';
    entityId: number;
    content: string;
  }

  export interface UpdateNoteRequest {
    content: string;
  }

  export const noteApi = api.injectEndpoints({
    endpoints: (builder) => ({
      getNotes: builder.query<
        APIResponse<PaginatedResponse<Note>>,
        { entityType: string; entityId: string; page?: number; size?: number }
      >({
        query: ({ entityType, entityId, page = 1, size = 20 }) => ({
          url: '/notes',
          method: 'GET',
          params: { entityType, entityId, page, size },
        }),
        extraOptions: { service: 'crm' },
        providesTags: (result, error, { entityType, entityId }) => [
          { type: 'Note' as const, id: `${entityType}-${entityId}-LIST` },
        ],
      }),

      createNote: builder.mutation<APIResponse<Note>, CreateNoteRequest>({
        query: (data) => ({
          url: '/notes',
          method: 'POST',
          body: data,
        }),
        extraOptions: { service: 'crm' },
        invalidatesTags: (result, error, { entityType, entityId }) => [
          { type: 'Note', id: `${entityType}-${entityId}-LIST` },
        ],
      }),

      updateNote: builder.mutation<
        APIResponse<Note>,
        { id: string; data: UpdateNoteRequest; entityType: string; entityId: string }
      >({
        query: ({ id, data }) => ({
          url: `/notes/${id}`,
          method: 'PUT',
          body: data,
        }),
        extraOptions: { service: 'crm' },
        invalidatesTags: (result, error, { entityType, entityId }) => [
          { type: 'Note', id: `${entityType}-${entityId}-LIST` },
        ],
      }),

      deleteNote: builder.mutation<
        APIResponse<any>,
        { id: string; entityType: string; entityId: string }
      >({
        query: ({ id }) => ({
          url: `/notes/${id}`,
          method: 'DELETE',
        }),
        extraOptions: { service: 'crm' },
        invalidatesTags: (result, error, { entityType, entityId }) => [
          { type: 'Note', id: `${entityType}-${entityId}-LIST` },
        ],
      }),
    }),
  });

  export const {
    useGetNotesQuery,
    useCreateNoteMutation,
    useUpdateNoteMutation,
    useDeleteNoteMutation,
  } = noteApi;
  ```

- [ ] **Step 2: Export in crmApi.ts**
  Add the export statement in `serp_web/src/modules/crm/api/crmApi.ts`:
  ```diff
   export * from './customerApi';
   export * from './leadApi';
+  export * from './noteApi';
   export * from './opportunityApi';
  ```

- [ ] **Step 3: Commit frontend API changes**
  Commit the newly created API file and modified `crmApi.ts`.

---

### Task 9: Reusable Notes UI Tab Component
Build the `<CRMNotesTab>` React component for managing notes with a clean, dynamic timeline.

**Files:**
- Create: `serp_web/src/modules/crm/components/shared/CRMNotesTab.tsx`
- Modify: `serp_web/src/modules/crm/components/shared/index.ts:1-9`

- [ ] **Step 1: Write CRMNotesTab.tsx**
  Implement the React functional component with timeline history, add/edit/delete states, and real-time mapping of assignee/creator names:
  ```tsx
  import React, { useState } from 'react';
  import {
    useGetNotesQuery,
    useCreateNoteMutation,
    useUpdateNoteMutation,
    useDeleteNoteMutation,
  } from '../../api/crmApi';
  import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
  import { selectOrganizationId } from '@/modules/account/store';
  import { useAppSelector } from '@/shared/hooks';
  import {
    Button,
    Textarea,
    Avatar,
    AvatarFallback,
    Card,
    CardContent,
  } from '@/shared/components/ui';
  import { MessageSquare, Edit2, Trash2, Check, X, Send } from 'lucide-react';
  import { toast } from 'sonner';

  interface CRMNotesTabProps {
    entityType: 'LEAD' | 'ACCOUNT' | 'OPPORTUNITY' | 'ACTIVITY';
    entityId: string;
  }

  export const CRMNotesTab: React.FC<CRMNotesTabProps> = ({
    entityType,
    entityId,
  }) => {
    const [newNoteContent, setNewNoteContent] = useState('');
    const [editingNoteId, setEditingNoteId] = useState<string | null>(null);
    const [editContent, setEditContent] = useState('');

    const organizationId = useAppSelector(selectOrganizationId);
    const { data: notesData, isLoading: isNotesLoading } = useGetNotesQuery({
      entityType,
      entityId,
    });
    const { data: orgUsersResponse } = useGetOrganizationUsersQuery(
      { organizationId: organizationId as number, page: 0, pageSize: 100 },
      { skip: !organizationId }
    );

    const [createNote, { isLoading: isCreating }] = useCreateNoteMutation();
    const [updateNote, { isLoading: isUpdating }] = useUpdateNoteMutation();
    const [deleteNote, { isLoading: isDeleting }] = useDeleteNoteMutation();

    const notes = notesData?.data?.data ?? [];
    const users = orgUsersResponse?.data?.items ?? [];

    const getUserName = (userId: string) => {
      const user = users.find((u) => String(u.id) === userId);
      if (!user) return `User #${userId}`;
      const name = [user.firstName, user.lastName].filter(Boolean).join(' ');
      return name || user.email;
    };

    const getUserInitials = (userId: string) => {
      const name = getUserName(userId);
      return name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2);
    };

    const handleAddNote = async () => {
      if (!newNoteContent.trim()) return;
      try {
        await createNote({
          entityType,
          entityId: Number(entityId),
          content: newNoteContent.trim(),
        }).unwrap();
        setNewNoteContent('');
        toast.success('Note added successfully');
      } catch {
        toast.error('Failed to add note');
      }
    };

    const handleUpdateNote = async (noteId: string) => {
      if (!editContent.trim()) return;
      try {
        await updateNote({
          id: noteId,
          data: { content: editContent.trim() },
          entityType,
          entityId,
        }).unwrap();
        setEditingNoteId(null);
        toast.success('Note updated successfully');
      } catch {
        toast.error('Failed to update note');
      }
    };

    const handleDeleteNote = async (noteId: string) => {
      if (!confirm('Are you sure you want to delete this note?')) return;
      try {
        await deleteNote({ id: noteId, entityType, entityId }).unwrap();
        toast.success('Note deleted successfully');
      } catch {
        toast.error('Failed to delete note');
      }
    };

    const formatDate = (dateString?: string) => {
      if (!dateString) return '';
      return new Date(dateString).toLocaleString('vi-VN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    };

    return (
      <div className='space-y-6'>
        <Card className='border-none shadow-sm'>
          <CardContent className='p-4'>
            <div className='flex gap-3'>
              <Textarea
                value={newNoteContent}
                onChange={(e) => setNewNoteContent(e.target.value)}
                placeholder='Type a note or log updates here...'
                disabled={isCreating}
                rows={3}
                className='resize-none'
              />
            </div>
            <div className='mt-3 flex justify-end'>
              <Button
                onClick={handleAddNote}
                disabled={!newNoteContent.trim() || isCreating}
                className='gap-2'
              >
                <Send className='h-4 w-4' />
                Add Note
              </Button>
            </div>
          </CardContent>
        </Card>

        {isNotesLoading ? (
          <div className='text-center py-6 text-muted-foreground'>
            Loading notes...
          </div>
        ) : notes.length > 0 ? (
          <div className='relative pl-6 before:absolute before:left-3 before:top-2 before:bottom-2 before:w-0.5 before:bg-muted space-y-6'>
            {notes.map((note) => {
              const isEditing = editingNoteId === note.id;
              return (
                <div key={note.id} className='relative group'>
                  <div className='absolute -left-9 top-1.5 flex h-6 w-6 items-center justify-center rounded-full border bg-background text-muted-foreground'>
                    <Avatar className='h-6 w-6'>
                      <AvatarFallback className='text-[8px]'>
                        {getUserInitials(note.createdBy)}
                      </AvatarFallback>
                    </Avatar>
                  </div>

                  <div className='rounded-lg border bg-card p-4 shadow-sm transition-shadow hover:shadow-md'>
                    <div className='flex items-center justify-between gap-3 mb-2'>
                      <div>
                        <span className='font-semibold text-sm text-foreground mr-2'>
                          {getUserName(note.createdBy)}
                        </span>
                        <span className='text-xs text-muted-foreground'>
                          {formatDate(note.createdAt)}
                        </span>
                      </div>
                      <div className='flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity'>
                        {!isEditing && (
                          <>
                            <Button
                              variant='ghost'
                              size='icon'
                              className='h-7 w-7 text-muted-foreground hover:text-foreground'
                              onClick={() => {
                                setEditingNoteId(note.id);
                                setEditContent(note.content);
                              }}
                            >
                              <Edit2 className='h-3.5 w-3.5' />
                            </Button>
                            <Button
                              variant='ghost'
                              size='icon'
                              className='h-7 w-7 text-destructive hover:text-destructive/80'
                              onClick={() => handleDeleteNote(note.id)}
                            >
                              <Trash2 className='h-3.5 w-3.5' />
                            </Button>
                          </>
                        )}
                      </div>
                    </div>

                    {isEditing ? (
                      <div className='space-y-3 mt-2'>
                        <Textarea
                          value={editContent}
                          onChange={(e) => setEditContent(e.target.value)}
                          rows={3}
                        />
                        <div className='flex justify-end gap-2'>
                          <Button
                            variant='outline'
                            size='sm'
                            onClick={() => setEditingNoteId(null)}
                          >
                            <X className='mr-1.5 h-3.5 w-3.5' />
                            Cancel
                          </Button>
                          <Button
                            size='sm'
                            onClick={() => handleUpdateNote(note.id)}
                            disabled={!editContent.trim() || isUpdating}
                          >
                            <Check className='mr-1.5 h-3.5 w-3.5' />
                            Save
                          </Button>
                        </div>
                      </div>
                    ) : (
                      <p className='text-sm text-foreground/80 whitespace-pre-wrap'>
                        {note.content}
                      </p>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className='flex flex-col items-center justify-center rounded-lg border border-dashed py-12 text-center text-muted-foreground'>
            <MessageSquare className='mb-3 h-8 w-8 text-muted-foreground/40' />
            <p className='text-sm'>No notes added yet.</p>
          </div>
        )}
      </div>
    );
  };

  export default CRMNotesTab;
  ```

- [ ] **Step 2: Export in index.ts**
  Add the export statement in `serp_web/src/modules/crm/components/shared/index.ts`:
  ```diff
   export * from './StatusBadge';
   export * from './ExportDropdown';
+  export * from './CRMNotesTab';
   export * from './CRMDatePicker';
  ```

- [ ] **Step 3: Commit shared component files**
  Commit the new UI component file and the modified `index.ts`.

---

### Task 10: Lead Detail Integration
Mount the reusable `<CRMNotesTab>` inside the Lead Detail tab slot.

**Files:**
- Modify: `serp_web/src/modules/crm/pages/leads/LeadDetailPageEnhanced.tsx:669-679`

- [ ] **Step 1: Edit LeadDetailPageEnhanced.tsx**
  Replace the mock card block with the CRMNotesTab component:
  ```diff
               </TabsContent>
   
               <TabsContent value='notes' className='mt-4 space-y-4'>
-                <Card className='border-none shadow-sm'>
-                  <CardContent className='flex flex-col items-center justify-center py-12'>
-                    <MessageSquare className='mb-4 h-12 w-12 text-muted-foreground/50' />
-                    <p className='text-muted-foreground'>
-                      Dedicated lead notes API is not integrated yet.
-                    </p>
-                  </CardContent>
-                </Card>
+                <CRMNotesTab entityType='LEAD' entityId={leadId} />
               </TabsContent>
             </Tabs>
  ```
  Ensure `CRMNotesTab` is properly imported at the top of the file:
  `import { CRMNotesTab } from '../../components/shared/CRMNotesTab';`

- [ ] **Step 2: Commit details page integration**
  Commit the modified `LeadDetailPageEnhanced.tsx`.

---

### Task 11: End-to-End Verification
Verify the notes flow functions end-to-end on both local servers.

- [ ] **Step 1: Start backend service**
  Run: `docker-compose -f docker-compose.dev.yml up -d` (to ensure DB and keycloak are running)
  Run: `mvnw.cmd spring-boot:run` (from the `crm/` directory to run the CRM backend on port 8086)

- [ ] **Step 2: Start frontend service**
  Run: `npm run dev` (from the `serp_web/` directory to run the Next.js frontend)

- [ ] **Step 3: Perform manual verification**
  1. Open a browser and navigate to a lead detail page (e.g. `http://localhost:3000/crm/leads/1`).
  2. Select the **Notes** tab.
  3. Type a text note and click **Add Note**.
  4. Verify the note renders immediately in the timeline with your correct user name/initials.
  5. Click the **Edit** icon, change the note content, and verify it updates.
  6. Click the **Delete** icon and confirm, verifying the note is removed.
