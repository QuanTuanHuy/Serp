# Discuss Conversation Details Sidebar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace unsupported call/video chat actions with a Slack/Discord-style right sidebar that shows conversation overview, members, and shared file history.

**Architecture:** Backend adds a narrow channel attachment-history read path through controller → use case → service → port → JPA repository, with channel membership enforcement in the use case. Frontend adds an RTK Query endpoint and a focused sidebar component owned by `ChatWindow`, reusing existing member and attachment behavior.

**Tech Stack:** Java 21, Spring Boot 3.5, JUnit 5, Mockito, Spring Data JPA, Next.js 15, React 19, TypeScript, RTK Query, Tailwind CSS, Shadcn/Radix UI primitives.

---

## File structure

Backend files:

- Modify `discuss_service/src/main/java/serp/project/discuss_service/core/service/IAttachmentService.java` — add paginated channel attachment method.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/AttachmentService.java` — implement tenant-scoped paginated channel attachment lookup.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/core/port/store/IAttachmentPort.java` — expose paginated channel attachment lookup.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/adapter/AttachmentAdapter.java` — adapt paginated repository result to domain entities.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/repository/IAttachmentRepository.java` — add Spring Data page query ordered newest-first.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/core/usecase/AttachmentUseCase.java` — enforce membership and return `PaginatedResponse<AttachmentResponse>`.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/ui/controller/ChannelController.java` — add `GET /channels/{channelId}/attachments`.
- Modify `discuss_service/src/test/java/serp/project/discuss_service/core/service/impl/AttachmentServiceTest.java` — service regression tests.
- Modify `discuss_service/src/test/java/serp/project/discuss_service/core/usecase/AttachmentUseCaseTest.java` — membership and pagination regression tests.

Frontend files:

- Modify `serp_web/src/modules/discuss/types/message.ts` — include `channelId` and storage field names on `Attachment`.
- Modify `serp_web/src/modules/discuss/types/channel.ts` — include backend conversation overview counters used by the sidebar.
- Modify `serp_web/src/modules/discuss/api/transformers.ts` — normalize attachment IDs, `channelId`, and storage fields.
- Modify `serp_web/src/modules/discuss/api/attachments.api.ts` — add `getChannelAttachments` endpoint.
- Create `serp_web/src/modules/discuss/components/ConversationDetailsSidebar.tsx` — right sidebar shell with Overview, Members, and Files tabs.
- Modify `serp_web/src/modules/discuss/components/ChatWindow.tsx` — remove unsupported call buttons and mount sidebar.
- Modify `serp_web/src/modules/discuss/components/index.ts` — export sidebar.

---

## Task 1: Backend service and port tests for channel attachment history

**Files:**

- Modify: `discuss_service/src/test/java/serp/project/discuss_service/core/service/impl/AttachmentServiceTest.java`

- [ ] **Step 1: Add failing service tests**

Add imports:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
```

Add this nested class before `GenerateDownloadUrlTests`:

```java
    @Nested
    @DisplayName("getAttachmentsByChannel")
    class GetAttachmentsByChannelTests {

        @Test
        @DisplayName("should return tenant scoped channel attachments page")
        void testGetAttachmentsByChannel_HasAttachments_ReturnsTenantScopedPage() {
            // Given
            AttachmentEntity image = TestDataFactory.createImageAttachment();
            image.setId(1L);

            AttachmentEntity document = TestDataFactory.createDocumentAttachment();
            document.setId(2L);

            AttachmentEntity otherTenant = TestDataFactory.createVideoAttachment();
            otherTenant.setId(3L);
            otherTenant.setTenantId(999L);

            Pageable pageable = PageRequest.of(0, 20);
            Page<AttachmentEntity> page = new PageImpl<>(
                    List.of(image, document, otherTenant),
                    pageable,
                    3
            );

            when(attachmentPort.findByChannelId(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.TENANT_ID,
                    pageable
            )).thenReturn(page);

            // When
            Page<AttachmentEntity> result = attachmentService.getAttachmentsByChannel(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.TENANT_ID,
                    pageable
            );

            // Then
            assertEquals(2, result.getContent().size());
            assertEquals(2, result.getTotalElements());
            assertTrue(result.getContent().stream()
                    .allMatch(attachment -> attachment.getTenantId().equals(TestDataFactory.TENANT_ID)));
            verify(attachmentPort).findByChannelId(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.TENANT_ID,
                    pageable
            );
        }

        @Test
        @DisplayName("should return empty page when channel has no attachments")
        void testGetAttachmentsByChannel_NoAttachments_ReturnsEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            when(attachmentPort.findByChannelId(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.TENANT_ID,
                    pageable
            )).thenReturn(Page.empty(pageable));

            // When
            Page<AttachmentEntity> result = attachmentService.getAttachmentsByChannel(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.TENANT_ID,
                    pageable
            );

            // Then
            assertTrue(result.isEmpty());
            assertEquals(0, result.getTotalElements());
        }
    }
