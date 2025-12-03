# PTM Implementation Plan - Roadmap to Completion

**Version:** 1.0  
**Date:** December 3, 2025  
**Author:** QuanTuanHuy  
**Status:** In Progress  

---

## 📊 Current Implementation Status

### **Services Overview**

| Service | Status | Completeness |
|---------|--------|--------------|
| `ptm_task` (Go:8090) | ✅ Core Complete | 85% |
| `ptm_schedule` (Go:8091) | ✅ Core Complete | 80% |
| `ptm_optimization` (Java:8092) | ⚠️ Basic | 40% |

---

## ✅ What's Already Implemented

### **ptm_task Service**

| Feature | Status | Notes |
|---------|--------|-------|
| **Project CRUD** | ✅ Done | Create, Read, Update, Delete projects |
| **Project Stats** | ✅ Done | Progress %, total/completed tasks, estimated hours |
| **Task CRUD** | ✅ Done | Full lifecycle management |
| **Task Status Management** | ✅ Done | TODO → IN_PROGRESS → DONE/CANCELLED |
| **Task Priority** | ✅ Done | LOW, MEDIUM, HIGH with priority score |
| **Task Dependencies** | ✅ Done | `task_dependency_graph` table, circular validation |
| **Recurring Tasks** | ✅ Done | RecurrencePattern enum (DAILY, WEEKLY, etc.) |
| **Task Templates** | ✅ Done | Create from template, variable substitution |
| **Task Notes** | ✅ Done | CRUD, attach to task/project |
| **Deep Work Flag** | ✅ Done | `is_deep_work` field |
| **Kafka Events** | ✅ Done | TASK_CREATED, TASK_UPDATED, TASK_DELETED |

**Entities:**
- ✅ `TaskEntity` - Full fields including tags, category, recurrence
- ✅ `ProjectEntity` - With stats calculation
- ✅ `NoteEntity` - Markdown content support
- ✅ `TaskTemplateEntity` - Variable substitution
- ✅ `TaskDependencyGraphEntity` - DAG validation

---

### **ptm_schedule Service**

| Feature | Status | Notes |
|---------|--------|-------|
| **Schedule Plan CRUD** | ✅ Done | Active/Proposed plans |
| **Schedule Task Snapshots** | ✅ Done | Sync from ptm_task via Kafka |
| **Schedule Events** | ✅ Done | Date, time slots, multi-part support |
| **Schedule Windows** | ✅ Done | Generated from availability |
| **Availability Calendar** | ✅ Done | Weekly patterns, defaults |
| **Calendar Exceptions** | ✅ Done | Block specific dates/times |
| **Reschedule Queue** | ✅ Done | Async processing, triggers |
| **Reschedule Strategy Service** | ✅ Done | Ripple, Insertion, FullReplan |
| **Hybrid Scheduler Algorithm** | ✅ Done | Greedy + Ripple Effect |
| **Event Pinning** | ✅ Done | Manual override support |
| **Event Split** | ✅ Done | Multi-day task support |
| **Event Complete** | ✅ Done | Actual duration tracking |
| **Kafka Consumer** | ✅ Done | Task event handling |

**Algorithm Features:**
- ✅ Greedy Insertion with scoring
- ✅ Ripple Effect for critical tasks
- ✅ Task splitting (max parts, min duration)
- ✅ Deadline & earliest start constraints
- ✅ Priority-based scheduling
- ✅ Pinned event preservation
- ✅ Buffer time support

---

### **ptm_optimization Service**

| Feature | Status | Notes |
|---------|--------|-------|
| **Basic Setup** | ✅ Done | Spring Boot, PostgreSQL |
| **Optimization Job Table** | ⚠️ Partial | Schema exists |
| **MILP Integration** | ❌ Not Done | OR-Tools not integrated |
| **A/B Testing** | ❌ Not Done | Schema designed only |

---

## ❌ What's Missing (Gap Analysis)

### **Priority 0 - Critical (Must Have for MVP)**

