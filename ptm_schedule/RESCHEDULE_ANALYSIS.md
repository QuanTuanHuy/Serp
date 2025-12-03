# Phân tích Reschedule Queue và Strategy Service

## 1. Tổng quan các loại Trigger

| Trigger Type | Priority | Entity Type | Khi nào xảy ra | Strategy được chọn |
|-------------|----------|-------------|----------------|-------------------|
| `TriggerManualDrag` | 1 | EVENT | User kéo thả event trên UI | `StrategyRipple` |
| `TriggerEventSplit` | 2 | EVENT | User chia nhỏ event | `StrategyRipple` |
| `TriggerEventComplete` | 2 | EVENT | User đánh dấu hoàn thành | `StrategyRipple` |
| `TriggerConstraintChange` | 5 | TASK | Task thay đổi deadline/priority/duration | `StrategyInsertion` |
| `TriggerTaskAdded` | 5 | TASK | Task mới được tạo | `StrategyInsertion` |
| `TriggerTaskDeleted` | 5 | TASK | Task bị xóa | `StrategyInsertion` |
| `TriggerAvailability` | 9 | (empty) | User thay đổi availability windows | `StrategyFullReplan` |

---

## 2. Luồng xử lý Reschedule

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            USER ACTIONS                                      │
└─────────────────────────────────────────────────────────────────────────────┘
       │                    │                    │                    │
       ▼                    ▼                    ▼                    ▼
  Task CRUD          Event Move/Split      Event Complete      Availability
       │                    │                    │                    │
       ▼                    ▼                    ▼                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     RESCHEDULE QUEUE SERVICE                                 │
│  EnqueueTaskChange()  EnqueueEventMove()  EnqueueEventComplete()            │
└─────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      RESCHEDULE WORKER (polling)                            │
│  1. GetDirtyPlanIDs()                                                       │
│  2. FetchAndLockBatch() → []*RescheduleQueueItem                           │
│  3. DetermineStrategy() → Ripple/Insertion/FullReplan                      │
│  4. Execute()                                                               │
└─────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    RESCHEDULE STRATEGY SERVICE                              │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────┐                      │
│  │ RunRipple() │  │RunInsertion()│  │RunFullReplan()│                      │
│  │ Full sched  │  │ Incremental  │  │ Clear & redo  │                      │
│  └─────────────┘  └──────────────┘  └───────────────┘                      │
└─────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       HYBRID SCHEDULER                                       │
│  Schedule() / ScheduleIncremental()                                         │
│  - Greedy Insertion                                                         │
│  - Ripple Effect (for critical tasks)                                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Phân tích chi tiết từng Trigger

### 3.1. TriggerTaskAdded ✅

**Flow:**
```
Kafka Event → HandleTaskCreated() → CreateSnapshot() → EnqueueTaskChange()
    → EntityType="TASK", EntityID=scheduleTaskID
    → Strategy: Insertion
    → AffectedTaskIDs: [newScheduleTaskID]
    → ScheduleIncremental() với task mới
```

**Kết quả:** ✅ HOẠT ĐỘNG ĐÚNG - Task mới được insert vào schedule

---

### 3.2. TriggerTaskDeleted 🔴 BUG

**Flow:**
```
Kafka Event → HandleTaskDeleted() 
    → DeleteSnapshot() (XÓA TASK KHỎI DB TRƯỚC)
    → EnqueueTaskChange()
    → EntityType="TASK", EntityID=scheduleTaskID (đã bị xóa)
```

**Vấn đề:**
1. Task đã bị xóa khỏi DB **TRƯỚC** khi enqueue
2. `loadScheduleData()` sẽ không load được task đã xóa
3. `AffectedScheduleTaskIDs()` trả về ID không tồn tại trong task list
4. `filterTasksByIDs()` trả về empty list
5. **Events của deleted task KHÔNG bị xóa!**

**Code path:**
```go
// hybrid_scheduler.go
func (s *HybridScheduler) ScheduleIncremental(input *ScheduleInput, newTaskIDs []int64) {
    // newTaskIDs chứa ID của task đã xóa
    newTasks := s.filterTasksByIDs(input.Tasks, newTaskIDs)
    // newTasks = [] vì task không còn trong input.Tasks
    
    // ExistingEvents VẪN CHỨA events của deleted task!
    currentSchedule := make([]*Assignment, len(input.ExistingEvents))
    copy(currentSchedule, input.ExistingEvents)
    
    // Không có task mới nào để schedule
    // Events cũ được giữ nguyên → BUG!
}
```