```

- [ ] **Step 2: Run the focused service test and verify it fails**

Run from `discuss_service/`:

```bash
mvn -Dtest=AttachmentServiceTest#getAttachmentsByChannel test
```

Expected: compilation fails because `getAttachmentsByChannel(...)` and paginated `findByChannelId(...)` are not defined yet.

- [ ] **Step 3: Commit the failing tests**

```bash
git add discuss_service/src/test/java/serp/project/discuss_service/core/service/impl/AttachmentServiceTest.java
git commit -m "test(ds): cover channel attachment history service"
```

---

## Task 2: Backend service, port, adapter, and repository implementation

**Files:**

- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/service/IAttachmentService.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/AttachmentService.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/port/store/IAttachmentPort.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/adapter/AttachmentAdapter.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/repository/IAttachmentRepository.java`

- [ ] **Step 1: Add service interface method**

In `IAttachmentService.java`, add imports:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
```

Add this method after `getAttachmentsByMessage(...)`:

```java
    Page<AttachmentEntity> getAttachmentsByChannel(Long channelId, Long tenantId, Pageable pageable);
```

- [ ] **Step 2: Add port interface method**

In `IAttachmentPort.java`, add imports:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
```

Replace the current list-returning channel method:

```java
    List<AttachmentEntity> findByChannelId(Long channelId);
```

with:

```java
    Page<AttachmentEntity> findByChannelId(Long channelId, Long tenantId, Pageable pageable);
```

- [ ] **Step 3: Add repository query**

In `IAttachmentRepository.java`, add imports:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
```

Replace:

```java
    List<AttachmentModel> findByChannelId(Long channelId);
```

with:

```java
    Page<AttachmentModel> findByChannelIdAndTenantIdOrderByCreatedAtDesc(
            Long channelId,
            Long tenantId,
            Pageable pageable);
```

- [ ] **Step 4: Implement adapter method**

In `AttachmentAdapter.java`, add imports:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
```

Replace:

```java
    @Override
    public List<AttachmentEntity> findByChannelId(Long channelId) {
        return attachmentMapper.toEntityList(
                attachmentRepository.findByChannelId(channelId));
    }
```

with:

```java
    @Override
    public Page<AttachmentEntity> findByChannelId(Long channelId, Long tenantId, Pageable pageable) {
        return attachmentRepository
                .findByChannelIdAndTenantIdOrderByCreatedAtDesc(channelId, tenantId, pageable)
                .map(attachmentMapper::toEntity);
    }
```

- [ ] **Step 5: Implement service method**

In `AttachmentService.java`, add imports:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
```

Add this method after `getAttachmentsByMessage(...)`:

```java
    @Override
    public Page<AttachmentEntity> getAttachmentsByChannel(Long channelId, Long tenantId, Pageable pageable) {
        Page<AttachmentEntity> attachments = attachmentPort.findByChannelId(channelId, tenantId, pageable);
        List<AttachmentEntity> tenantScopedContent = attachments.getContent().stream()
                .filter(attachment -> attachment.getTenantId().equals(tenantId))
                .toList();

        if (tenantScopedContent.size() == attachments.getContent().size()) {
            return attachments;
        }

        return new org.springframework.data.domain.PageImpl<>(
                tenantScopedContent,
                pageable,
                tenantScopedContent.size()
        );
    }
