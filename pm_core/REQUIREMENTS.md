# Project Management (PM) Module - Requirements

## 1. Module Overview

PM (Project Management) la module quan ly du an cho team/organization trong he thong SERP ERP. Khac voi PTM (Personal Task Management) phuc vu ca nhan, PM tap trung vao quan ly cong viec nhom voi day du quy trinh Agile/Scrum/Kanban.

### Microservices

| Service | Port | Language | Responsibility |
|---------|------|----------|----------------|
| **pm_core** | 8093 | Go (Gin) | Project, WorkItem, Sprint, Board, Comment, Workflow, Time Tracking |
| **pm_analytics** | 8094 | Java (Spring Boot) | Metrics, Dashboards, Reports, Resource Utilization |

### Architecture

```
                        serp_web /modules/pm/
                              |
                        API Gateway (8080)
                         /pm/api/v1/*
                              |
                  +-----------+-----------+
                  |                       |
           pm_core (Go)          pm_analytics (Java)
           Port: 8093            Port: 8094
           DB: pm_core_db        DB: pm_analytics_db
                  |                       |
                  +----> Kafka <----------+
                  |
      +-----+----+-----+-----+
      |     |          |     |
  account  notif   mail   logging
  (HTTP)  (Kafka) (Kafka) (Kafka)
```

---

## 2. Bounded Context & Scope

### PM Module so huu (owns):
- **Project lifecycle** - tao, quan ly, dong du an
- **Work items** - Epic, Story, Task, Bug, Subtask voi hierarchy va dependencies
- **Team membership** - thanh vien du an voi roles (Owner, Admin, Member, Viewer)
- **Sprint management** - lap ke hoach, thuc hien, dong sprint (Scrum)
- **Board management** - Kanban/Scrum board voi columns, WIP limits
- **Milestone & Roadmap** - muc tieu, timeline du an
- **Comments & Activity** - thao luan tren work items, lich su thay doi
- **Time tracking** - ghi nhan gio lam viec
- **Workflow customization** - trang thai, chuyen doi tuy chinh
- **Custom fields** - truong du lieu tuy chinh tren work items
- **Labels** - phan loai work items
- **Attachments** - file dinh kem (metadata, file luu tren S3/MinIO)
- **Analytics & Reporting** - velocity, burndown, dashboards, export reports

### Out of scope (KHONG nam trong PM):
- **Chat/Discussion giua nguoi dung** -> `discuss_service`
- **Video conferencing** -> 3rd party integration
- **Wiki/Knowledge base** -> service rieng (tuong lai)
- **Email sending** -> `mailservice` (PM chi publish Kafka events)
- **Push notifications** -> `notification_service` (PM chi publish events)
- **AI predictions** -> `serp_llm` (PM cung cap data, LLM xu ly, tuong lai)
- **Calendar sync** -> `ptm_schedule` (tuong lai sync tu PM)
- **Full-text search** -> Elasticsearch infrastructure
- **White-label/Branding** -> infrastructure concern
- **User/Org management** -> `account` service
- **Audit logging** -> `logging_tracker` (PM publish events)

### PTM vs PM

| Aspect | ptm_task (Personal) | pm_core (Team) |
|--------|---------------------|----------------|
| Ownership | userId (ca nhan) | projectId + assignments (nhom) |
| Entity name | TaskEntity | WorkItemEntity |
| Hierarchy | Task -> SubTask | Epic -> Story -> Task -> Bug -> SubTask |
| Assignment | Creator = owner | N:M (nhieu nguoi 1 work item) |
| Methodology | Khong co | Scrum / Kanban / Waterfall / Hybrid |
| Sprint | Khong co | Full sprint lifecycle |
| Board | Khong co | Kanban + Scrum boards |
| Database | ptm_task_db | pm_core_db |

**Future sync:** ptm_task da co `ExternalID` va `Source` fields. Khi PM publish `WORK_ITEM_ASSIGNED`, ptm_task co the tao personal task voi `Source: "pm"` de user thay work items duoc assign trong personal task list.

---

## 3. Core Entities & Relationships

### pm_core Entities (~20)

#### Project & Team

