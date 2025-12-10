# UC-CUST-005: Cập nhật Credit Limit

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-CUST-005 |
| **Use Case Name** | Cập nhật Credit Limit (Update Credit Limit) |
| **Actor** | Sales Manager, Finance Manager |
| **Priority** | Medium |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép cập nhật hạn mức tín dụng của Customer để quản lý rủi ro tài chính và điều chỉnh chính sách thanh toán cho từng khách hàng.

---

## 3. User Stories

### US-CUST-005-01
**As a** Finance Manager  
**I want to** set credit limits for customers  
**So that** I can manage financial risk exposure

### US-CUST-005-02
**As a** Sales Manager  
**I want to** increase credit limits for good customers  
**So that** I can facilitate larger deals

---

## 4. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Credit limit phải >= 0 | Validation |
| BR-002 | Chỉ user có quyền đặc biệt mới được update | Authorization |
| BR-003 | Ghi log khi thay đổi credit limit | Audit |
| BR-004 | Có thể set credit limit = 0 để block credit | Behavior |

---

## 5. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Mở Customer Detail |
| 2 | User | - | Click "Update Credit Limit" |
| 3 | - | System | Hiển thị form với giá trị hiện tại |
| 4 | User | - | Nhập credit limit mới |
| 5 | User | - | Click "Save" |
| 6 | - | System | Validate credit limit >= 0 |
| 7 | - | System | Gọi updateCreditLimit() |
| 8 | - | System | Lưu và ghi audit log |
| 9 | - | System | Trả về kết quả |

---

## 6. API Specification

### Endpoint
```
PUT /crm/api/v1/customers/{customerId}/credit-limit
```

### Request Body
```json
{
  "creditLimit": 200000000
}
```

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Credit limit updated successfully",
  "data": {
    "id": 101,
    "name": "ABC Company Ltd",
    "creditLimit": 200000000,
    "updatedAt": "2025-12-10T14:30:00Z",
    "updatedBy": 789
  }
}
```

---

## 7. Exception Flows

### EF-01: Negative Credit Limit
```json
{
  "code": 400,
  "message": "Credit limit cannot be negative"
}
```

---

## 8. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Update credit limit với giá trị hợp lệ | Success |
| TC-002 | Update credit limit = 0 | Success |
| TC-003 | Update credit limit âm | Error |
| TC-004 | Update với customer không tồn tại | 404 Error |

---

## 9. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
