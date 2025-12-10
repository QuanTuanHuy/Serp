# UC-CUST-001: Tạo Customer Mới

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-CUST-001 |
| **Use Case Name** | Tạo Customer Mới (Create New Customer) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép người dùng tạo mới một Customer (khách hàng) trong hệ thống CRM. Customer có thể được tạo trực tiếp hoặc từ quá trình convert Lead.

---

## 3. User Stories

### US-CUST-001-01
**As a** Sales Representative  
**I want to** create a new customer record  
**So that** I can track customer information and opportunities

### US-CUST-001-02
**As a** Sales Manager  
**I want to** set up customer credit limits  
**So that** I can manage financial exposure

---

## 4. Preconditions

| ID | Điều kiện |
|----|-----------|
| PRE-01 | Người dùng đã đăng nhập |
| PRE-02 | Người dùng có quyền CREATE_CUSTOMER |

---

## 5. Postconditions

| ID | Điều kiện |
|----|-----------|
| POST-01 | Customer mới được tạo với status ACTIVE |
| POST-02 | Audit information được ghi nhận |
| POST-03 | Event CUSTOMER_CREATED được publish |

---

## 6. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Customer name là bắt buộc | Mandatory |
| BR-002 | Email phải unique trong hệ thống (nếu có) | Validation |
| BR-003 | Credit limit phải >= 0 | Validation |
| BR-004 | Customer mới có activeStatus = ACTIVE | Default |
| BR-005 | totalOpportunities, wonOpportunities, totalRevenue = 0 | Default |

---

## 7. Data Requirements

### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | String | Yes | Max 255 chars | Tên khách hàng/công ty |
| phone | String | No | Phone format | Số điện thoại |
| email | String | No | Email format, unique | Email |
| website | String | No | URL format | Website |
| industry | String | No | Max 100 chars | Ngành nghề |
| companySize | String | No | Max 50 chars | Quy mô |
| parentCustomerId | Long | No | Valid customer ID | Công ty mẹ |
| taxId | String | No | Max 50 chars | Mã số thuế |
| creditLimit | Decimal | No | >= 0 | Hạn mức tín dụng |
| notes | String | No | Max 2000 chars | Ghi chú |
| address | Object | No | - | Thông tin địa chỉ |

---

## 8. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Truy cập màn hình tạo Customer mới |
| 2 | User | - | Nhập thông tin Customer |
| 3 | User | - | Click "Create Customer" |
| 4 | - | System | Validate dữ liệu đầu vào |
| 5 | - | System | Kiểm tra email unique (nếu có) |
| 6 | - | System | Tạo CustomerEntity với defaults |
| 7 | - | System | Lưu vào database |
| 8 | - | System | Publish event CUSTOMER_CREATED |
| 9 | - | System | Trả về Customer đã tạo |

---

## 9. Alternative Flows

### AF-01: Create from Lead Conversion

| Step | Description |
|------|-------------|
| 1a | Use case được trigger từ UC-LEAD-004 (Convert Lead) |
| 2a | Thông tin được copy từ Lead |
| 3a | Contact chính được tạo cùng Customer |

### AF-02: Create with Contacts

| Step | Description |
|------|-------------|
| 2a | User thêm contacts trong cùng form |
| 7a | System tạo Customer và Contacts trong transaction |

---

## 10. API Specification

### Endpoint

```
POST /crm/api/v1/customers
```

### Request Body

```json
{
  "name": "ABC Company Ltd",
  "phone": "+84901234567",
  "email": "contact@abc.com",
  "website": "https://abc.com",
  "industry": "Technology",
  "companySize": "50-200",
  "taxId": "0123456789",
  "creditLimit": 100000000,
  "notes": "Key account in technology sector",
  "address": {
    "street": "123 Nguyen Hue",
    "city": "Ho Chi Minh",
    "state": "Ho Chi Minh",
    "country": "Vietnam",
    "postalCode": "70000"
  }
}
```

### Response (Success - 201 Created)

```json
{
  "code": 201,
  "message": "Customer created successfully",
  "data": {
    "id": 101,
    "name": "ABC Company Ltd",
    "email": "contact@abc.com",
    "activeStatus": "ACTIVE",
    "creditLimit": 100000000,
    "totalOpportunities": 0,
    "wonOpportunities": 0,
    "totalRevenue": 0,
    "createdAt": "2025-12-10T10:30:00Z",
    "createdBy": 789
  }
}
```

---

## 11. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Tạo Customer với thông tin tối thiểu | Success, status = ACTIVE |
| TC-002 | Tạo Customer với đầy đủ thông tin | Success |
| TC-003 | Tạo Customer thiếu name | Validation error |
| TC-004 | Tạo Customer với email trùng | Error - email exists |
| TC-005 | Tạo Customer với credit limit âm | Validation error |

---

## 12. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
