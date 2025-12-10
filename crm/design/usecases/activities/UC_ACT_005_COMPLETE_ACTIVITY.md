# UC-ACT-005: Hoàn thành Activity

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-ACT-005 |
| **Use Case Name** | Hoàn thành Activity (Complete Activity) |
| **Actor** | Sales Representative |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép đánh dấu một Activity (Call, Meeting, Email, Task) đã hoàn thành.

---

## 3. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Activity phải đang ở status PLANNED | Constraint |
| BR-002 | Set status = COMPLETED | Behavior |
| BR-003 | Set progressPercent = 100 | Behavior |
| BR-004 | Không thể complete Activity đã CANCELLED | Constraint |
| BR-005 | Ghi nhận người complete và thời gian | Audit |

---

## 4. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Mở Activity Detail |
| 2 | User | - | Click "Mark as Complete" |
| 3 | - | System | Kiểm tra status = PLANNED |
| 4 | - | System | Gọi markAsCompleted() |
| 5 | - | System | Set status = COMPLETED, progressPercent = 100 |
| 6 | - | System | Ghi updatedBy |
| 7 | - | System | Publish event ACTIVITY_COMPLETED |
| 8 | - | System | Trả về kết quả |

---

## 5. API Specification

### Endpoint
```
PUT /crm/api/v1/activities/{activityId}/complete
```

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Activity completed successfully",
  "data": {
    "id": 1001,
    "activityType": "CALL",
    "subject": "Follow-up call with CEO",
    "status": "COMPLETED",
    "progressPercent": 100,
    "updatedAt": "2025-12-10T14:30:00Z",
    "updatedBy": 123
  }
}
```

---

## 6. Exception Flows

### EF-01: Already Completed
```json
{
  "code": 400,
  "message": "Activity is already completed"
}
```

### EF-02: Already Cancelled
```json
{
  "code": 400,
  "message": "Cannot complete a cancelled activity"
}
```

---

## 7. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Complete PLANNED activity | Success, status = COMPLETED |
| TC-002 | Complete already completed | Error |
| TC-003 | Complete cancelled activity | Error |
| TC-004 | progressPercent = 100 | Verified |

---

## 8. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