**Hậu quả:** Events orphan (không có task tương ứng) vẫn tồn tại trong schedule.

---

### 3.3. TriggerConstraintChange 🟠 POTENTIAL BUG

**Flow:**
```
Kafka Event → HandleTaskUpdated() → SyncSnapshot() 
    → EnqueueTaskChange()
    → EntityType="TASK", EntityID=scheduleTaskID
    → Strategy: Insertion
```

**Vấn đề:**
```go
// hybrid_scheduler.go - ScheduleIncremental
func (s *HybridScheduler) ScheduleIncremental(input *ScheduleInput, affectedTaskIDs []int64) {
    // ⚠️ EXISTING EVENTS BAO GỒM CẢ EVENTS CŨ CỦA AFFECTED TASK
    currentSchedule := make([]*Assignment, len(input.ExistingEvents))
    copy(currentSchedule, input.ExistingEvents)

    // Lọc ra affected tasks (với constraints mới)
    newTasks := s.filterTasksByIDs(input.Tasks, affectedTaskIDs)
    
    for _, task := range sortedNewTasks {
        // Cố insert task vào gaps
        // NHƯNG events cũ của task này vẫn chiếm chỗ!
        inserted := s.tryInsertTask(task, &currentSchedule, input.Windows)
    }
}
```

**Kịch bản lỗi:**
1. Task có duration 60 min, đã schedule tại 9:00-10:00
2. User thay đổi duration thành 90 min
3. `ScheduleIncremental()` được gọi
4. Event cũ (60 min) vẫn ở 9:00-10:00
5. Thuật toán cố schedule task 90 min nhưng slot 9:00-10:00 đã bị chiếm
6. Kết quả: Task có thể bị schedule ở chỗ khác hoặc fail

**Hậu quả có thể:**
- Duplicate events cho cùng 1 task
- Task không được reschedule đúng
- Events cũ và mới conflict

---

### 3.4. TriggerManualDrag 🟡 WORKS BUT SUBOPTIMAL

**Flow:**
```
UI → ManuallyMoveEvent() 
    → MoveAndPinEvent() (set IsPinned=true, cập nhật DB)
    → EnqueueEventMove() với payload {newDateMs, newStartMin, newEndMin}
    → EntityType="EVENT", EntityID=eventID
    → Strategy: Ripple (vì không match Availability/Constraint/TaskAdded/TaskDeleted)
```

**Cách hoạt động:**
```go
// DetermineStrategy()
func (b *RescheduleBatch) DetermineStrategy() enum.RescheduleStrategy {
    if b.HasTrigger(enum.TriggerAvailability) { return StrategyFullReplan }
    if b.HasTrigger(enum.TriggerConstraintChange) || ... { return StrategyInsertion }
    return enum.StrategyRipple  // ← ManualDrag đi vào đây
}

// runSchedule() với StrategyRipple
if useRipple || strategy == enum.StrategyRipple {
    output = s.scheduler.Schedule(input)  // FULL SCHEDULE
}
```

**Thông tin sử dụng:**
- ❌ `ChangePayload` (chứa newDateMs, newStartMin, newEndMin) - **KHÔNG ĐƯỢC SỬ DỤNG**
- ✅ `IsPinned` flag từ DB - Thuật toán dựa vào flag này

**Tại sao vẫn hoạt động:**
```go
// schedule_event_service.go - MoveAndPinEvent()
event.IsPinned = true  // Được lưu vào DB TRƯỚC khi enqueue

// Sau đó khi scheduler chạy:
func (s *HybridScheduler) Schedule(input *ScheduleInput) {
    pinnedAssignments := s.extractPinnedAssignments(...)
    // Event đã move được giữ nguyên vì IsPinned=true
    
    for _, task := range sortedTasks {
        if task.IsPinned { continue }  // Skip pinned
    }
}
```