**ProjectEntity:**
```
- id, tenantId, createdBy, updatedBy
- name: string (ten du an)
- key: string (ma du an, vd: "SERP", "PM" -> work item ID: PM-123)
- description: string (mo ta)
- status: enum (PLANNING, ACTIVE, ON_HOLD, COMPLETED, ARCHIVED)
- priority: enum (LOW, MEDIUM, HIGH, CRITICAL)
- methodologyType: enum (SCRUM, KANBAN, WATERFALL, HYBRID)
- startDateMs, targetEndDateMs, actualEndDateMs: int64 (timestamps)
- defaultWorkflowId: int64 (FK -> WorkflowDefinition)
- defaultBoardId: int64 (FK -> Board)
- color, icon: string (UI customization)
- totalWorkItems, completedWorkItems, progressPercentage: int (denormalized)
- totalMembers: int (denormalized)
- activeStatus: enum (ACTIVE, DELETED)
```

**ProjectMemberEntity:**
```
- id, tenantId, createdBy
- projectId: int64 (FK -> Project)
- userId: int64 (FK -> account service user)
- role: enum (OWNER, ADMIN, MEMBER, VIEWER)
- joinedAtMs: int64
- activeStatus: enum (ACTIVE, REMOVED)
```

#### Work Items

**WorkItemEntity:**
```
- id, tenantId, createdBy, updatedBy
- projectId: int64 (FK -> Project)
- itemNumber: int (sequential per project, cho PM-123)
- type: enum (EPIC, STORY, TASK, BUG, SUBTASK)
- title: string
- description: string (markdown)
- status: string (dynamic, dinh nghia boi WorkflowState)
- priority: enum (LOWEST, LOW, MEDIUM, HIGH, HIGHEST)
- parentWorkItemId: int64 (FK -> WorkItem, hierarchy)
- hasChildren: bool
- sprintId: int64 (FK -> Sprint)
- milestoneId: int64 (FK -> Milestone)
- reporterId: int64 (nguoi tao)
- startDateMs, dueDateMs, completedAtMs: int64
- storyPoints: int
- estimatedHours, actualHours, remainingHours: float64
- boardColumnId: int64 (FK -> BoardColumn)
- position: int (thu tu trong column/backlog, dung Lexorank hoac float)
- commentCount, attachmentCount: int (denormalized)
- childCount, completedChildCount: int (denormalized)
- activeStatus: enum (ACTIVE, DELETED)
- assignments: []WorkItemAssignment (loaded separately)
- children: []WorkItem (loaded separately)
```

**WorkItemAssignmentEntity:**
```
- id, tenantId
- workItemId: int64 (FK -> WorkItem)
- userId: int64 (FK -> account user)
- assignedAtMs: int64
- assignedBy: int64
```

**WorkItemDependencyEntity:**
```
- id, tenantId
- sourceWorkItemId: int64 (FK -> WorkItem)
- targetWorkItemId: int64 (FK -> WorkItem)
- dependencyType: enum (FINISH_TO_START, START_TO_START, FINISH_TO_FINISH, START_TO_FINISH)
```

#### Sprint & Milestone

**SprintEntity:**
```
- id, tenantId, createdBy
- projectId: int64 (FK -> Project)
- name: string (vd: "Sprint 14")
- goal: string
- status: enum (PLANNING, ACTIVE, COMPLETED, CANCELLED)
- startDateMs, endDateMs: int64
- totalPoints, completedPoints: int (denormalized)
- totalWorkItems, completedWorkItems: int (denormalized)
- sprintOrder: int (thu tu trong project)
```

**MilestoneEntity:**
```
- id, tenantId, createdBy
- projectId: int64 (FK -> Project)
- name: string
- description: string
- targetDateMs: int64
- status: enum (PENDING, IN_PROGRESS, COMPLETED, MISSED)
- completedAtMs: int64
- totalWorkItems, completedWorkItems: int (denormalized)
```

#### Board

**BoardEntity:**
```
- id, tenantId, createdBy
- projectId: int64 (FK -> Project)
- name: string
- type: enum (KANBAN, SCRUM)
- isDefault: bool
```

**BoardColumnEntity:**
```
- id, tenantId
- boardId: int64 (FK -> Board)
- name: string (vd: "To Do", "In Progress", "Done")
- columnOrder: int
- wipLimit: int (Work In Progress limit, 0 = unlimited)
- statusMapping: string (map voi WorkflowState)
- color: string
```

#### Collaboration

**CommentEntity:**
```
- id, tenantId
- workItemId: int64 (FK -> WorkItem)
- authorId: int64
- parentCommentId: int64 (FK -> Comment, threading)
- content: string (markdown)
- mentionedUserIds: []int64 (extracted tu content)
- isEdited: bool
- editedAtMs: int64
- activeStatus: enum (ACTIVE, DELETED)
```

