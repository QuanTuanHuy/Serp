# UC-LEAD-007: Xem chi tiết Lead (View Lead Detail)

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-LEAD-007 |
| **Use Case Name** | Xem chi tiết Lead (View Lead Detail) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép người dùng xem thông tin chi tiết của một Lead, bao gồm thông tin liên hệ, thông tin công ty, lịch sử activities, và timeline.

---

## 3. User Stories

### US-LEAD-007-01
**As a** Sales Representative  
**I want to** view complete lead information  
**So that** I can prepare for customer interactions

### US-LEAD-007-02
**As a** Sales Manager  
**I want to** view lead activity history  
**So that** I can track engagement progress

---

## 4. Preconditions

| ID | Điều kiện |
|----|-----------|
| PRE-01 | Lead tồn tại trong hệ thống |
| PRE-02 | Người dùng có quyền VIEW_LEAD |

---

## 5. Information Displayed

### Contact Information
- Name, Email, Phone
- Job Title
- Social profiles (LinkedIn, Twitter)

### Company Information
- Company Name, Industry
- Company Size, Website
- Address (Street, City, State, Country, Postal Code)

### Lead Details
- Lead Status, Lead Source
- Estimated Value, Probability
- Expected Close Date
- Assigned To
- Notes

### Related Data
- Activity History (Calls, Meetings, Emails, Tasks)
- Timeline of status changes
- Converted Opportunity/Customer (if converted)

---

## 6. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Click vào Lead từ danh sách |
| 2 | - | System | Load thông tin Lead từ database |
| 3 | - | System | Load related activities |
| 4 | - | System | Hiển thị trang Lead Detail |
| 5 | User | - | Xem thông tin chi tiết |

---

## 7. API Specification

### Endpoint

```
GET /crm/api/v1/leads/{leadId}
```

### Response (Success - 200 OK)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 456,
    "name": "Nguyễn Văn A",
    "email": "nguyenvana@example.com",
    "phone": "+84901234567",
    "company": "ABC Company",
    "jobTitle": "CEO",
    "industry": "Technology",
    "companySize": "50-200",
    "website": "https://abc.com",
    "leadSource": "WEBSITE",
    "leadStatus": "QUALIFIED",
    "estimatedValue": 75000000,
    "probability": 40,
    "expectedCloseDate": "2026-03-15",
    "assignedTo": {
      "id": 123,
      "name": "Trần Văn B"
    },
    "address": {
      "street": "123 Nguyen Hue",
      "city": "Ho Chi Minh",
      "state": "Ho Chi Minh",
      "country": "Vietnam",
      "postalCode": "70000"
    },
    "notes": "High potential client",
    "convertedOpportunityId": null,
    "convertedCustomerId": null,
    "createdAt": "2025-12-01T10:00:00Z",
    "createdBy": 789,
    "updatedAt": "2025-12-10T14:30:00Z",
    "updatedBy": 789
  }
}
```

---

## 8. Exception Flows

### EF-01: Lead Not Found

| Condition | Response |
|-----------|----------|
| Lead ID không tồn tại | HTTP 404 Not Found |

---

## 9. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | View Lead tồn tại | Hiển thị đầy đủ thông tin |
| TC-002 | View Lead không tồn tại | 404 Not Found |
| TC-003 | View Lead converted | Hiển thị link to Opportunity/Customer |

---

## 10. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