**Vấn đề:**
1. Chạy full `Schedule()` thay vì chỉ reschedule các tasks bị conflict
2. `ChangePayload` được lưu nhưng lãng phí
3. Hiệu suất không tối ưu cho thay đổi nhỏ

---

### 3.5. TriggerEventSplit ✅

**Flow:**
```
UI → SplitEvent() 
    → Tạo 2 events trong DB
    → EnqueueEventSplit() với payload
    → Strategy: Ripple (full schedule)
```

**Kết quả:** ✅ HOẠT ĐỘNG - Events đã được cập nhật trong DB trước khi reschedule

---

### 3.6. TriggerEventComplete ✅

**Flow:**
```
UI → CompleteEvent() → status = Completed
    → EnqueueEventComplete()
    → Strategy: Ripple
```

**Kết quả:** ✅ HOẠT ĐỘNG - Completed events được giữ nguyên, free up time cho tasks khác

---

### 3.7. TriggerAvailability ✅

**Flow:**
```
UI → UpdateAvailability() → EnqueueAvailabilityChange()
    → EntityType="", EntityID=0
    → Strategy: FullReplan
```

**Code:**
```go
// runFullReplan()
func (s *RescheduleStrategyService) runFullReplan(...) {
    // Chỉ giữ lại pinned events
    input.ExistingEvents = filterPinnedOnly(input.ExistingEvents)
    
    // Schedule lại từ đầu
    output := s.scheduler.Schedule(input)
}
```

**Kết quả:** ✅ HOẠT ĐỘNG ĐÚNG

---

## 4. Tổng hợp các vấn đề

| Severity | Trigger | Vấn đề | Impact |
|----------|---------|--------|--------|
| 🔴 CRITICAL | `TriggerTaskDeleted` | Events của deleted task KHÔNG bị xóa | Orphan events trong schedule |
| 🟠 HIGH | `TriggerConstraintChange` | Events cũ không bị xóa trước reschedule | Duplicate/conflict events |
| 🟡 MEDIUM | `TriggerManualDrag` | `ChangePayload` không được sử dụng | Lãng phí data, không tối ưu |
| 🟡 MEDIUM | `TriggerEventSplit` | `ChangePayload` không được sử dụng | Lãng phí data |
| 🔵 LOW | All Event triggers | `AffectedScheduleTaskIDs()` bỏ qua EntityTypeEvent | Không thể incremental cho event changes |

---

## 5. Đề xuất sửa lỗi

### 5.1. Fix TriggerTaskDeleted (CRITICAL)

**Option A: Xóa events trong DeleteSnapshot()**
```go
// schedule_task_service.go
func (s *ScheduleTaskService) DeleteSnapshot(ctx context.Context, tx *gorm.DB, planID, taskID int64) error {
    scheduleTask, err := s.taskPort.GetByPlanIDAndTaskID(ctx, planID, taskID)
    if err != nil {
        return err
    }
    
    // XÓA EVENTS TRƯỚC
    if err := s.eventPort.DeleteByScheduleTaskID(ctx, tx, scheduleTask.ID); err != nil {
        return err
    }
    
    // Sau đó xóa task
    return s.taskPort.DeleteByID(ctx, tx, scheduleTask.ID)
}
```

**Option B: Xử lý trong Strategy Service**
```go
// reschedule_strategy_service.go
func (s *RescheduleStrategyService) runSchedule(...) {
    // Xóa events của deleted tasks trước khi load data
    for _, item := range batch.Items {
        if item.TriggerType == enum.TriggerTaskDeleted {
            s.eventPort.DeleteByScheduleTaskID(ctx, nil, item.EntityID)
        }
    }
    
    input, taskMap, err := s.loadScheduleData(ctx, planID, batch.UserID)
    // ...
}
```

---

### 5.2. Fix TriggerConstraintChange (HIGH)

**Sửa trong ScheduleIncremental:**
```go
// hybrid_scheduler.go
func (s *HybridScheduler) ScheduleIncremental(input *ScheduleInput, affectedTaskIDs []int64) *ScheduleOutput {
    affectedSet := make(map[int64]bool)
    for _, id := range affectedTaskIDs {
        affectedSet[id] = true
    }
    
    // FILTER RA EVENTS CŨ CỦA AFFECTED TASKS
    currentSchedule := make([]*Assignment, 0, len(input.ExistingEvents))
    for _, e := range input.ExistingEvents {
        if !affectedSet[e.ScheduleTaskID] {
            currentSchedule = append(currentSchedule, e)
        }
    }

    // Giờ affected tasks sẽ được schedule fresh
    newTasks := s.filterTasksByIDs(input.Tasks, affectedTaskIDs)
    // ...
}
```

