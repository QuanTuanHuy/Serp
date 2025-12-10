# UC-OPP-006: Phân công Opportunity

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-OPP-006 |
| **Use Case Name** | Phân công Opportunity (Assign Opportunity) |
| **Actor** | Sales Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép Sales Manager phân công Opportunity cho Sales Representative phù hợp.

---

## 3. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Chỉ assign cho user có quyền VIEW_OPPORTUNITY | Authorization |
| BR-002 | Không thể assign Opportunity đã closed | Constraint |
| BR-003 | Gửi notification cho người được assign | Behavior |
| BR-004 | Ghi log thay đổi assignment | Audit |

---

## 4. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | Manager | - | Mở Opportunity Detail |
| 2 | Manager | - | Click "Assign" |
| 3 | - | System | Hiển thị danh sách Sales Reps |
| 4 | Manager | - | Chọn người assign |
| 5 | Manager | - | Confirm |
| 6 | - | System | Update assignedTo |
| 7 | - | System | Gửi notification |
| 8 | - | System | Trả về kết quả |

---

## 5. API Specification

### Endpoint
```
PUT /crm/api/v1/opportunities/{opportunityId}/assign
```

### Request Body
```json
{
  "assignedTo": 123
}
```

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Opportunity assigned successfully",
  "data": {
    "id": 789,
    "name": "ABC Company - Enterprise License",
    "assignedTo": {
      "id": 123,
      "name": "Trần Văn B"
    },
    "updatedAt": "2025-12-10T14:30:00Z"
  }
}
```

---

## 6. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Assign cho user hợp lệ | Success |
| TC-002 | Assign cho user không tồn tại | Error |
| TC-003 | Assign Opportunity đã closed | Error |

---

## 7. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