**AttachmentEntity:**
```
- id, tenantId
- workItemId: int64 (FK -> WorkItem)
- commentId: int64 (FK -> Comment, optional)
- uploadedBy: int64
- fileName: string
- fileSize: int64 (bytes)
- mimeType: string
- storageUrl: string (S3/MinIO URL)
- activeStatus: enum (ACTIVE, DELETED)
```

**ActivityLogEntity:**
```
- id, tenantId
- projectId: int64
- entityType: string (PROJECT, WORK_ITEM, SPRINT, MILESTONE, COMMENT)
- entityId: int64
- action: string (CREATED, UPDATED, DELETED, STATUS_CHANGED, ASSIGNED, COMMENTED)
- actorId: int64
- changes: JSON string (old/new values)
- createdAtMs: int64
```

#### Time Tracking

**TimeEntryEntity:**
```
- id, tenantId
- workItemId: int64 (FK -> WorkItem)
- projectId: int64 (denormalized)
- userId: int64
- durationMin: int (thoi gian lam viec, phut)
- startedAtMs, endedAtMs: int64
- description: string
- status: enum (DRAFT, SUBMITTED, APPROVED, REJECTED)
- approvedBy: int64
```

#### Labels

**LabelEntity:**
```
- id, tenantId
- projectId: int64 (FK -> Project)
- name: string
- color: string (hex)
```

**WorkItemLabelEntity:**
```
- id
- workItemId: int64 (FK -> WorkItem)
- labelId: int64 (FK -> Label)
```

#### Workflow Customization

**WorkflowDefinitionEntity:**
```
- id, tenantId, createdBy
- projectId: int64 (FK -> Project)
- name: string (vd: "Default Scrum Workflow")
- isDefault: bool
```

**WorkflowStateEntity:**
```
- id, tenantId
- workflowId: int64 (FK -> WorkflowDefinition)
- name: string (vd: "To Do", "In Progress", "In Review", "Done")
- category: enum (TODO, IN_PROGRESS, DONE) (de phan loai cho metrics)
- stateOrder: int
- color: string
```

**WorkflowTransitionEntity:**
```
- id, tenantId
- workflowId: int64 (FK -> WorkflowDefinition)
- fromStateId: int64 (FK -> WorkflowState)
- toStateId: int64 (FK -> WorkflowState)
- name: string (vd: "Start Work", "Submit for Review")
```

#### Custom Fields

**CustomFieldDefinitionEntity:**
```
- id, tenantId
- projectId: int64 (FK -> Project)
- name: string
- fieldType: enum (TEXT, NUMBER, DATE, SELECT, MULTI_SELECT, CHECKBOX, URL, USER)
- options: JSON string (cho SELECT/MULTI_SELECT)
- isRequired: bool
- fieldOrder: int
```

**CustomFieldValueEntity:**
```
- id, tenantId
- workItemId: int64 (FK -> WorkItem)
- fieldDefinitionId: int64 (FK -> CustomFieldDefinition)
- value: string (gia tri duoc serialize)
```

### pm_analytics Entities (~6)

**ProjectMetricsSnapshotEntity:**
```
- id, tenantId
- projectId: int64
- snapshotDateMs: int64
- totalWorkItems, completedWorkItems, inProgressWorkItems: int
- totalStoryPoints, completedStoryPoints: int
- averageCycleTimeHours, averageLeadTimeHours: float64
- onTimeDeliveryRate: float64
```

**SprintMetricsEntity:**
```
- id, tenantId
- sprintId: int64
- projectId: int64
- velocity: int (completed story points)
- plannedPoints, completedPoints, carryOverPoints: int
- burndownData: JSON (daily remaining points)
- completionRate: float64
- averageCycleTimeHours: float64
```

**ResourceUtilizationEntity:**
```
- id, tenantId
- userId: int64
- periodStartMs, periodEndMs: int64
- totalAssignedItems: int
- completedItems: int
- totalLoggedHours: float64
- totalEstimatedHours: float64
- utilizationRate: float64
```

**DashboardEntity:**
```
- id, tenantId, createdBy
- name: string
- description: string
- isDefault: bool
- projectId: int64 (null = cross-project)
```

**DashboardWidgetEntity:**
```
- id, tenantId
- dashboardId: int64 (FK -> Dashboard)
- widgetType: enum (VELOCITY_CHART, BURNDOWN, CUMULATIVE_FLOW, STATUS_DISTRIBUTION, RESOURCE_HEATMAP, CUSTOM)
- title: string
- config: JSON (data source, filters, display options)
- position: int
- width, height: int (grid layout)
```

