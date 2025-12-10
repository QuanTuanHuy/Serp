# UC-LEAD-005: Phân công Lead (Assign Lead)

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-LEAD-005 |
| **Use Case Name** | Phân công Lead (Assign Lead) |
| **Actor** | Sales Manager, System (Auto-assign) |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép Sales Manager phân công Lead cho Sales Representative hoặc hệ thống tự động phân công theo các quy tắc được cấu hình.

---

## 3. User Stories

### US-LEAD-005-01
**As a** Sales Manager  
**I want to** assign leads to specific sales representatives  
**So that** leads are handled by the right person

### US-LEAD-005-02
**As a** Sales Manager  
**I want to** bulk assign multiple leads  
**So that** I can efficiently distribute workload

### US-LEAD-005-03
**As a** System Administrator  
**I want to** configure auto-assignment rules  
**So that** new leads are automatically distributed

---

## 4. Preconditions

| ID | Điều kiện |
|----|-----------|
| PRE-01 | Lead tồn tại và chưa CONVERTED/DISQUALIFIED |
| PRE-02 | Người được gán là user hợp lệ trong hệ thống |
| PRE-03 | Người thực hiện có quyền ASSIGN_LEAD |

---

## 5. Postconditions

| ID | Điều kiện |
|----|-----------|
| POST-01 | Lead.assignedTo được cập nhật |
| POST-02 | Notification được gửi cho người được gán |
| POST-03 | Activity log được ghi nhận |

---

## 6. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Không thể assign Lead đã CONVERTED | Constraint |
| BR-002 | Người được gán phải có quyền VIEW_LEAD | Authorization |
| BR-003 | Auto-assign theo round-robin trong team | Logic |
| BR-004 | Có thể re-assign Lead cho người khác | Behavior |
| BR-005 | Gửi notification khi assign | Behavior |

---

## 7. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | Manager | - | Xem danh sách Leads hoặc chi tiết Lead |
| 2 | Manager | - | Click "Assign" hoặc chọn từ dropdown |
| 3 | - | System | Hiển thị danh sách Sales Reps có thể assign |
| 4 | Manager | - | Chọn Sales Rep |
| 5 | Manager | - | (Optional) Thêm notes |
| 6 | Manager | - | Click "Confirm" |
| 7 | - | System | Cập nhật Lead.assignedTo |
| 8 | - | System | Ghi activity log |
| 9 | - | System | Gửi notification cho assignee |
| 10 | - | System | Trả về Lead đã cập nhật |

---

## 8. Alternative Flows

### AF-01: Bulk Assign

| Step | Description |
|------|-------------|
| 1a | Manager chọn nhiều Leads từ danh sách |
| 2a | Click "Bulk Assign" |
| 3a | Chọn Sales Rep để assign tất cả |
| 4a | System assign tất cả Leads đã chọn |

### AF-02: Auto-Assign (System)

| Step | Description |
|------|-------------|
| 1a | Lead mới được tạo mà không có assignedTo |
| 2a | System kiểm tra auto-assign rules |
| 3a | System assign theo round-robin hoặc territory |
| 4a | Notification được gửi cho assignee |

---

## 9. API Specification

### Endpoint - Single Assign

```
PUT /crm/api/v1/leads/{leadId}/assign
```

### Request Body

```json
{
  "assignedTo": 123,
  "notes": "Assigned based on territory"
}
```

### Endpoint - Bulk Assign

```
PUT /crm/api/v1/leads/bulk-assign
```

### Request Body

```json
{
  "leadIds": [1, 2, 3, 4, 5],
  "assignedTo": 123,
  "notes": "Bulk assignment for Q1 campaign leads"
}
```

### Response (Success - 200 OK)

```json
{
  "code": 200,
  "message": "Lead(s) assigned successfully",
  "data": {
    "assignedCount": 5,
    "assignedTo": {
      "id": 123,
      "name": "Trần Văn B"
    }
  }
}
```

---

## 10. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Assign Lead cho user hợp lệ | Success |
| TC-002 | Assign Lead đã converted | Error |
| TC-003 | Assign cho user không tồn tại | Error |
| TC-004 | Bulk assign nhiều Leads | Success |
| TC-005 | Auto-assign Lead mới | Lead được assign tự động |

---

## 11. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