```

- [ ] **Step 6: Run service test and verify it passes**

Run from `discuss_service/`:

```bash
mvn -Dtest=AttachmentServiceTest#getAttachmentsByChannel test
```

Expected: PASS.

- [ ] **Step 7: Commit implementation**

```bash
git add discuss_service/src/main/java/serp/project/discuss_service/core/service/IAttachmentService.java discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/AttachmentService.java discuss_service/src/main/java/serp/project/discuss_service/core/port/store/IAttachmentPort.java discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/adapter/AttachmentAdapter.java discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/repository/IAttachmentRepository.java
git commit -m "feat(ds): add paginated channel attachment store path"
```

---

## Task 3: Backend use case membership and pagination tests

**Files:**

- Modify: `discuss_service/src/test/java/serp/project/discuss_service/core/usecase/AttachmentUseCaseTest.java`

- [ ] **Step 1: Add failing use case tests**

Add imports:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import serp.project.discuss_service.core.domain.dto.response.PaginatedResponse;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.service.IChannelMemberService;
```

Add mock field:

```java
    @Mock
    private IChannelMemberService channelMemberService;
```

Add this nested class before `GetDownloadUrlTests`:

```java
    @Nested
    @DisplayName("getAttachmentsByChannel")
    class GetAttachmentsByChannelTests {

        @Test
        @DisplayName("should return paginated attachment history for channel member")
        void testGetAttachmentsByChannel_ChannelMember_ReturnsPaginatedResponses() {
            // Given
            AttachmentEntity image = TestDataFactory.createImageAttachment();
            image.setId(1L);
            AttachmentEntity document = TestDataFactory.createDocumentAttachment();
            document.setId(2L);

            PageRequest pageable = PageRequest.of(0, 20);
            Page<AttachmentEntity> attachmentPage = new PageImpl<>(
                    List.of(image, document),
                    pageable,
                    2
            );

            when(channelMemberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1))
                    .thenReturn(true);
            when(attachmentService.getAttachmentsByChannel(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.TENANT_ID,
                    pageable
            )).thenReturn(attachmentPage);

            // When
            PaginatedResponse<AttachmentResponse> result = attachmentUseCase.getAttachmentsByChannel(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID,
                    0,
                    20
            );

            // Then
            assertEquals(2, result.getItems().size());
            assertEquals(0, result.getPage());
            assertEquals(20, result.getPageSize());
            assertEquals(2, result.getTotalItems());
            verify(channelMemberService).isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1);
            verify(attachmentService).getAttachmentsByChannel(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.TENANT_ID,
                    pageable
            );
            verify(attachmentUrlService, never()).enrichWithUrls(anyList());
        }

        @Test
        @DisplayName("should reject non members")
        void testGetAttachmentsByChannel_NotMember_ThrowsNotChannelMember() {
            // Given
            when(channelMemberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_2))
                    .thenReturn(false);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> attachmentUseCase.getAttachmentsByChannel(
                            TestDataFactory.CHANNEL_ID,
                            TestDataFactory.USER_ID_2,
                            TestDataFactory.TENANT_ID,
                            0,
                            20
                    ));

            assertEquals(ErrorCode.NOT_CHANNEL_MEMBER.getMessage(), exception.getMessage());
            verify(attachmentService, never()).getAttachmentsByChannel(anyLong(), anyLong(), any());
        }
    }
```

- [ ] **Step 2: Run the focused use case test and verify it fails**

Run from `discuss_service/`:

```bash
mvn -Dtest=AttachmentUseCaseTest#getAttachmentsByChannel test
```

Expected: compilation fails because `AttachmentUseCase` does not inject `IChannelMemberService` and does not define `getAttachmentsByChannel(...)`.

- [ ] **Step 3: Commit the failing use case tests**

```bash
git add discuss_service/src/test/java/serp/project/discuss_service/core/usecase/AttachmentUseCaseTest.java
git commit -m "test(ds): cover channel attachment history use case"
```

---

## Task 4: Backend use case and controller endpoint

**Files:**

- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/usecase/AttachmentUseCase.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/ui/controller/ChannelController.java`

- [ ] **Step 1: Update AttachmentUseCase dependencies**

In `AttachmentUseCase.java`, add imports:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import serp.project.discuss_service.core.domain.dto.response.PaginatedResponse;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.service.IChannelMemberService;
```

Add field:

```java
    private final IChannelMemberService channelMemberService;
```

- [ ] **Step 2: Add use case method**

Add this method after `getAttachmentsByMessage(...)`:

