# UC-ACT-003: Lên lịch Call

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-ACT-003 |
| **Use Case Name** | Lên lịch Call (Schedule Call) |
| **Actor** | Sales Representative |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép lên lịch cuộc gọi với khách hàng. Đây là extension của UC-ACT-001 với activity type = CALL.

---

## 3. User Stories

### US-ACT-003-01
**As a** Sales Representative  
**I want to** schedule follow-up calls  
**So that** I can maintain regular contact with leads

---

## 4. Call-specific Fields

| Field | Description |
|-------|-------------|
| durationMinutes | Thời lượng cuộc gọi dự kiến |
| activityDate | Thời điểm cuộc gọi |
| notes | Ghi chú, mục đích cuộc gọi |

---

## 5. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | activityType = CALL | Constraint |
| BR-002 | durationMinutes mặc định = 15 | Default |

---

## 6. API Specification

### Endpoint
```
POST /crm/api/v1/activities
```

### Request Body
```json
{
  "activityType": "CALL",
  "subject": "Follow-up call - Pricing discussion",
  "description": "Discuss final pricing and discount options",
  "leadId": 456,
  "activityDate": 1702300000000,
  "durationMinutes": 30,
  "priority": "MEDIUM",
  "assignedTo": 123
}
```

---

## 7. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Schedule call | Success, type = CALL |
| TC-002 | Schedule call với duration | Duration được ghi nhận |

---

## 8. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