**ReportTemplateEntity:**
```
- id, tenantId, createdBy
- name: string
- description: string
- templateType: enum (SPRINT_REVIEW, PROJECT_STATUS, RESOURCE_REPORT, CUSTOM)
- config: JSON (sections, data sources, filters)
- outputFormat: enum (PDF, EXCEL, CSV)
```

### Entity Relationships (ERD)

```
Project (1) ──── (N) ProjectMember
Project (1) ──── (N) WorkItem
Project (1) ──── (N) Sprint
Project (1) ──── (N) Milestone
Project (1) ──── (N) Board
Project (1) ──── (N) Label
Project (1) ──── (N) WorkflowDefinition
Project (1) ──── (N) CustomFieldDefinition

WorkItem (1) ──── (N) WorkItem (parent-child hierarchy)
WorkItem (1) ──── (N) WorkItemAssignment
WorkItem (1) ──── (N) WorkItemDependency
WorkItem (1) ──── (N) Comment
WorkItem (1) ──── (N) Attachment
WorkItem (1) ──── (N) TimeEntry
WorkItem (1) ──── (N) WorkItemLabel
WorkItem (1) ──── (N) CustomFieldValue
WorkItem (N) ──── (1) Sprint
WorkItem (N) ──── (1) Milestone
WorkItem (N) ──── (1) BoardColumn

Board (1) ──── (N) BoardColumn
Comment (1) ──── (N) Comment (threading)
Comment (1) ──── (N) Attachment

WorkflowDefinition (1) ──── (N) WorkflowState
WorkflowDefinition (1) ──── (N) WorkflowTransition
```

---

## 4. Phase 1 - MVP Functional Requirements

### 4.1. Project Management

**UC-P01: Tao du an**
- Actor: Authenticated user
- Input: name, key, description, methodologyType, priority, startDate, targetEndDate
- Logic: Validate key unique per tenant, tao default board (Kanban), tao default workflow (To Do -> In Progress -> Done), add creator as OWNER
- Output: ProjectResponse

**UC-P02: Cap nhat du an**
- Actor: Project OWNER hoac ADMIN
- Input: name, description, status, priority, dates
- Constraint: Chi OWNER/ADMIN moi cap nhat duoc

**UC-P03: Xoa/Archive du an**
- Actor: Project OWNER
- Logic: Soft delete (activeStatus = DELETED) hoac archive (status = ARCHIVED)

**UC-P04: Xem danh sach du an**
- Actor: Authenticated user
- Filter: status, priority, methodologyType, search by name
- Pagination: page, pageSize, sortBy, sortOrder
- Chi hien thi projects ma user la member

**UC-P05: Quan ly thanh vien du an**
- Actor: Project OWNER hoac ADMIN
- Actions: Add member, remove member, change role
- Roles: OWNER (1 per project), ADMIN, MEMBER, VIEWER
- Constraint: VIEWER chi xem, MEMBER xem + tao/cap nhat work items, ADMIN quan ly members

### 4.2. Work Item Management

**UC-W01: Tao work item**
- Actor: Project MEMBER+
- Input: type, title, description, priority, parentWorkItemId, sprintId, milestoneId, assigneeIds, dueDateMs, storyPoints, estimatedHours, labelIds
- Logic: Auto-generate itemNumber (sequential per project), validate parent type (EPIC chi chua STORY, STORY chi chua TASK/BUG, TASK/BUG chi chua SUBTASK), set initial status tu workflow
- Output: WorkItemResponse voi itemKey (vd: "PM-123")

**UC-W02: Cap nhat work item**
- Actor: Project MEMBER+
- Input: title, description, priority, dates, storyPoints, estimatedHours
- Logic: Ghi activity log, publish Kafka event

**UC-W03: Chuyen trang thai work item**
- Actor: Project MEMBER+
- Input: workItemId, newStatus
- Logic: Validate transition theo WorkflowTransition, cap nhat status, cap nhat completedAtMs neu status category = DONE, cap nhat parent progress
- Side effects: Activity log, Kafka event, notification neu assigned

**UC-W04: Gan/Bo gan nguoi thuc hien**
- Actor: Project MEMBER+
- Input: workItemId, userId (phai la project member)
- Logic: Tao/xoa WorkItemAssignment, publish notification

**UC-W05: Di chuyen work item giua sprints**
- Actor: Project MEMBER+
- Input: workItemId, targetSprintId (null = back to backlog)
- Logic: Cap nhat sprintId, cap nhat sprint stats (denormalized counts)