```java
    public PaginatedResponse<AttachmentResponse> getAttachmentsByChannel(
            Long channelId,
            Long userId,
            Long tenantId,
            Integer page,
            Integer pageSize) {
        if (!channelMemberService.isMember(channelId, userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        int safePage = page == null || page < 0 ? 0 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        PageRequest pageable = PageRequest.of(safePage, safePageSize);

        Page<AttachmentEntity> attachments = attachmentService.getAttachmentsByChannel(
                channelId,
                tenantId,
                pageable
        );

        List<AttachmentResponse> responses = attachments.getContent().stream()
                .map(AttachmentResponse::fromEntity)
                .toList();

        return PaginatedResponse.of(
                responses,
                attachments.getNumber(),
                attachments.getSize(),
                attachments.getTotalElements()
        );
    }
```

- [ ] **Step 3: Add controller endpoint**

In `ChannelController.java`, add import:

```java
import serp.project.discuss_service.core.domain.dto.response.AttachmentResponse;
import serp.project.discuss_service.core.usecase.AttachmentUseCase;
```

Add field:

```java
    private final AttachmentUseCase attachmentUseCase;
```

Add this endpoint after `getChannelMembers(...)`:

```java
    @GetMapping("/{channelId}/attachments")
    public ResponseEntity<GeneralResponse<PaginatedResponse<AttachmentResponse>>> getChannelAttachments(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer pageSize) {
        Long userId = authContext.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authContext.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_ID_REQUIRED));

        log.debug("User {} getting attachments for channel {}", userId, channelId);

        PaginatedResponse<AttachmentResponse> response = attachmentUseCase.getAttachmentsByChannel(
                channelId,
                userId,
                tenantId,
                page,
                pageSize
        );

        return ResponseEntity.ok(responseUtils.success(response));
    }
```

- [ ] **Step 4: Run focused backend tests**

Run from `discuss_service/`:

```bash
mvn -Dtest=AttachmentUseCaseTest#getAttachmentsByChannel,AttachmentServiceTest#getAttachmentsByChannel test
```

Expected: PASS.

- [ ] **Step 5: Run backend compile gate**

Run from `discuss_service/`:

```bash
mvn -q -DskipTests compile
```

Expected: compile succeeds.

- [ ] **Step 6: Commit backend endpoint**

```bash
git add discuss_service/src/main/java/serp/project/discuss_service/core/usecase/AttachmentUseCase.java discuss_service/src/main/java/serp/project/discuss_service/ui/controller/ChannelController.java
git commit -m "feat(ds): expose channel attachment history"
```

---

## Task 5: Frontend attachment types and API endpoint

**Files:**

- Modify: `serp_web/src/modules/discuss/types/message.ts`
- Modify: `serp_web/src/modules/discuss/types/channel.ts`
- Modify: `serp_web/src/modules/discuss/api/transformers.ts`
- Modify: `serp_web/src/modules/discuss/api/attachments.api.ts`

- [ ] **Step 1: Update Attachment type**

In `message.ts`, replace the `Attachment` interface with:

```ts
export interface Attachment extends BaseEntity {
  messageId: string;
  channelId: string;
  tenantId?: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  fileExtension?: string;
  storageProvider?: string;
  storageBucket?: string;
  storageKey?: string;
  storageUrl?: string;
  s3Key?: string;
  s3Bucket?: string;
  downloadUrl?: string;
  thumbnailUrl?: string;
  width?: number;
  height?: number;
  metadata?: Record<string, any>;
  fileSizeFormatted?: string;
  urlExpiresAt?: string;
}
```

- [ ] **Step 2: Update Channel type for overview fields**

In `channel.ts`, add these optional fields to the `Channel` interface after `memberCount`:

```ts
  messageCount?: number;
  isPrivate?: boolean;
  createdBy?: string;
```

- [ ] **Step 3: Normalize attachment transformer**

In `transformers.ts`, replace `transformAttachment` with:

```ts
export const transformAttachment = (backendAttachment: any): Attachment => {
  const storageKey = backendAttachment.storageKey ?? backendAttachment.s3Key;
  const storageBucket =
    backendAttachment.storageBucket ?? backendAttachment.s3Bucket;

  return {
    ...backendAttachment,
    id: String(backendAttachment.id),
    messageId: String(backendAttachment.messageId),
    channelId: backendAttachment.channelId
      ? String(backendAttachment.channelId)
      : '',
    tenantId: backendAttachment.tenantId
      ? String(backendAttachment.tenantId)
      : undefined,
    storageKey,
    storageBucket,
    s3Key: storageKey,
    s3Bucket: storageBucket,
  };
};
```

