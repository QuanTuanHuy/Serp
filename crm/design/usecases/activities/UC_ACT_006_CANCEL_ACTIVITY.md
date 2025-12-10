# UC-ACT-006: Hủy Activity

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-ACT-006 |
| **Use Case Name** | Hủy Activity (Cancel Activity) |
| **Actor** | Sales Representative |
| **Priority** | Medium |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép hủy một Activity đã lên lịch nhưng không thể thực hiện.

---

## 3. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Activity phải đang ở status PLANNED | Constraint |
| BR-002 | Không thể hủy Activity đã COMPLETED | Constraint |
| BR-003 | Set status = CANCELLED | Behavior |
| BR-004 | Ghi nhận người hủy và thời gian | Audit |

---

## 4. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Mở Activity Detail |
| 2 | User | - | Click "Cancel Activity" |
| 3 | - | System | Confirm dialog |
| 4 | User | - | Confirm |
| 5 | - | System | Kiểm tra Activity chưa COMPLETED |
| 6 | - | System | Gọi markAsCancelled() |
| 7 | - | System | Set status = CANCELLED |
| 8 | - | System | Trả về kết quả |

---

## 5. API Specification

### Endpoint
```
PUT /crm/api/v1/activities/{activityId}/cancel
```

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Activity cancelled successfully",
  "data": {
    "id": 1001,
    "activityType": "MEETING",
    "subject": "Product Demo Meeting",
    "status": "CANCELLED",
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
  "message": "Cannot cancel a completed activity"
}
```

---

## 7. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Cancel PLANNED activity | Success, status = CANCELLED |
| TC-002 | Cancel COMPLETED activity | Error |
| TC-003 | Cancel already cancelled | Idempotent - success |

---

## 8. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
