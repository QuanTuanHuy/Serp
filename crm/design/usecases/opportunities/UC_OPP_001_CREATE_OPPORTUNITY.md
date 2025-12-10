# UC-OPP-001: Tạo Opportunity Mới

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-OPP-001 |
| **Use Case Name** | Tạo Opportunity Mới (Create New Opportunity) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | Critical |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép tạo mới một Opportunity (cơ hội bán hàng) trong hệ thống. Opportunity đại diện cho một deal tiềm năng với khách hàng.

---

## 3. User Stories

### US-OPP-001-01
**As a** Sales Representative  
**I want to** create a new opportunity  
**So that** I can track the sales process through the pipeline

### US-OPP-001-02
**As a** Sales Manager  
**I want to** see all opportunities in the pipeline  
**So that** I can forecast revenue accurately

---

## 4. Preconditions

| ID | Điều kiện |
|----|-----------|
| PRE-01 | Người dùng đã đăng nhập |
| PRE-02 | Người dùng có quyền CREATE_OPPORTUNITY |
| PRE-03 | Customer tồn tại (nếu link với customer) |
| PRE-04 | Lead tồn tại (nếu tạo từ lead conversion) |

---

## 5. Postconditions

| ID | Điều kiện |
|----|-----------|
| POST-01 | Opportunity được tạo với stage = PROSPECTING |
| POST-02 | Probability được set tự động theo stage |
| POST-03 | Event OPPORTUNITY_CREATED được publish |

---

## 6. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Name là bắt buộc | Mandatory |
| BR-002 | Stage mặc định = PROSPECTING | Default |
| BR-003 | Probability được set theo stage | Auto-calculated |
| BR-004 | estimatedValue phải >= 0 | Validation |
| BR-005 | expectedCloseDate nên là ngày trong tương lai | Validation |

### Probability by Stage

| Stage | Probability |
|-------|-------------|
| PROSPECTING | 10% |
| QUALIFICATION | 25% |
| PROPOSAL | 50% |
| NEGOTIATION | 75% |
| CLOSED_WON | 100% |
| CLOSED_LOST | 0% |

---

## 7. Data Requirements

### Input Data

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Tên opportunity |
| description | String | No | Mô tả chi tiết |
| customerId | Long | No | Link với customer |
| leadId | Long | No | Link với lead (nếu convert) |
| estimatedValue | Decimal | No | Giá trị ước tính |
| expectedCloseDate | Date | No | Ngày dự kiến close |
| assignedTo | Long | No | Người phụ trách |
| notes | String | No | Ghi chú |

---

## 8. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Truy cập màn hình tạo Opportunity |
| 2 | User | - | Nhập thông tin Opportunity |
| 3 | User | - | (Optional) Link với Customer |
| 4 | User | - | Click "Create" |
| 5 | - | System | Validate dữ liệu |
| 6 | - | System | Set defaults (stage, probability) |
| 7 | - | System | Lưu vào database |
| 8 | - | System | Publish event |
| 9 | - | System | Trả về Opportunity đã tạo |

---

## 9. API Specification

### Endpoint
```
POST /crm/api/v1/opportunities
```

### Request Body
```json
{
  "name": "ABC Company - Enterprise License",
  "description": "Annual enterprise license deal",
  "customerId": 101,
  "estimatedValue": 100000000,
  "expectedCloseDate": "2026-03-15",
  "assignedTo": 123,
  "notes": "High priority deal"
}
```

### Response (Success - 201 Created)
```json
{
  "code": 201,
  "message": "Opportunity created successfully",
  "data": {
    "id": 789,
    "name": "ABC Company - Enterprise License",
    "stage": "PROSPECTING",
    "probability": 10,
    "estimatedValue": 100000000,
    "expectedCloseDate": "2026-03-15",
    "customerId": 101,
    "assignedTo": 123,
    "createdAt": "2025-12-10T10:30:00Z"
  }
}
```

---

## 10. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Tạo Opportunity mới | Success, stage = PROSPECTING |
| TC-002 | Tạo Opportunity thiếu name | Validation error |
| TC-003 | Tạo Opportunity với invalid customer | 404 Customer not found |
| TC-004 | Tạo Opportunity với estimatedValue < 0 | Validation error |

---

## 11. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