| Feature | Service | Effort | Status |
|---------|---------|--------|--------|
| Full-text search for notes | ptm_task | 2 days | ❌ Not Done |
| Note attachments (S3/MinIO) | ptm_task | 3 days | ❌ Not Done |
| Deep work protection constraint | ptm_schedule | 2 days | ❌ Not Done |
| Focus time blocks table | ptm_schedule | 1 day | ❌ Not Done |
| Algorithm comparison UI notification | ptm_schedule | 2 days | ❌ Not Done |

### **Priority 1 - High (Competitive Essentials)**

| Feature | Service | Effort | Status |
|---------|---------|--------|--------|
| ML-based duration estimation | ptm_task | 3 days | ❌ Not Done |
| Activity feed / Change log | ptm_schedule | 3 days | ❌ Not Done |
| Schedule change notifications | All | 2 days | ❌ Not Done |
| Deadline risk alerts | ptm_schedule | 2 days | ❌ Not Done |
| Smart buffer time (auto-insert) | ptm_schedule | 2 days | ❌ Not Done |
| Recurring task instance generation | ptm_task | 3 days | ⚠️ Partial |
| Quick Place mode (<200ms) | ptm_schedule | 1 day | ✅ Done |

### **Priority 2 - Medium (Nice to Have)**

| Feature | Service | Effort | Status |
|---------|---------|--------|--------|
| Historical duration learning | ptm_schedule | 3 days | ❌ Not Done |
| User productivity rhythms | ptm_schedule | 3 days | ❌ Not Done |
| Cross-day task moves | ptm_schedule | 2 days | ❌ Not Done |
| Conflict detection & resolution | ptm_schedule | 2 days | ❌ Not Done |
| MILP optimization (OR-Tools) | ptm_optimization | 1 week | ❌ Not Done |
| Warm-start caching | ptm_optimization | 2 days | ❌ Not Done |

### **Priority 3 - Low (Future Enhancements)**

| Feature | Service | Effort | Status |
|---------|---------|--------|--------|
| Google Calendar sync | New service | 2 weeks | ❌ Not Done |
| Meeting assistant / Booking | New service | 2 weeks | ❌ Not Done |
| LLM integration (NLP tasks) | serp_llm | 3 weeks | ❌ Not Done |
| Team features (multi-user) | All | 4+ weeks | ❌ Not Done |

---

## 🗓️ Phased Implementation Roadmap

### **Phase 1: Complete Core Features (2 weeks)**

**Goal:** Finish MVP-critical features

#### Week 1: ptm_task Enhancements

| Task | Priority | Effort | Assignee |
|------|----------|--------|----------|
| Add PostgreSQL full-text search on notes | P0 | 2 days | - |
| Implement note attachments (S3/MinIO) | P0 | 3 days | - |
| Add recurring task instance generator (cron) | P1 | 2 days | - |

**Deliverables:**
- [ ] `content_plain` field + tsvector index on notes
- [ ] S3 client adapter in infrastructure
- [ ] Note search API endpoint
- [ ] Background job for recurring task generation

#### Week 2: ptm_schedule Enhancements

| Task | Priority | Effort | Assignee |
|------|----------|--------|----------|
| Create `focus_time_blocks` table | P0 | 1 day | - |
| Add deep work protection in scheduler | P0 | 2 days | - |
| Create `schedule_change_log` table | P1 | 1 day | - |
| Implement activity feed logging | P1 | 2 days | - |

**Deliverables:**
- [ ] Focus time block entity, service, API
- [ ] Scheduler respects focus blocks (deep work only)
- [ ] Schedule change log on every event modification
- [ ] API endpoint to get activity feed

---

### **Phase 2: Smart Features (2 weeks)**

**Goal:** Motion-like intelligence

#### Week 3: Learning & Predictions

| Task | Priority | Effort | Assignee |
|------|----------|--------|----------|
| Add `task_completion_history` table | P1 | 1 day | - |
| Implement duration learning (moving avg) | P1 | 2 days | - |
| Add deadline risk detection | P1 | 2 days | - |

**Implementation Details:**