---

### 5.3. Tối ưu TriggerManualDrag (MEDIUM)

**Option A: Sử dụng ChangePayload để incremental reschedule**
```go
// reschedule_batch.go - thêm method
func (b *RescheduleBatch) GetAffectedScheduleTaskIDsFromEvents() []int64 {
    // Lấy scheduleTaskID từ eventID thông qua lookup
    // Hoặc store scheduleTaskID trong payload
}

// reschedule_strategy_service.go
func (s *RescheduleStrategyService) runSchedule(...) {
    affectedTaskIDs := batch.AffectedScheduleTaskIDs()
    
    // Nếu là event trigger, lấy task IDs từ events
    if len(affectedTaskIDs) == 0 {
        affectedTaskIDs = s.getTaskIDsFromEventTriggers(ctx, batch)
    }
    
    if len(affectedTaskIDs) == 0 {
        // Fallback to full schedule
        output = s.scheduler.Schedule(input)
    } else {
        output = s.scheduler.ScheduleIncremental(input, affectedTaskIDs)
    }
}
```

**Option B: Thêm scheduleTaskID vào payload khi enqueue**
```go
// schedule_event_usecase.go - ManuallyMoveEvent()
payload := map[string]any{
    "newDateMs":       newDateMs,
    "newStartMin":     newStartMin,
    "newEndMin":       newEndMin,
    "scheduleTaskID":  result.Event.ScheduleTaskID,  // THÊM
}
```

---

### 5.4. Mở rộng AffectedScheduleTaskIDs (LOW)

```go
// reschedule_queue_item.go
func (b *RescheduleBatch) AffectedScheduleTaskIDs() []int64 {
    seen := make(map[int64]bool)
    result := make([]int64, 0)

    for _, item := range b.Items {
        var taskID int64
        
        if item.EntityType == constant.EntityTypeTask && item.EntityID > 0 {
            taskID = item.EntityID
        } else if item.EntityType == constant.EntityTypeEvent && item.ChangePayload != "" {
            // Parse scheduleTaskID từ payload
            var payload map[string]any
            if json.Unmarshal([]byte(item.ChangePayload), &payload) == nil {
                if id, ok := payload["scheduleTaskID"].(float64); ok {
                    taskID = int64(id)
                }
            }
        }
        
        if taskID > 0 && !seen[taskID] {
            seen[taskID] = true
            result = append(result, taskID)
        }
    }

    return result
}
```

---

## 6. Test Cases cần thêm

```go
// reschedule_strategy_service_test.go

func TestRunSchedule_TaskDeleted_ShouldRemoveOrphanEvents(t *testing.T) {
    // Setup: Create task with events
    // Action: Delete task, trigger reschedule
    // Assert: Events should be removed
}

func TestRunSchedule_ConstraintChange_ShouldReplaceOldEvents(t *testing.T) {
    // Setup: Task with 60min duration scheduled at 9:00-10:00
    // Action: Change duration to 90min, trigger reschedule
    // Assert: Old event removed, new event scheduled correctly
}

func TestRunSchedule_ManualDrag_WithConflicts_ShouldRescheduleConflicting(t *testing.T) {
    // Setup: Two events at 9:00-10:00 and 10:00-11:00
    // Action: Move first event to 10:30-11:30 (overlap)
    // Assert: Second event should be rescheduled
}
```

---

## 7. Kết luận

Implementation hiện tại có một số vấn đề nghiêm trọng cần được fix:

1. **TriggerTaskDeleted** - Bug critical, cần fix ngay
2. **TriggerConstraintChange** - Cần cải thiện logic trong `ScheduleIncremental`
3. **ChangePayload** - Đang lưu nhưng không sử dụng, cần quyết định có dùng hay bỏ

Các triggers khác (TaskAdded, EventSplit, EventComplete, Availability) hoạt động đúng.