- [ ] **Step 4: Add channel attachments endpoint**

In `attachments.api.ts`, update import:

```ts
import type {
  Attachment,
  APIResponse,
  PaginatedResponse,
  PaginationParams,
} from '../types';
```

Add endpoint after `getMessageAttachments`:

```ts
    /**
     * Get paginated attachment history for a channel.
     */
    getChannelAttachments: builder.query<
      APIResponse<PaginatedResponse<Attachment>>,
      { channelId: string; pagination: PaginationParams }
    >({
      query: ({ channelId, pagination }) => ({
        url: `/channels/${channelId}/attachments`,
        params: {
          page: pagination.page - 1,
          size: pagination.limit,
        },
      }),
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: {
          ...response.data,
          items: response.data.items.map(transformAttachment),
        },
      }),
      providesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-FILES-${channelId}` },
      ],
    }),
```

Add export:

```ts
  useGetChannelAttachmentsQuery,
```

- [ ] **Step 5: Run frontend type check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: type check reaches existing project result with no new Discuss attachment type errors.

- [ ] **Step 6: Commit FE API work**

```bash
git add serp_web/src/modules/discuss/types/message.ts serp_web/src/modules/discuss/types/channel.ts serp_web/src/modules/discuss/api/transformers.ts serp_web/src/modules/discuss/api/attachments.api.ts
git commit -m "feat(web): add discuss channel attachments api"
```

---

## Task 6: Conversation details sidebar component

**Files:**

- Create: `serp_web/src/modules/discuss/components/ConversationDetailsSidebar.tsx`
- Modify: `serp_web/src/modules/discuss/components/index.ts`

- [ ] **Step 1: Create sidebar component**

Create `ConversationDetailsSidebar.tsx`:

```tsx
/*
Author: QuanTuanHuy
Description: Part of Serp Project - Conversation details sidebar for Discuss
*/

'use client';

