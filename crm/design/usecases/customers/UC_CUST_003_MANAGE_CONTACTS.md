# UC-CUST-003: Quản lý Contacts

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-CUST-003 |
| **Use Case Name** | Quản lý Contacts (Manage Customer Contacts) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép quản lý danh sách người liên hệ (contacts) của một Customer, bao gồm thêm, sửa, xóa contacts và đặt primary contact.

---

## 3. User Stories

### US-CUST-003-01
**As a** Sales Representative  
**I want to** add multiple contacts to a customer  
**So that** I can track all key people in the organization

### US-CUST-003-02
**As a** Sales Representative  
**I want to** set a primary contact  
**So that** I know who to contact first

### US-CUST-003-03
**As a** Sales Representative  
**I want to** track contact information and social profiles  
**So that** I can communicate through multiple channels

---

## 4. Preconditions

| ID | Điều kiện |
|----|-----------|
| PRE-01 | Customer tồn tại trong hệ thống |
| PRE-02 | Người dùng có quyền MANAGE_CONTACTS |

---

## 5. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Một Customer có thể có nhiều Contacts | Cardinality |
| BR-002 | Chỉ có 1 Primary Contact tại một thời điểm | Constraint |
| BR-003 | Khi set Primary mới, Primary cũ tự động bị reset | Behavior |
| BR-004 | Contact mới mặc định activeStatus = ACTIVE | Default |
| BR-005 | Contact mới mặc định isPrimary = false | Default |
| BR-006 | Contact có contactType: PRIMARY, SECONDARY, BILLING, TECHNICAL | Validation |

---

## 6. Contact Types

| Type | Description |
|------|-------------|
| PRIMARY | Liên hệ chính, quyết định |
| SECONDARY | Liên hệ phụ |
| BILLING | Liên hệ về hóa đơn, thanh toán |
| TECHNICAL | Liên hệ kỹ thuật |

---

## 7. Main Flows

### 7.1 Add New Contact

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Mở Customer Detail > Contacts tab |
| 2 | User | - | Click "Add Contact" |
| 3 | - | System | Hiển thị form thêm Contact |
| 4 | User | - | Nhập thông tin Contact |
| 5 | User | - | Click "Save" |
| 6 | - | System | Validate và set defaults |
| 7 | - | System | Lưu Contact với customerId |
| 8 | - | System | Trả về Contact đã tạo |

### 7.2 Set Primary Contact

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Click "Set as Primary" trên Contact |
| 2 | - | System | Tìm Primary Contact hiện tại |
| 3 | - | System | Reset isPrimary = false cho contact cũ |
| 4 | - | System | Set isPrimary = true cho contact mới |
| 5 | - | System | Lưu thay đổi |

### 7.3 Update Contact

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Click "Edit" trên Contact |
| 2 | - | System | Hiển thị form với data hiện tại |
| 3 | User | - | Chỉnh sửa thông tin |
| 4 | User | - | Click "Save" |
| 5 | - | System | Update và lưu Contact |

### 7.4 Deactivate Contact

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Click "Deactivate" trên Contact |
| 2 | - | System | Confirm dialog |
| 3 | User | - | Confirm |
| 4 | - | System | Set activeStatus = INACTIVE |

---

## 8. API Specifications

### Add Contact
```
POST /crm/api/v1/customers/{customerId}/contacts
```

### Request Body
```json
{
  "name": "Nguyễn Văn A",
  "email": "nguyenvana@abc.com",
  "phone": "+84901234567",
  "jobPosition": "CEO",
  "contactType": "PRIMARY",
  "linkedInUrl": "https://linkedin.com/in/nguyenvana",
  "twitterHandle": "@nguyenvana",
  "notes": "Key decision maker"
}
```

### Set Primary Contact
```
PUT /crm/api/v1/contacts/{contactId}/set-primary
```

### Update Contact
```
PUT /crm/api/v1/contacts/{contactId}
```

### Deactivate Contact
```
PUT /crm/api/v1/contacts/{contactId}/deactivate
```

### Get Customer Contacts
```
GET /crm/api/v1/customers/{customerId}/contacts
```

---

## 9. Response Examples

### Contact Created (201)
```json
{
  "code": 201,
  "message": "Contact created successfully",
  "data": {
    "id": 202,
    "name": "Nguyễn Văn A",
    "email": "nguyenvana@abc.com",
    "phone": "+84901234567",
    "jobPosition": "CEO",
    "contactType": "PRIMARY",
    "isPrimary": false,
    "activeStatus": "ACTIVE",
    "customerId": 101,
    "createdAt": "2025-12-10T10:30:00Z"
  }
}
```

### Customer Contacts List (200)
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 202,
      "name": "Nguyễn Văn A",
      "email": "nguyenvana@abc.com",
      "isPrimary": true,
      "contactType": "PRIMARY",
      "activeStatus": "ACTIVE"
    },
    {
      "id": 203,
      "name": "Trần Thị B",
      "email": "tranthib@abc.com",
      "isPrimary": false,
      "contactType": "BILLING",
      "activeStatus": "ACTIVE"
    }
  ]
}
```

---

## 10. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Add contact to customer | Contact created |
| TC-002 | Set primary contact | Old primary reset, new primary set |
| TC-003 | Update contact info | Contact updated |
| TC-004 | Deactivate contact | activeStatus = INACTIVE |
| TC-005 | Add contact to non-existent customer | 404 Error |
| TC-006 | Set primary for non-existent contact | 404 Error |

---

## 11. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