**UC-W06: Quan ly dependencies**
- Actor: Project MEMBER+
- Input: sourceWorkItemId, targetWorkItemId, dependencyType
- Logic: Validate khong tao circular dependency (cycle detection), validate ca 2 work items cung project

**UC-W07: Xem work item tree**
- Actor: Project MEMBER+
- Input: workItemId
- Output: Work item voi full children hierarchy

**UC-W08: Xem backlog**
- Actor: Project MEMBER+
- Input: projectId, filters (type, status, priority, assigneeId, labelId, sprintId=null)
- Output: Paginated work items chua thuoc sprint nao, sorted by position

**UC-W09: Xoa work item**
- Actor: Project MEMBER+ (chi xoa work item minh tao hoac ADMIN+)
- Logic: Soft delete, cascade soft delete children

### 4.3. Board Management

**UC-B01: Xem Kanban board**
- Actor: Project MEMBER+
- Input: boardId, filters (assigneeId, priority, type, labelId)
- Output: Board voi columns, moi column co danh sach work items sorted by position

**UC-B02: Di chuyen work item tren board (drag-and-drop)**
- Actor: Project MEMBER+
- Input: workItemId, targetColumnId, targetPosition
- Logic: Cap nhat boardColumnId va position, neu column map voi workflow state thi cap nhat work item status tuong ung

**UC-B03: Quan ly board columns**
- Actor: Project ADMIN+
- Actions: Add column, rename, reorder, set WIP limit, delete column
- Constraint: Khong xoa column con work items (phai move truoc)

### 4.4. Sprint Management

**UC-S01: Tao sprint**
- Actor: Project ADMIN+
- Input: name, goal, startDateMs, endDateMs
- Logic: Auto-set sprintOrder, status = PLANNING

**UC-S02: Bat dau sprint**
- Actor: Project ADMIN+
- Constraint: Chi 1 sprint ACTIVE tai 1 thoi diem per project
- Logic: Set status = ACTIVE, validate co work items trong sprint

**UC-S03: Ket thuc sprint**
- Actor: Project ADMIN+
- Input: incompleteItemAction (MOVE_TO_BACKLOG | MOVE_TO_NEXT_SPRINT)
- Logic: Set status = COMPLETED, xu ly incomplete items theo choice, tinh sprint metrics (velocity, completion rate)

**UC-S04: Xem sprint board**
- Actor: Project MEMBER+
- Output: Sprint info + work items grouped by status/column

### 4.5. Milestone Tracking

**UC-M01: Tao milestone**
- Actor: Project ADMIN+
- Input: name, description, targetDateMs

**UC-M02: Xem milestone progress**
- Actor: Project MEMBER+
- Output: Milestone info voi totalWorkItems, completedWorkItems, progressPercentage

**UC-M03: Gan work item vao milestone**
- Actor: Project MEMBER+
- Input: workItemId, milestoneId

### 4.6. Label Management

**UC-L01: CRUD Labels**
- Actor: Project ADMIN+ (tao/xoa), MEMBER+ (gan vao work item)
- Input: name, color (hex)

**UC-L02: Gan/bo label cho work item**
- Actor: Project MEMBER+
- Input: workItemId, labelId

### 4.7. Comments & Activity

**UC-C01: Them comment**
- Actor: Project MEMBER+
- Input: workItemId, content (markdown), parentCommentId (optional, threading)
- Logic: Extract @mentions tu content, publish notification cho mentioned users, tang commentCount

**UC-C02: Sua/Xoa comment**
- Actor: Author cua comment
- Logic: Set isEdited=true, editedAtMs hoac soft delete

**UC-C03: Xem activity log**
- Actor: Project MEMBER+
- Input: projectId hoac workItemId
- Output: Paginated activity entries, sorted by createdAt desc

---

## 5. Phase 2 - Enhancement Requirements

### 5.1. Workflow Customization
- Tao custom workflow voi custom states va transitions per project
- Dinh nghia rules cho transitions (vd: chi ADMIN moi chuyen sang "Done")
- Workflow templates (vd: "Scrum Default", "Bug Tracking")

### 5.2. Custom Fields
- Dinh nghia custom fields per project (text, number, date, select, multi-select, checkbox, URL, user)
- Hien thi custom fields tren work item detail va board cards
- Filter/sort theo custom fields

### 5.3. Time Tracking
- Log time entries cho work items
- Timer (start/stop) hoac manual entry
- Time entry approval workflow (DRAFT -> SUBMITTED -> APPROVED/REJECTED)
- So sanh actual hours vs estimated hours

