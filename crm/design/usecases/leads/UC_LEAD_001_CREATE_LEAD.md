# UC-LEAD-001: Tạo Lead Mới

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-LEAD-001 |
| **Use Case Name** | Tạo Lead Mới (Create New Lead) |
| **Actor** | Sales Representative, Marketing Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép người dùng tạo mới một Lead (khách hàng tiềm năng) trong hệ thống CRM. Lead có thể được tạo từ nhiều nguồn khác nhau như website, social media, referral, cold call, hoặc email campaign.

---

## 3. User Stories

### US-LEAD-001-01
**As a** Sales Representative  
**I want to** create a new lead with contact information  
**So that** I can track and nurture potential customers

### US-LEAD-001-02
**As a** Marketing Manager  
**I want to** import leads from marketing campaigns  
**So that** I can transfer qualified leads to the sales team

### US-LEAD-001-03
**As a** Sales Representative  
**I want to** record the lead source  
**So that** I can track which channels generate the most leads

---

## 4. Preconditions (Điều kiện tiên quyết)

| ID | Điều kiện |
|----|-----------|
| PRE-01 | Người dùng đã đăng nhập vào hệ thống |
| PRE-02 | Người dùng có quyền tạo Lead (CREATE_LEAD permission) |
| PRE-03 | Người dùng thuộc tenant/organization hợp lệ |

---

## 5. Postconditions (Điều kiện sau)

| ID | Điều kiện |
|----|-----------|
| POST-01 | Lead mới được tạo trong database với trạng thái NEW |
| POST-02 | Lead được gán ID unique |
| POST-03 | Thông tin audit (createdBy, createdAt) được ghi nhận |
| POST-04 | Event tạo lead được publish lên Kafka (nếu có) |

---

## 6. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Email phải có định dạng hợp lệ (nếu được cung cấp) | Validation |
| BR-002 | Phone phải có định dạng hợp lệ (nếu được cung cấp) | Validation |
| BR-003 | Tên Lead (name) là trường bắt buộc | Mandatory |
| BR-004 | LeadSource phải là một trong các giá trị: WEBSITE, SOCIAL_MEDIA, REFERRAL, COLD_CALL, EMAIL_CAMPAIGN | Validation |
| BR-005 | Lead mới tự động có trạng thái NEW | Default |
| BR-006 | Probability mặc định là 0% | Default |
| BR-007 | EstimatedValue phải >= 0 (nếu được cung cấp) | Validation |
| BR-008 | Probability phải trong khoảng 0-100 (nếu được cung cấp) | Validation |
| BR-009 | ExpectedCloseDate phải là ngày trong tương lai hoặc hôm nay | Validation |

---

## 7. Data Requirements

### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | String | Yes | Max 255 chars | Tên người liên hệ |
| email | String | No | Email format | Email liên hệ |
| phone | String | No | Phone format | Số điện thoại |
| company | String | No | Max 255 chars | Tên công ty |
| jobTitle | String | No | Max 100 chars | Chức danh |
| industry | String | No | Max 100 chars | Ngành nghề |
| companySize | String | No | Max 50 chars | Quy mô công ty |
| website | String | No | URL format | Website công ty |
| leadSource | Enum | No | Valid LeadSource | Nguồn lead |
| estimatedValue | Decimal | No | >= 0 | Giá trị ước tính |
| probability | Integer | No | 0-100 | Xác suất thành công |
| expectedCloseDate | Date | No | >= today | Ngày dự kiến chốt |
| assignedTo | Long | No | Valid user ID | Người được gán |
| notes | String | No | Max 2000 chars | Ghi chú |
| address | Object | No | - | Thông tin địa chỉ |

### Address Object

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| street | String | No | Địa chỉ đường |
| city | String | No | Thành phố |
| state | String | No | Tỉnh/Bang |
| country | String | No | Quốc gia |
| postalCode | String | No | Mã bưu chính |

### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | Long | ID của Lead mới tạo |
| name | String | Tên Lead |
| leadStatus | Enum | Trạng thái (NEW) |
| createdAt | DateTime | Thời gian tạo |
| createdBy | Long | ID người tạo |

---

## 8. Main Flow (Luồng chính)

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   User/Actor    │────▶│    Controller   │────▶│    UseCase      │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                                        │
                               ┌────────────────────────┘
                               ▼
                        ┌─────────────────┐
                        │    Service      │
                        └─────────────────┘
                               │
                               ▼
                        ┌─────────────────┐
                        │   Repository    │
                        └─────────────────┘
                               │
                               ▼
                        ┌─────────────────┐
                        │    Database     │
                        └─────────────────┘
```

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Truy cập màn hình tạo Lead mới |
| 2 | User | - | Nhập thông tin Lead (name, email, phone, company,...) |
| 3 | User | - | Chọn Lead Source từ dropdown |
| 4 | User | - | (Optional) Nhập estimated value và probability |
| 5 | User | - | (Optional) Chọn người được gán (assignedTo) |
| 6 | User | - | Click nút "Tạo Lead" / "Create Lead" |
| 7 | - | System | Validate dữ liệu đầu vào theo Business Rules |
| 8 | - | System | Tạo LeadEntity với các giá trị mặc định |
| 9 | - | System | Set leadStatus = NEW, probability = 0 (nếu chưa có) |
| 10 | - | System | Gán createdBy = current user ID |
| 11 | - | System | Lưu Lead vào database |
| 12 | - | System | Publish event LEAD_CREATED lên Kafka |
| 13 | - | System | Trả về thông tin Lead vừa tạo |
| 14 | User | - | Nhận thông báo tạo thành công |

---

## 9. Alternative Flows (Luồng thay thế)

### AF-01: Validation Failed (Lỗi validation)

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 7a | - | System | Phát hiện lỗi validation |
| 7b | - | System | Trả về danh sách lỗi với mã lỗi và message |
| 7c | User | - | Xem thông báo lỗi |
| 7d | User | - | Sửa thông tin và quay lại Step 6 |

### AF-02: Duplicate Lead Detection (Phát hiện Lead trùng)

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 7a | - | System | Phát hiện Lead có email/phone đã tồn tại |
| 7b | - | System | Hiển thị cảnh báo và thông tin Lead trùng |
| 7c | User | - | Chọn: (1) Tiếp tục tạo mới, (2) Xem Lead hiện tại, (3) Hủy |
| 7d | - | System | Thực hiện theo lựa chọn của user |

### AF-03: Auto-assign Lead (Tự động gán Lead)

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 5a | - | System | Nếu user không chọn assignedTo và có rule auto-assign |
| 5b | - | System | Tự động gán Lead theo round-robin hoặc territory rule |
| 5c | - | System | Tiếp tục Step 6 |

---

## 10. Exception Flows (Luồng ngoại lệ)

### EF-01: Unauthorized Access

| Condition | Response |
|-----------|----------|
| User không có quyền CREATE_LEAD | HTTP 403 Forbidden với message "Access denied" |

### EF-02: Database Error

| Condition | Response |
|-----------|----------|
| Lỗi kết nối database | HTTP 500 Internal Server Error, log error, rollback transaction |

### EF-03: Kafka Unavailable

| Condition | Response |
|-----------|----------|
| Không thể publish event lên Kafka | Lead vẫn được tạo, event được lưu vào outbox table để retry |

---

## 11. API Specification

### Endpoint

```
POST /crm/api/v1/leads
```

### Request Headers

| Header | Value | Required |
|--------|-------|----------|
| Authorization | Bearer {jwt_token} | Yes |
| Content-Type | application/json | Yes |

### Request Body

```json
{
  "name": "Nguyễn Văn A",
  "email": "nguyenvana@example.com",
  "phone": "+84901234567",
  "company": "ABC Company",
  "jobTitle": "CEO",
  "industry": "Technology",
  "companySize": "50-200",
  "website": "https://abc.com",
  "leadSource": "WEBSITE",
  "estimatedValue": 50000000,
  "probability": 20,
  "expectedCloseDate": "2025-03-15",
  "assignedTo": 123,
  "notes": "Interested in our Enterprise plan",
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
  "message": "Lead created successfully",
  "data": {
    "id": 456,
    "name": "Nguyễn Văn A",
    "email": "nguyenvana@example.com",
    "phone": "+84901234567",
    "company": "ABC Company",
    "leadStatus": "NEW",
    "leadSource": "WEBSITE",
    "estimatedValue": 50000000,
    "probability": 0,
    "assignedTo": 123,
    "createdAt": "2025-12-10T10:30:00Z",
    "createdBy": 789
  }
}
```

### Response (Validation Error - 400 Bad Request)

```json
{
  "code": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Invalid email format"
    },
    {
      "field": "probability",
      "message": "Probability must be between 0 and 100"
    }
  ]
}
```

---

## 12. UI Mockup Reference

### Form Fields Layout

```
┌─────────────────────────────────────────────────────────────────┐
│                      CREATE NEW LEAD                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Contact Information                                            │
│  ┌─────────────────────────┐  ┌─────────────────────────┐      │
│  │ Name *                  │  │ Email                   │      │
│  └─────────────────────────┘  └─────────────────────────┘      │
│  ┌─────────────────────────┐  ┌─────────────────────────┐      │
│  │ Phone                   │  │ Job Title               │      │
│  └─────────────────────────┘  └─────────────────────────┘      │
│                                                                 │
│  Company Information                                            │
│  ┌─────────────────────────┐  ┌─────────────────────────┐      │
│  │ Company Name            │  │ Industry ▼              │      │
│  └─────────────────────────┘  └─────────────────────────┘      │
│  ┌─────────────────────────┐  ┌─────────────────────────┐      │
│  │ Company Size ▼          │  │ Website                 │      │
│  └─────────────────────────┘  └─────────────────────────┘      │
│                                                                 │
│  Lead Details                                                   │
│  ┌─────────────────────────┐  ┌─────────────────────────┐      │
│  │ Lead Source * ▼         │  │ Assigned To ▼           │      │
│  └─────────────────────────┘  └─────────────────────────┘      │
│  ┌─────────────────────────┐  ┌─────────────────────────┐      │
│  │ Estimated Value         │  │ Expected Close Date 📅  │      │
│  └─────────────────────────┘  └─────────────────────────┘      │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Notes                                                    │   │
│  │                                                          │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│                            ┌──────────┐  ┌──────────┐          │
│                            │  Cancel  │  │  Create  │          │
│                            └──────────┘  └──────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

---

## 13. Test Cases

| TC ID | Scenario | Input | Expected Result |
|-------|----------|-------|-----------------|
| TC-001 | Tạo Lead với thông tin tối thiểu | name = "Test Lead" | Lead được tạo với status = NEW |
| TC-002 | Tạo Lead với đầy đủ thông tin | All fields filled | Lead được tạo đầy đủ |
| TC-003 | Tạo Lead thiếu name | name = null | Validation error |
| TC-004 | Tạo Lead với email invalid | email = "invalid" | Validation error |
| TC-005 | Tạo Lead với probability > 100 | probability = 150 | Validation error |
| TC-006 | Tạo Lead với estimatedValue < 0 | estimatedValue = -1000 | Validation error |
| TC-007 | Tạo Lead không có quyền | No CREATE_LEAD permission | 403 Forbidden |

---

## 14. Related Use Cases

| Use Case ID | Name | Relationship |
|-------------|------|--------------|
| UC-LEAD-002 | Cập nhật Lead | Extends |
| UC-LEAD-003 | Qualify Lead | Sequence |
| UC-LEAD-004 | Chuyển đổi Lead | Sequence |
| UC-LEAD-005 | Phân công Lead | Related |

---

## 15. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