```go
// Duration Learning - Simple moving average
func (s *TaskService) UpdateDurationLearning(ctx context.Context, task *entity.TaskEntity, actualMin int) {
    if task.Category == nil {
        return
    }
    
    history := s.historyPort.GetByUserAndCategory(ctx, task.UserID, *task.Category)
    
    // Exponential moving average
    alpha := 0.2
    newEstimate := alpha * float64(actualMin) + (1-alpha) * history.AvgDuration
    
    s.historyPort.UpdateAvgDuration(ctx, task.UserID, *task.Category, newEstimate)
}
```

#### Week 4: Smart Scheduling Improvements

| Task | Priority | Effort | Assignee |
|------|----------|--------|----------|
| Auto buffer time between context switches | P1 | 2 days | - |
| Cross-day task move optimization | P2 | 2 days | - |
| Conflict detection alerts | P2 | 1 day | - |

**Buffer Time Logic:**
```go
// Add buffer when category changes
func (s *HybridScheduler) shouldAddBuffer(prevTask, nextTask *TaskInput) int {
    if prevTask == nil || nextTask == nil {
        return 0
    }
    if prevTask.Category != nextTask.Category {
        return 15 // 15 min context switch buffer
    }
    if prevTask.IsDeepWork && !nextTask.IsDeepWork {
        return 10 // Transition from deep work
    }
    return 0
}
```

---

### **Phase 3: Optimization Service (2 weeks)**

**Goal:** Deep optimization with MILP

#### Week 5-6: ptm_optimization Integration

| Task | Priority | Effort | Assignee |
|------|----------|--------|----------|
| Set up OR-Tools MILP solver | P2 | 3 days | - |
| Implement optimization job flow | P2 | 2 days | - |
| Kafka integration (REQUEST/COMPLETED) | P2 | 2 days | - |
| A/B testing framework | P2 | 2 days | - |
| Warm-start from heuristic | P2 | 1 day | - |

**MILP Model Structure:**
```java
// Variables
IntVar[][] x = new IntVar[tasks][windows]; // Task t in window w

// Constraints
// 1. Each task scheduled exactly once (or marked unscheduled)
// 2. No overlap in same time slot
// 3. Deadlines respected
// 4. Dependencies: if A→B, then A.end <= B.start

// Objective
// Maximize: sum(utility[t][w] * x[t][w])
```

---

### **Phase 4: External Integrations (3 weeks)**

**Goal:** Calendar sync & notifications

#### Week 7-8: Google Calendar Integration

| Task | Priority | Effort | Assignee |
|------|----------|--------|----------|
| OAuth2 flow for Google Calendar | P3 | 2 days | - |
| Import external events as exceptions | P3 | 3 days | - |
| Export PTM schedule to Google | P3 | 2 days | - |
| Webhook for real-time updates | P3 | 2 days | - |

#### Week 9: Notification System

| Task | Priority | Effort | Assignee |
|------|----------|--------|----------|
| Create notification service | P1 | 2 days | - |
| Push notification for schedule changes | P1 | 2 days | - |
| Email reminders for deadlines | P2 | 1 day | - |

---

### **Phase 5: AI/LLM Integration (4+ weeks)**

**Goal:** Natural language interaction

| Task | Priority | Effort | Assignee |
|------|----------|--------|----------|
| serp_llm endpoint for task creation | P3 | 1 week | - |
| Context extraction from user data | P3 | 1 week | - |
| Smart scheduling suggestions | P3 | 1 week | - |
| Project planning wizard | P3 | 1 week | - |

---

## 📁 Database Schema Changes Needed

### **New Tables**