import React, { useMemo, useState } from 'react';
import {
  Badge,
  Button,
  ScrollArea,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import {
  File as FileIcon,
  FileText,
  Image,
  Info,
  Loader2,
  Users,
  X,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetAttachmentDownloadUrlQuery,
  useGetChannelAttachmentsQuery,
} from '../api/discussApi';
import type { Attachment, Channel } from '../types';
import { ChannelMembersPanel } from './ChannelMembersPanel';

interface ConversationDetailsSidebarProps {
  channel: Channel;
  currentUserId: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const formatDate = (value?: string) => {
  if (!value) return '—';
  const date = new Date(Number.isNaN(Number(value)) ? value : Number(value));
  if (Number.isNaN(date.getTime())) return '—';
  return date.toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

const formatFileSize = (bytes?: number) => {
  if (!bytes) return '0 B';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

const getFileIcon = (attachment: Attachment) => {
  if (attachment.fileType?.startsWith('image/')) return Image;
  if (attachment.fileType === 'application/pdf') return FileText;
  return FileIcon;
};

function FileRow({ attachment }: { attachment: Attachment }) {
  const [downloadRequested, setDownloadRequested] = useState(false);
  const { data, isFetching } = useGetAttachmentDownloadUrlQuery(attachment.id, {
    skip: !downloadRequested,
  });

  React.useEffect(() => {
    if (data?.data?.downloadUrl) {
      window.open(data.data.downloadUrl, '_blank', 'noopener,noreferrer');
      setDownloadRequested(false);
    }
  }, [data]);

  const Icon = getFileIcon(attachment);

  return (
    <div className='flex items-center gap-3 rounded-lg border border-slate-200 p-3 dark:border-slate-700'>
      <div className='flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-violet-50 dark:bg-violet-950/40'>
        <Icon className='h-5 w-5 text-violet-600 dark:text-violet-300' />
      </div>
      <div className='min-w-0 flex-1'>
        <p className='truncate text-sm font-medium text-slate-900 dark:text-slate-100'>
          {attachment.fileName}
        </p>
        <p className='text-xs text-slate-500 dark:text-slate-400'>
          {formatFileSize(attachment.fileSize)} · {formatDate(attachment.createdAt)}
        </p>
      </div>
      <Button
        variant='ghost'
        size='sm'
        disabled={isFetching}
        onClick={() => setDownloadRequested(true)}
      >
        {isFetching ? <Loader2 className='h-4 w-4 animate-spin' /> : 'Open'}
      </Button>
    </div>
  );
}

export const ConversationDetailsSidebar: React.FC<
  ConversationDetailsSidebarProps
> = ({ channel, currentUserId, open, onOpenChange }) => {
  const [memberDialogOpen, setMemberDialogOpen] = useState(false);

  const { data: filesResponse, isLoading: filesLoading } =
    useGetChannelAttachmentsQuery(
      { channelId: channel.id, pagination: { page: 1, limit: 30 } },
      { skip: !open }
    );

  const files = filesResponse?.data?.items ?? [];

  const channelTypeLabel = useMemo(
    () => channel.type.charAt(0) + channel.type.slice(1).toLowerCase(),
    [channel.type]
  );

  return (
    <aside
      className={cn(
        'fixed inset-y-0 right-0 z-40 w-full max-w-sm translate-x-full border-l border-slate-200 bg-white shadow-xl transition-transform duration-200 dark:border-slate-700 dark:bg-slate-900 lg:static lg:z-auto lg:w-96 lg:max-w-none lg:shadow-none',
        open && 'translate-x-0'
      )}
    >
      <div className='flex h-full flex-col'>
        <div className='flex items-center justify-between border-b border-slate-200 px-4 py-3 dark:border-slate-700'>
          <div>
            <h3 className='font-semibold text-slate-900 dark:text-slate-100'>
              Conversation details
            </h3>
            <p className='truncate text-sm text-slate-500 dark:text-slate-400'>
              {channel.name}
            </p>
          </div>
          <Button variant='ghost' size='sm' onClick={() => onOpenChange(false)}>
            <X className='h-5 w-5' />
          </Button>
        </div>

        <Tabs defaultValue='overview' className='flex min-h-0 flex-1 flex-col'>
          <TabsList className='mx-4 mt-4 grid grid-cols-3'>
            <TabsTrigger value='overview'>Overview</TabsTrigger>
            <TabsTrigger value='members'>Members</TabsTrigger>
            <TabsTrigger value='files'>Files</TabsTrigger>
          </TabsList>

          <ScrollArea className='min-h-0 flex-1 px-4 py-4'>
            <TabsContent value='overview' className='mt-0 space-y-4'>
              <div className='flex items-center gap-3'>
                <div className='flex h-12 w-12 items-center justify-center rounded-xl bg-violet-100 dark:bg-violet-950/50'>
                  <Info className='h-6 w-6 text-violet-600 dark:text-violet-300' />
                </div>
                <div className='min-w-0'>
                  <p className='truncate font-semibold text-slate-900 dark:text-slate-100'>
                    {channel.name}
                  </p>
                  <Badge variant='outline'>{channelTypeLabel}</Badge>
                </div>
              </div>

              {channel.description && (
                <section>
                  <h4 className='mb-1 text-sm font-medium text-slate-700 dark:text-slate-300'>
                    Description
                  </h4>
                  <p className='text-sm text-slate-600 dark:text-slate-400'>
                    {channel.description}
                  </p>
                </section>
              )}

              <dl className='space-y-3 text-sm'>
                <div className='flex justify-between gap-4'>
                  <dt className='text-slate-500'>Members</dt>
                  <dd className='font-medium'>{channel.memberCount}</dd>
                </div>
                <div className='flex justify-between gap-4'>
                  <dt className='text-slate-500'>Messages</dt>
                  <dd className='font-medium'>{channel.messageCount ?? '—'}</dd>
                </div>
                <div className='flex justify-between gap-4'>
                  <dt className='text-slate-500'>Created</dt>
                  <dd className='font-medium'>{formatDate(channel.createdAt)}</dd>
                </div>
                {channel.type === 'TOPIC' && (
                  <div className='flex justify-between gap-4'>
                    <dt className='text-slate-500'>Linked entity</dt>
                    <dd className='truncate font-medium'>
                      {channel.entityType}/{channel.entityId}
                    </dd>
                  </div>
                )}
              </dl>
            </TabsContent>

            <TabsContent value='members' className='mt-0 space-y-3'>
              <Button
                variant='outline'
                className='w-full justify-start gap-2'
                onClick={() => setMemberDialogOpen(true)}
              >
                <Users className='h-4 w-4' />
                Manage members
              </Button>
              <p className='text-sm text-slate-500 dark:text-slate-400'>
                Member management opens the existing member panel while this
                sidebar owns conversation details.
              </p>
            </TabsContent>

            <TabsContent value='files' className='mt-0 space-y-3'>
              {filesLoading && (
                <div className='flex items-center justify-center py-8'>
                  <Loader2 className='h-5 w-5 animate-spin text-slate-500' />
                </div>
              )}
              {!filesLoading && files.length === 0 && (
                <p className='rounded-lg border border-dashed border-slate-300 p-4 text-center text-sm text-slate-500 dark:border-slate-700'>
                  No files have been shared in this conversation yet.
                </p>
              )}
              {files.map((attachment) => (
                <FileRow key={attachment.id} attachment={attachment} />
              ))}
            </TabsContent>
          </ScrollArea>
        </Tabs>
      </div>

      <ChannelMembersPanel
        open={memberDialogOpen}
        onOpenChange={setMemberDialogOpen}
        channelId={channel.id}
        channelName={channel.name}
        currentUserId={currentUserId}
      />
    </aside>
  );
};
```

- [ ] **Step 2: Export component**

In `components/index.ts`, add:

```ts
export { ConversationDetailsSidebar } from './ConversationDetailsSidebar';
```

- [ ] **Step 3: Run frontend type check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: any errors should point to missing UI exports or prop type mismatch; fix before moving on.

- [ ] **Step 4: Commit sidebar component**

```bash
git add serp_web/src/modules/discuss/components/ConversationDetailsSidebar.tsx serp_web/src/modules/discuss/components/index.ts
git commit -m "feat(web): add discuss conversation details sidebar"
```

---

## Task 7: Wire sidebar into ChatWindow and remove unsupported actions

**Files:**

- Modify: `serp_web/src/modules/discuss/components/ChatWindow.tsx`

- [ ] **Step 1: Update imports**

In `ChatWindow.tsx`, remove `Phone`, `Video`, and `MoreVertical` from the `lucide-react` import.

Remove:

```ts
import { ChannelMembersPanel } from './ChannelMembersPanel';
```

Add:

```ts
import { ConversationDetailsSidebar } from './ConversationDetailsSidebar';
```

- [ ] **Step 2: Rename sidebar state**

Replace:

```ts
  const [membersPanelOpen, setMembersPanelOpen] = useState(false);
```

with:

```ts
  const [detailsSidebarOpen, setDetailsSidebarOpen] = useState(false);
```

- [ ] **Step 3: Remove call/video/no-op menu buttons**

Delete the full direct-channel phone/video block:

```tsx
            {channel.type === 'DIRECT' && (
              <>
                <Button
                  variant='ghost'
                  size='sm'
                  className='text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100'
                >
                  <Phone className='h-5 w-5' />
                </Button>
                <Button
                  variant='ghost'
                  size='sm'
                  className='text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100'
                >
                  <Video className='h-5 w-5' />
                </Button>
              </>
            )}
```

Delete the no-op `MoreVertical` button:

```tsx
            <Button
              variant='ghost'
              size='sm'
              className='text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100'
            >
              <MoreVertical className='h-5 w-5' />
            </Button>
```

- [ ] **Step 4: Change info button handler**

Replace:

```tsx
              onClick={() => setMembersPanelOpen(true)}
```

with:

```tsx
              onClick={() => setDetailsSidebarOpen((open) => !open)}
```

Change title:

```tsx
              title='View conversation details'
```

- [ ] **Step 5: Replace the return root and member panel mount**

Change the return root from:

```tsx
    <div
      className={cn(
        'flex flex-col h-full bg-slate-50 dark:bg-slate-900',
        className
      )}
    >
```

to:

```tsx
    <div className={cn('flex h-full min-w-0 bg-slate-50 dark:bg-slate-900', className)}>
      <div className='flex min-w-0 flex-1 flex-col'>
```

At the bottom of `ChatWindow`, remove:

```tsx
      <ChannelMembersPanel
        open={membersPanelOpen}
        onOpenChange={setMembersPanelOpen}
        channelId={channel.id}
        channelName={channel.name}
        currentUserId={currentUserId}
      />
```

Immediately after the `SearchDialog` block, close the main chat column and mount the sidebar:

```tsx
      </div>

      <ConversationDetailsSidebar
        channel={channel}
        currentUserId={currentUserId}
        open={detailsSidebarOpen}
        onOpenChange={setDetailsSidebarOpen}
      />
    </div>
```

`SearchDialog` stays inside the main chat column so search behavior remains unchanged.

- [ ] **Step 6: Run frontend lint/type checks**

Run from `serp_web/`:

```bash
npm run type-check
npm run lint
```

Expected: no new unused import errors for `Phone`, `Video`, `MoreVertical`, or `ChannelMembersPanel`.

- [ ] **Step 7: Commit ChatWindow wiring**

```bash
git add serp_web/src/modules/discuss/components/ChatWindow.tsx
git commit -m "feat(web): wire discuss details sidebar into chat"
```

---

## Task 8: Final verification

**Files:**

- Verify all files changed by previous tasks.

- [ ] **Step 1: Run backend targeted tests**

Run from `discuss_service/`:

```bash
mvn -Dtest=AttachmentUseCaseTest,AttachmentServiceTest test
```

Expected: PASS.

- [ ] **Step 2: Run backend compile gate**

Run from `discuss_service/`:

```bash
mvn -q -DskipTests compile
```

Expected: PASS.

- [ ] **Step 3: Run frontend gates**

Run from `serp_web/`:

```bash
npm run type-check
npm run lint
```

Expected: PASS or only pre-existing unrelated warnings; record any unrelated failures in the handoff.

- [ ] **Step 4: Manual smoke test**

Start app stack as needed, then verify:

1. Discuss chat header shows search and info, not phone/video.
2. Info toggles a right sidebar.
3. Sidebar overview shows channel metadata.
4. Members tab opens member management through the existing member panel.
5. Files tab calls `/channels/{channelId}/attachments`.
6. Opening a file calls `/attachments/{attachmentId}/download-url`.
7. Message sending and search still work.

- [ ] **Step 5: Commit any final fixes**

```bash
git status --short
git add docs/superpowers/plans/2026-06-19-discuss-conversation-details-sidebar.md discuss_service/src/main/java/serp/project/discuss_service/core/service/IAttachmentService.java discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/AttachmentService.java discuss_service/src/main/java/serp/project/discuss_service/core/port/store/IAttachmentPort.java discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/adapter/AttachmentAdapter.java discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/repository/IAttachmentRepository.java discuss_service/src/main/java/serp/project/discuss_service/core/usecase/AttachmentUseCase.java discuss_service/src/main/java/serp/project/discuss_service/ui/controller/ChannelController.java discuss_service/src/test/java/serp/project/discuss_service/core/service/impl/AttachmentServiceTest.java discuss_service/src/test/java/serp/project/discuss_service/core/usecase/AttachmentUseCaseTest.java serp_web/src/modules/discuss/types/message.ts serp_web/src/modules/discuss/types/channel.ts serp_web/src/modules/discuss/api/transformers.ts serp_web/src/modules/discuss/api/attachments.api.ts serp_web/src/modules/discuss/components/ConversationDetailsSidebar.tsx serp_web/src/modules/discuss/components/ChatWindow.tsx serp_web/src/modules/discuss/components/index.ts
git commit -m "fix: polish discuss conversation details sidebar"
```

Only create this commit if verification required changes in one or more of those files.

---

## Self-review

Spec coverage:

- Unsupported call/video controls are removed in Task 7.
- Right sidebar is created in Task 6 and wired in Task 7.
- Overview, Members, and Files sections are defined in Task 6.
- Existing member management behavior is preserved by mounting `ChannelMembersPanel` from the Members tab.
- Backend file history endpoint is implemented in Tasks 1-4.
- Existing download-url endpoint remains the file open path in Task 6.

Placeholder scan:

- No unresolved placeholder markers or unnamed files are intentionally left in this plan.
- Every new method and component introduced by a later task is defined by an earlier or same task.

Scope check:

- This plan avoids pinned messages, shared links, media galleries, and true member-panel extraction. A deeper member-tab refactor can be a follow-up after the sidebar lands.
