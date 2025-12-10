# UC-CUST-002: Cập nhật Customer

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-CUST-002 |
| **Use Case Name** | Cập nhật Customer (Update Customer) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép người dùng cập nhật thông tin của một Customer hiện có trong hệ thống.

---

## 3. User Stories

### US-CUST-002-01
**As a** Sales Representative  
**I want to** update customer contact information  
**So that** I can keep the customer data accurate

### US-CUST-002-02
**As a** Sales Manager  
**I want to** update customer credit limit  
**So that** I can manage financial exposure appropriately

---

## 4. Preconditions

| ID | Điều kiện |
|----|-----------|
| PRE-01 | Customer tồn tại trong hệ thống |
| PRE-02 | Người dùng có quyền UPDATE_CUSTOMER |

---

## 5. Postconditions

| ID | Điều kiện |
|----|-----------|
| POST-01 | Thông tin Customer được cập nhật |
| POST-02 | updatedBy và updatedAt được ghi nhận |
| POST-03 | Event CUSTOMER_UPDATED được publish |

---

## 6. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Name không được để trống | Validation |
| BR-002 | Credit limit phải >= 0 | Validation |
| BR-003 | Không thể update totalRevenue, totalOpportunities trực tiếp | Constraint |
| BR-004 | Partial update được hỗ trợ | Behavior |

---

## 7. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Truy cập màn hình Customer Detail |
| 2 | User | - | Click "Edit" |
| 3 | - | System | Load và hiển thị form với data hiện tại |
| 4 | User | - | Chỉnh sửa các trường cần thiết |
| 5 | User | - | Click "Save" |
| 6 | - | System | Validate dữ liệu |
| 7 | - | System | Update entity với updateFrom() |
| 8 | - | System | Lưu vào database |
| 9 | - | System | Publish event CUSTOMER_UPDATED |
| 10 | - | System | Trả về Customer đã cập nhật |

---

## 8. API Specification

### Endpoint

```
PUT /crm/api/v1/customers/{customerId}
```

### Request Body

```json
{
  "name": "ABC Company Ltd (Updated)",
  "phone": "+84901234568",
  "industry": "Technology & Services",
  "notes": "Updated company profile"
}
```

### Response (Success - 200 OK)

```json
{
  "code": 200,
  "message": "Customer updated successfully",
  "data": {
    "id": 101,
    "name": "ABC Company Ltd (Updated)",
    "phone": "+84901234568",
    "industry": "Technology & Services",
    "updatedAt": "2025-12-10T14:30:00Z",
    "updatedBy": 789
  }
}
```

---

## 9. Exception Flows

### EF-01: Customer Not Found

| Condition | Response |
|-----------|----------|
| Customer ID không tồn tại | HTTP 404 Not Found |

---

## 10. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Update thông tin cơ bản | Success |
| TC-002 | Update Customer không tồn tại | 404 Not Found |
| TC-003 | Update với name = null | Validation error |
| TC-004 | Partial update | Chỉ fields được gửi được update |

---

## 11. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
