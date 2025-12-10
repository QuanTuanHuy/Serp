# UC-CUST-004: Kích hoạt/Vô hiệu hóa Customer

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-CUST-004 |
| **Use Case Name** | Kích hoạt/Vô hiệu hóa Customer (Activate/Deactivate Customer) |
| **Actor** | Sales Manager, System Administrator |
| **Priority** | Medium |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép kích hoạt hoặc vô hiệu hóa một Customer. Customer bị vô hiệu hóa sẽ không thể tạo Opportunity mới nhưng vẫn giữ lại data lịch sử.

---

## 3. User Stories

### US-CUST-004-01
**As a** Sales Manager  
**I want to** deactivate a customer  
**So that** I can prevent new business activities with inactive customers

### US-CUST-004-02
**As a** Sales Manager  
**I want to** reactivate a deactivated customer  
**So that** I can resume business activities when needed

---

## 4. Preconditions

| ID | Điều kiện |
|----|-----------|
| PRE-01 | Customer tồn tại trong hệ thống |
| PRE-02 | Người dùng có quyền MANAGE_CUSTOMER_STATUS |

---

## 5. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Không thể activate Customer đang ACTIVE | Constraint |
| BR-002 | Không thể deactivate Customer đang INACTIVE | Constraint |
| BR-003 | Deactivate không xóa data, chỉ change status | Behavior |
| BR-004 | Customer INACTIVE không thể tạo Opportunity mới | Constraint |
| BR-005 | Opportunities hiện tại vẫn có thể tiếp tục | Behavior |

---

## 6. Main Flows

### 6.1 Deactivate Customer

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Mở Customer Detail |
| 2 | User | - | Click "Deactivate" |
| 3 | - | System | Hiển thị confirmation dialog |
| 4 | User | - | Confirm deactivation |
| 5 | - | System | Kiểm tra Customer đang ACTIVE |
| 6 | - | System | Set activeStatus = INACTIVE |
| 7 | - | System | Ghi updatedBy |
| 8 | - | System | Lưu và trả về kết quả |

### 6.2 Activate Customer

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Mở Customer Detail (INACTIVE) |
| 2 | User | - | Click "Activate" |
| 3 | - | System | Kiểm tra Customer đang INACTIVE |
| 4 | - | System | Set activeStatus = ACTIVE |
| 5 | - | System | Ghi updatedBy |
| 6 | - | System | Lưu và trả về kết quả |

---

## 7. API Specification

### Activate Customer
```
PUT /crm/api/v1/customers/{customerId}/activate
```

### Deactivate Customer
```
PUT /crm/api/v1/customers/{customerId}/deactivate
```

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Customer deactivated successfully",
  "data": {
    "id": 101,
    "name": "ABC Company Ltd",
    "activeStatus": "INACTIVE",
    "updatedAt": "2025-12-10T14:30:00Z",
    "updatedBy": 789
  }
}
```

---

## 8. Exception Flows

### EF-01: Already Active
```json
{
  "code": 400,
  "message": "Customer is already active"
}
```

### EF-02: Already Inactive
```json
{
  "code": 400,
  "message": "Customer is already inactive"
}
```

---

## 9. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Deactivate ACTIVE customer | Success, status = INACTIVE |
| TC-002 | Deactivate INACTIVE customer | Error - already inactive |
| TC-003 | Activate INACTIVE customer | Success, status = ACTIVE |
| TC-004 | Activate ACTIVE customer | Error - already active |

---

## 10. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
