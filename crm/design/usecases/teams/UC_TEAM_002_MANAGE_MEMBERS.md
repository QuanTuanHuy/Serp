# UC-TEAM-002: Quản lý Team Members

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-TEAM-002 |
| **Use Case Name** | Quản lý Team Members (Manage Team Members) |
| **Actor** | Sales Manager, Team Leader |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép quản lý thành viên trong team bao gồm thêm, xóa, và cập nhật trạng thái thành viên.

---

## 3. User Stories

### US-TEAM-002-01
**As a** Team Leader  
**I want to** add members to my team  
**So that** I can build my sales team

### US-TEAM-002-02
**As a** Sales Manager  
**I want to** remove members from teams  
**So that** I can reorganize teams as needed

---

## 4. Team Member Status

| Status | Description |
|--------|-------------|
| ACTIVE | Thành viên đang hoạt động |
| INACTIVE | Thành viên tạm ngưng |

---

## 5. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | User phải tồn tại trong hệ thống | Validation |
| BR-002 | User không thể là member của nhiều team cùng lúc | Constraint |
| BR-003 | Team Leader tự động là member của team | Behavior |
| BR-004 | Không thể xóa Team Leader khỏi team | Constraint |

---

## 6. Main Flows

### 6.1 Add Member to Team

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Mở Team Detail |
| 2 | User | - | Click "Add Member" |
| 3 | - | System | Hiển thị danh sách users có thể add |
| 4 | User | - | Chọn user cần add |
| 5 | User | - | Click "Add" |
| 6 | - | System | Kiểm tra user chưa thuộc team khác |
| 7 | - | System | Tạo TeamMember với status ACTIVE |
| 8 | - | System | Trả về kết quả |

### 6.2 Remove Member from Team

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Mở Team Detail |
| 2 | User | - | Click "Remove" trên member |
| 3 | - | System | Kiểm tra member không phải leader |
| 4 | - | System | Xóa TeamMember record |
| 5 | - | System | Trả về kết quả |

---

## 7. API Specifications

### Add Member
```
POST /crm/api/v1/teams/{teamId}/members
```

#### Request Body
```json
{
  "userId": 456
}
```

### Remove Member
```
DELETE /crm/api/v1/teams/{teamId}/members/{memberId}
```

### Get Team Members
```
GET /crm/api/v1/teams/{teamId}/members
```

### Response (Get Members - 200 OK)
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "userId": 123,
      "userName": "Trần Văn B",
      "role": "LEADER",
      "status": "ACTIVE",
      "joinedAt": "2025-12-01T00:00:00Z"
    },
    {
      "id": 2,
      "userId": 456,
      "userName": "Lê Thị C",
      "role": "MEMBER",
      "status": "ACTIVE",
      "joinedAt": "2025-12-05T00:00:00Z"
    }
  ]
}
```

---

## 8. Exception Flows

### EF-01: User Already in Another Team
```json
{
  "code": 400,
  "message": "User is already a member of another team"
}
```

### EF-02: Cannot Remove Leader
```json
{
  "code": 400,
  "message": "Cannot remove team leader from team. Reassign leader first."
}
```

---

## 9. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Add member to team | Success |
| TC-002 | Add member đã thuộc team khác | Error |
| TC-003 | Remove member | Success |
| TC-004 | Remove team leader | Error |
| TC-005 | Add invalid user | Error - user not found |

---

## 10. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