### 5.4. Attachments
- Upload files vao work items va comments
- Luu tru tren S3/MinIO (cung infrastructure voi discuss_service)
- Metadata: fileName, fileSize, mimeType, storageUrl

### 5.5. Risk & Issue Register
- RiskEntity: title, description, probability (1-5), impact (1-5), riskScore, status, mitigationPlan, ownerId
- IssueEntity: title, description, severity (LOW/MEDIUM/HIGH/CRITICAL), status (OPEN/IN_PROGRESS/RESOLVED/CLOSED), assigneeId, resolution
- Escalation rules: auto-notify khi issue open qua N ngay

---

## 6. Phase 3 - Advanced Requirements

### 6.1. Analytics Service (pm_analytics)
- Consume Kafka events tu pm_core
- Tinh toan va luu metrics snapshots (daily)
- Sprint velocity tracking qua cac sprints
- Burndown/burnup chart data
- Cumulative flow diagram data
- Cycle time va lead time analysis

### 6.2. Dashboards & Reports
- Dashboard tuy chinh voi widgets (velocity chart, burndown, status distribution, resource heatmap)
- Report templates (sprint review, project status, resource report)
- Export PDF, Excel, CSV

### 6.3. Resource Utilization
- Workload per user across projects
- Utilization rate = actual hours / available hours
- Capacity planning

### 6.4. PTM Sync
- PM publish `WORK_ITEM_ASSIGNED` -> ptm_task consume va tao personal task
- Status sync: PM work item status thay doi -> ptm_task cap nhat personal task

### 6.5. Automation Rules (future)
- Trigger actions dua tren events (vd: auto-assign reviewer khi status = "In Review")
- Scheduled actions (vd: noti khi work item sap den due date)

---

## 7. API Specifications

### pm_core API Endpoints

#### Projects
```
POST   /pm/api/v1/projects                          - Tao project
GET    /pm/api/v1/projects                          - Danh sach projects (filtered, paginated)
GET    /pm/api/v1/projects/:id                      - Chi tiet project
PATCH  /pm/api/v1/projects/:id                      - Cap nhat project
DELETE /pm/api/v1/projects/:id                      - Xoa project (soft delete)

POST   /pm/api/v1/projects/:id/members              - Them member
GET    /pm/api/v1/projects/:id/members              - Danh sach members
PATCH  /pm/api/v1/projects/:id/members/:memberId    - Cap nhat role
DELETE /pm/api/v1/projects/:id/members/:memberId    - Xoa member
```

#### Work Items
```
POST   /pm/api/v1/projects/:projectId/work-items    - Tao work item
GET    /pm/api/v1/projects/:projectId/work-items    - Danh sach work items (filtered)
GET    /pm/api/v1/work-items/:id                    - Chi tiet work item
PATCH  /pm/api/v1/work-items/:id                    - Cap nhat work item
DELETE /pm/api/v1/work-items/:id                    - Xoa work item
GET    /pm/api/v1/work-items/:id/tree               - Work item tree (hierarchy)
POST   /pm/api/v1/work-items/:id/status             - Chuyen trang thai
POST   /pm/api/v1/work-items/:id/assignments        - Gan nguoi
DELETE /pm/api/v1/work-items/:id/assignments/:userId - Bo gan nguoi
POST   /pm/api/v1/work-items/:id/dependencies       - Them dependency
DELETE /pm/api/v1/work-items/:id/dependencies/:depId - Xoa dependency
GET    /pm/api/v1/work-items/:id/comments           - Danh sach comments
POST   /pm/api/v1/work-items/:id/comments           - Them comment
GET    /pm/api/v1/work-items/:id/activity            - Activity log
GET    /pm/api/v1/work-items/:id/attachments         - Danh sach attachments
POST   /pm/api/v1/work-items/:id/attachments         - Upload attachment
```

#### Backlog
```
GET    /pm/api/v1/projects/:projectId/backlog       - Backlog (work items khong trong sprint)
PATCH  /pm/api/v1/projects/:projectId/backlog/reorder - Sap xep lai backlog
```

#### Sprints
```
POST   /pm/api/v1/projects/:projectId/sprints       - Tao sprint
GET    /pm/api/v1/projects/:projectId/sprints       - Danh sach sprints
GET    /pm/api/v1/sprints/:id                       - Chi tiet sprint
PATCH  /pm/api/v1/sprints/:id                       - Cap nhat sprint
POST   /pm/api/v1/sprints/:id/start                 - Bat dau sprint
POST   /pm/api/v1/sprints/:id/complete              - Ket thuc sprint
GET    /pm/api/v1/sprints/:id/work-items            - Work items trong sprint
```

