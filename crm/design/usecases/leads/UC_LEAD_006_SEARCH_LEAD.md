# UC-LEAD-006: Tìm kiếm Lead (Search Lead)

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-LEAD-006 |
| **Use Case Name** | Tìm kiếm Lead (Search Lead) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép người dùng tìm kiếm và lọc danh sách Leads theo nhiều tiêu chí khác nhau như tên, email, company, status, source, assigned user, v.v.

---

## 3. User Stories

### US-LEAD-006-01
**As a** Sales Representative  
**I want to** search leads by name or email  
**So that** I can quickly find specific leads

### US-LEAD-006-02
**As a** Sales Manager  
**I want to** filter leads by status and source  
**So that** I can analyze lead distribution

### US-LEAD-006-03
**As a** Sales Manager  
**I want to** filter leads by assigned user  
**So that** I can review team performance

---

## 4. Preconditions

| ID | Điều kiện |
|----|-----------|
| PRE-01 | Người dùng đã đăng nhập |
| PRE-02 | Người dùng có quyền VIEW_LEAD |

---

## 5. Search Criteria

| Field | Operator | Description |
|-------|----------|-------------|
| name | LIKE, EQUALS | Tìm theo tên |
| email | LIKE, EQUALS | Tìm theo email |
| phone | LIKE, EQUALS | Tìm theo SĐT |
| company | LIKE, EQUALS | Tìm theo công ty |
| leadStatus | IN | Lọc theo trạng thái |
| leadSource | IN | Lọc theo nguồn |
| assignedTo | EQUALS | Lọc theo người phụ trách |
| industry | IN | Lọc theo ngành |
| createdAt | BETWEEN | Lọc theo ngày tạo |
| estimatedValue | BETWEEN | Lọc theo giá trị |

---

## 6. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Truy cập màn hình Lead List |
| 2 | User | - | Nhập từ khóa tìm kiếm và/hoặc chọn filters |
| 3 | User | - | Click "Search" hoặc nhấn Enter |
| 4 | - | System | Validate search parameters |
| 5 | - | System | Build query với các điều kiện |
| 6 | - | System | Execute query với pagination |
| 7 | - | System | Trả về danh sách Leads matching |
| 8 | User | - | Xem kết quả tìm kiếm |

---

## 7. API Specification

### Endpoint

```
GET /crm/api/v1/leads
```

### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| keyword | String | No | Tìm kiếm theo name, email, phone, company |
| leadStatus | String | No | Comma-separated list: NEW,CONTACTED |
| leadSource | String | No | Comma-separated list: WEBSITE,REFERRAL |
| assignedTo | Long | No | User ID |
| industry | String | No | Industry name |
| minValue | Decimal | No | Minimum estimated value |
| maxValue | Decimal | No | Maximum estimated value |
| fromDate | Date | No | Created from date |
| toDate | Date | No | Created to date |
| page | Integer | No | Page number (default: 0) |
| size | Integer | No | Page size (default: 20, max: 100) |
| sortBy | String | No | Sort field (default: createdAt) |
| sortDir | String | No | ASC or DESC (default: DESC) |

### Example Request

```
GET /crm/api/v1/leads?keyword=ABC&leadStatus=NEW,CONTACTED&leadSource=WEBSITE&page=0&size=20&sortBy=createdAt&sortDir=DESC
```

### Response (Success - 200 OK)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Nguyễn Văn A",
        "email": "nguyenvana@abc.com",
        "company": "ABC Company",
        "leadStatus": "NEW",
        "leadSource": "WEBSITE",
        "estimatedValue": 50000000,
        "assignedTo": 123,
        "createdAt": "2025-12-10T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

---

## 8. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Search với keyword | Trả về leads matching keyword |
| TC-002 | Filter theo status | Trả về leads với status được chọn |
| TC-003 | Combine filters | Trả về leads thỏa mãn tất cả điều kiện |
| TC-004 | Empty result | Trả về empty list |
| TC-005 | Pagination test | Pagination hoạt động đúng |

---

## 9. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