```sql
-- 1. Focus Time Blocks (ptm_schedule)
CREATE TABLE focus_time_blocks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    day_of_week INT NOT NULL,
    start_min INT NOT NULL,
    end_min INT NOT NULL,
    protection_level VARCHAR(20) DEFAULT 'STRICT', -- STRICT, FLEXIBLE
    active_status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Schedule Change Log (ptm_schedule)
CREATE TABLE schedule_change_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    schedule_plan_id BIGINT NOT NULL,
    event_id BIGINT,
    change_type VARCHAR(50) NOT NULL, -- CREATED, MOVED, CANCELLED, COMPLETED
    old_date_ms BIGINT,
    old_start_min INT,
    old_end_min INT,
    new_date_ms BIGINT,
    new_start_min INT,
    new_end_min INT,
    reason TEXT,
    source VARCHAR(50) DEFAULT 'SYSTEM', -- SYSTEM, USER
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Task Completion History (ptm_task or ptm_schedule)
CREATE TABLE task_completion_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    category VARCHAR(50),
    estimated_duration_min INT,
    actual_duration_min INT,
    completion_quality INT, -- 1-5 scale
    completed_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. User Scheduling Patterns (ptm_schedule)
CREATE TABLE user_scheduling_patterns (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    category_durations JSONB, -- {"coding": 45, "writing": 60, ...}
    preferred_hours JSONB, -- {"deep_work": [9, 10, 11], "meetings": [14, 15, 16]}
    productivity_by_hour JSONB, -- {"9": 0.95, "10": 0.90, ...}
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. Full-text search index on notes (ptm_task)
ALTER TABLE notes ADD COLUMN content_plain TEXT;
ALTER TABLE notes ADD COLUMN content_tsvector TSVECTOR 
    GENERATED ALWAYS AS (to_tsvector('english', COALESCE(content_plain, ''))) STORED;
CREATE INDEX idx_notes_content_fts ON notes USING GIN (content_tsvector);
```

---

## 🔧 Technical Debt to Address

| Item | Service | Priority | Effort |
|------|---------|----------|--------|
| Add more unit tests for scheduler | ptm_schedule | High | 2 days |
| Add integration tests (Kafka) | ptm_schedule | High | 2 days |
| Implement retry mechanism for Kafka | All | Medium | 1 day |
| Add metrics/monitoring (Prometheus) | All | Medium | 2 days |
| API documentation (OpenAPI/Swagger) | All | Medium | 1 day |
| Rate limiting on heavy endpoints | api_gateway | Low | 1 day |

---

## 📈 Success Metrics

### **Phase 1 Complete:**
- [ ] Notes searchable with FTS
- [ ] Attachments uploadable to S3
- [ ] Focus time blocks configurable
- [ ] Activity feed shows all changes

### **Phase 2 Complete:**
- [ ] Duration predictions within 20% of actual
- [ ] Deadline warnings 24h+ in advance
- [ ] Auto buffers reduce context switches

### **Phase 3 Complete:**
- [ ] MILP finds 10%+ better solutions
- [ ] Optimization completes in <5s
- [ ] A/B test data collected

### **Phase 4 Complete:**
- [ ] Google Calendar 2-way sync works
- [ ] Push notifications delivered
- [ ] External events auto-block schedule

---

## 🚀 Quick Wins (Can Do Today)

1. **Add full-text search index on notes** (1 day)
   - Just add `content_plain` column and tsvector index
   - Query with `to_tsquery()`

2. **Create schedule_change_log table** (0.5 day)
   - Insert log entries in existing event service methods

3. **Add focus_time_blocks table** (0.5 day)
   - Simple CRUD, similar to availability_calendar

4. **Update scheduler to respect focus blocks** (1 day)
   - Filter windows based on `is_deep_work` flag

---

## 📝 Notes

### **Key Decisions Made:**

1. **Hybrid Algorithm (Heuristic + MILP):** Use fast heuristic for instant feedback, MILP for background optimization
2. **Task Snapshots:** `schedule_tasks` copies from `tasks` to avoid cross-service joins
3. **Event-Driven:** All inter-service communication via Kafka events
4. **Single-User Focus:** No team features in MVP

### **Open Questions:**

1. Should duration learning be per-category or per-user-category?
2. What's the threshold for suggesting MILP results to users?
3. How to handle recurring tasks that span past deadline?

---

## 📚 References

- [PTM Use Case Specification](./PTM_USECASE_SPECIFICATION.md)
- [Motion App Comparison](./MOTION_APP_COMPARISON.md)
- [Reschedule Analysis](../../ptm_schedule/RESCHEDULE_ANALYSIS.md)

---

**Last Updated:** December 3, 2025