#### Milestones
```
POST   /pm/api/v1/projects/:projectId/milestones    - Tao milestone
GET    /pm/api/v1/projects/:projectId/milestones    - Danh sach milestones
GET    /pm/api/v1/milestones/:id                    - Chi tiet milestone
PATCH  /pm/api/v1/milestones/:id                    - Cap nhat milestone
DELETE /pm/api/v1/milestones/:id                    - Xoa milestone
```

#### Boards
```
GET    /pm/api/v1/projects/:projectId/boards        - Danh sach boards
GET    /pm/api/v1/boards/:id                        - Chi tiet board
PATCH  /pm/api/v1/boards/:id                        - Cap nhat board
POST   /pm/api/v1/boards/:id/columns                - Them column
PATCH  /pm/api/v1/boards/:id/columns/:colId         - Cap nhat column
DELETE /pm/api/v1/boards/:id/columns/:colId         - Xoa column
GET    /pm/api/v1/boards/:id/work-items             - Work items tren board (Kanban view)
PATCH  /pm/api/v1/boards/:id/work-items/:itemId/move - Di chuyen work item (drag-drop)
```

#### Labels
```
POST   /pm/api/v1/projects/:projectId/labels        - Tao label
GET    /pm/api/v1/projects/:projectId/labels        - Danh sach labels
PATCH  /pm/api/v1/labels/:id                        - Cap nhat label
DELETE /pm/api/v1/labels/:id                        - Xoa label
```

#### Comments
```
PATCH  /pm/api/v1/comments/:id                      - Sua comment
DELETE /pm/api/v1/comments/:id                      - Xoa comment
```

#### Time Entries (Phase 2)
```
POST   /pm/api/v1/work-items/:id/time-entries       - Log time
GET    /pm/api/v1/work-items/:id/time-entries       - Xem time entries cua work item
GET    /pm/api/v1/time-entries                       - Time entries cua user (cross-project)
PATCH  /pm/api/v1/time-entries/:id                  - Cap nhat time entry
DELETE /pm/api/v1/time-entries/:id                  - Xoa time entry
```

### pm_analytics API Endpoints (Phase 3)
```
GET    /pm/api/v1/analytics/projects/:id/velocity       - Velocity chart
GET    /pm/api/v1/analytics/projects/:id/burndown       - Burndown chart
GET    /pm/api/v1/analytics/projects/:id/throughput      - Throughput metrics
GET    /pm/api/v1/analytics/projects/:id/cycle-time      - Cycle time
GET    /pm/api/v1/analytics/projects/:id/cumulative-flow - Cumulative flow diagram

GET    /pm/api/v1/analytics/sprints/:id/metrics          - Sprint metrics
GET    /pm/api/v1/analytics/sprints/:id/burndown         - Sprint burndown

GET    /pm/api/v1/analytics/resources/utilization        - Resource utilization
GET    /pm/api/v1/analytics/resources/:userId/workload   - User workload

POST   /pm/api/v1/dashboards                            - Tao dashboard
GET    /pm/api/v1/dashboards                            - Danh sach dashboards
GET    /pm/api/v1/dashboards/:id                        - Chi tiet dashboard
PATCH  /pm/api/v1/dashboards/:id                        - Cap nhat dashboard
DELETE /pm/api/v1/dashboards/:id                        - Xoa dashboard
POST   /pm/api/v1/dashboards/:id/widgets                - Them widget
PATCH  /pm/api/v1/dashboards/:id/widgets/:widgetId      - Cap nhat widget
DELETE /pm/api/v1/dashboards/:id/widgets/:widgetId      - Xoa widget

POST   /pm/api/v1/reports/generate                      - Generate report
GET    /pm/api/v1/reports/templates                      - Report templates
```

---

## 8. Kafka Events

### Topics

| Topic | Publisher | Consumers |
|-------|-----------|-----------|
| `pm.project` | pm_core | pm_analytics, logging_tracker |
| `pm.workitem` | pm_core | pm_analytics, notification_service, ptm_task (future) |
| `pm.sprint` | pm_core | pm_analytics |
| `pm.milestone` | pm_core | pm_analytics |
| `pm.comment` | pm_core | notification_service |
| `pm.member` | pm_core | notification_service |
| `pm.timeentry` | pm_core | pm_analytics |

### Event Types

**Project events:**
- `PROJECT_CREATED` - du an duoc tao
- `PROJECT_UPDATED` - du an duoc cap nhat
- `PROJECT_ARCHIVED` - du an duoc archive
- `PROJECT_DELETED` - du an bi xoa

