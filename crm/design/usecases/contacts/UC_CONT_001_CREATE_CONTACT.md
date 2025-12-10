# UC-CONT-001: Tạo Contact Mới

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-CONT-001 |
| **Use Case Name** | Tạo Contact Mới (Create New Contact) |
| **Actor** | Sales Representative |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép tạo mới một Contact (người liên hệ) trong hệ thống, có thể độc lập hoặc liên kết với Customer.

---

## 3. User Stories

### US-CONT-001-01
**As a** Sales Representative  
**I want to** add contact information for key people  
**So that** I can maintain a comprehensive contact database

---

## 4. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Name là bắt buộc | Mandatory |
| BR-002 | Email phải có định dạng hợp lệ (nếu có) | Validation |
| BR-003 | activeStatus mặc định = ACTIVE | Default |
| BR-004 | isPrimary mặc định = false | Default |
| BR-005 | contactType mặc định = SECONDARY | Default |

---

## 5. Data Requirements

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Họ tên |
| email | String | No | Email |
| phone | String | No | Số điện thoại |
| jobPosition | String | No | Chức danh |
| customerId | Long | No | Link với Customer |
| contactType | Enum | No | PRIMARY, SECONDARY, BILLING, TECHNICAL |
| linkedInUrl | String | No | Profile LinkedIn |
| twitterHandle | String | No | Twitter handle |
| notes | String | No | Ghi chú |
| address | Object | No | Địa chỉ |

---

## 6. API Specification

### Endpoint
```
POST /crm/api/v1/contacts
```

### Request Body
```json
{
  "name": "Nguyễn Văn A",
  "email": "nguyenvana@abc.com",
  "phone": "+84901234567",
  "jobPosition": "CEO",
  "customerId": 101,
  "contactType": "PRIMARY",
  "linkedInUrl": "https://linkedin.com/in/nguyenvana",
  "notes": "Key decision maker"
}
```

### Response (Success - 201 Created)
```json
{
  "code": 201,
  "message": "Contact created successfully",
  "data": {
    "id": 202,
    "name": "Nguyễn Văn A",
    "email": "nguyenvana@abc.com",
    "contactType": "PRIMARY",
    "isPrimary": false,
    "activeStatus": "ACTIVE",
    "customerId": 101,
    "createdAt": "2025-12-10T10:30:00Z"
  }
}
```

---

## 7. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Create contact với thông tin tối thiểu | Success |
| TC-002 | Create contact với đầy đủ thông tin | Success |
| TC-003 | Create contact thiếu name | Error |
| TC-004 | Create contact với invalid email | Error |

---

## 8. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