**Work item events:**
- `WORK_ITEM_CREATED` - work item duoc tao
- `WORK_ITEM_UPDATED` - work item duoc cap nhat
- `WORK_ITEM_DELETED` - work item bi xoa
- `WORK_ITEM_STATUS_CHANGED` - trang thai thay doi
- `WORK_ITEM_ASSIGNED` - nguoi duoc gan
- `WORK_ITEM_UNASSIGNED` - nguoi bi bo gan
- `WORK_ITEM_MOVED_SPRINT` - di chuyen giua sprints
- `WORK_ITEM_MOVED_BOARD` - di chuyen tren board

**Sprint events:**
- `SPRINT_CREATED` - sprint duoc tao
- `SPRINT_STARTED` - sprint bat dau
- `SPRINT_COMPLETED` - sprint ket thuc

**Milestone events:**
- `MILESTONE_CREATED` - milestone duoc tao
- `MILESTONE_COMPLETED` - milestone hoan thanh

**Comment events:**
- `COMMENT_CREATED` - comment moi (bao gom mentionedUserIds)

**Member events:**
- `MEMBER_ADDED` - thanh vien duoc them
- `MEMBER_REMOVED` - thanh vien bi xoa
- `MEMBER_ROLE_CHANGED` - role thay doi

**Time entry events:**
- `TIME_ENTRY_LOGGED` - ghi nhan thoi gian

### Message Format

```json
{
  "meta": {
    "id": "uuid",
    "type": "WORK_ITEM_CREATED",
    "source": "pm_core",
    "v": "1.0",
    "ts": 1704067200000,
    "traceId": "uuid",
    "tenantId": 1,
    "entityType": "WORK_ITEM",
    "entityId": "123",
    "userId": 456
  },
  "data": {
    "projectId": 10,
    "itemNumber": 123,
    "itemKey": "PM-123",
    "type": "TASK",
    "title": "Implement login feature",
    "status": "TODO",
    "priority": "HIGH",
    "assigneeIds": [456, 789],
    "sprintId": 5,
    "storyPoints": 8,
    "dueDateMs": 1704153600000
  }
}
```

---

## 9. Integration Points

| Target Service | Mechanism | Purpose |
|---|---|---|
| **account** (8081) | HTTP Client | Lay user profiles, validate org membership, check permissions |
| **notification_service** (8090) | Kafka | Push notifications (assignment, mention, due date) |
| **mailservice** (8091) | Kafka | Email notifications (assignment, sprint start/end) |
| **logging_tracker** (8082) | Kafka | Audit trail cho compliance |
| **ptm_task** (8083) | Kafka (future) | Sync assigned work items sang personal task list |
| **discuss_service** (8092) | Kafka (optional) | Auto-create discussion channel cho project |

---

## 10. Non-functional Requirements

### Performance
- API response time < 200ms cho CRUD operations
- Board view (Kanban) load < 500ms cho 500 work items
- Backlog load < 300ms cho 1000 work items (paginated)

### Scalability
- Support 100+ concurrent users per tenant
- Support 1000+ projects per tenant
- Support 50,000+ work items per project

### Security
- JWT authentication qua API Gateway + Keycloak
- Project-level access control (chi members moi access)
- Role-based permissions (OWNER > ADMIN > MEMBER > VIEWER)
- Tenant isolation (tenantId filter tren moi query)
- Soft delete (khong xoa data vinh vien)

### Reliability
- Kafka events dam bao at-least-once delivery
- Idempotent event processing (deduplication)
- Database transactions cho multi-entity operations
- Graceful error handling (khong expose internal errors)

---

## 11. Tech Stack

### pm_core
- **Language:** Go 1.22+
- **Framework:** Gin (HTTP), GORM (ORM)
- **DI:** Uber FX
- **Database:** PostgreSQL (pm_core_db)
- **Cache:** Redis
- **Message Queue:** Kafka
- **File Storage:** S3/MinIO
- **Auth:** JWT via Keycloak JWKS
- **Architecture:** Clean Architecture (Controller -> UseCase -> Service -> Port -> Adapter)

### pm_analytics
- **Language:** Java 21
- **Framework:** Spring Boot 3.x
- **Database:** PostgreSQL (pm_analytics_db)
- **Migrations:** Flyway
- **Message Queue:** Kafka (consumer)
- **Auth:** JWT via Keycloak JWKS
- **Architecture:** Clean Architecture (same pattern as account, crm services)
