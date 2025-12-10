# CRM System - Use Case Documentation Overview

## Mục lục

1. [Tổng quan hệ thống](#tổng-quan-hệ-thống)
2. [Actors](#actors)
3. [Module chính](#module-chính)
4. [Tài liệu Use Case chi tiết](#tài-liệu-use-case-chi-tiết)

---

## Tổng quan hệ thống

Hệ thống CRM (Customer Relationship Management) của SERP là một module quản lý quan hệ khách hàng hiện đại, được thiết kế theo kiến trúc microservices. Hệ thống hỗ trợ quản lý toàn bộ vòng đời khách hàng từ Lead (tiềm năng) → Opportunity (cơ hội) → Customer (khách hàng).

### Các tính năng chính:
- **Lead Management**: Quản lý khách hàng tiềm năng, nuôi dưỡng và chuyển đổi
- **Customer Management**: Quản lý thông tin khách hàng, liên hệ, địa chỉ
- **Opportunity Management**: Quản lý cơ hội bán hàng, theo dõi pipeline
- **Contact Management**: Quản lý danh bạ liên hệ khách hàng
- **Activity Management**: Quản lý hoạt động (cuộc gọi, họp, email, task)
- **Team Management**: Quản lý đội ngũ bán hàng

---

## Actors

### 1. Sales Representative (Nhân viên bán hàng)
- Người trực tiếp tương tác với khách hàng
- Tạo và quản lý leads, opportunities
- Ghi nhận hoạt động tương tác
- Chuyển đổi lead thành opportunity/customer

### 2. Sales Manager (Quản lý bán hàng)
- Giám sát hiệu suất đội ngũ bán hàng
- Phân công leads/opportunities cho nhân viên
- Xem báo cáo và analytics
- Quản lý team và members

### 3. Marketing Manager (Quản lý marketing)
- Tạo và import leads từ các chiến dịch
- Theo dõi nguồn gốc leads
- Đánh giá hiệu quả chiến dịch marketing

### 4. Customer Service (Dịch vụ khách hàng)
- Xem thông tin khách hàng và lịch sử tương tác
- Ghi nhận các hoạt động support
- Cập nhật thông tin liên hệ

### 5. System Administrator (Quản trị hệ thống)
- Cấu hình hệ thống
- Quản lý người dùng và phân quyền
- Thiết lập workflow và automation

---

## Module chính

### 1. Lead Module
| Chức năng | Mô tả |
|-----------|-------|
| Tạo Lead | Tạo mới khách hàng tiềm năng |
| Cập nhật Lead | Chỉnh sửa thông tin lead |
| Qualify Lead | Đánh giá và qualify lead |
| Convert Lead | Chuyển đổi lead thành opportunity/customer |
| Assign Lead | Phân công lead cho nhân viên |
| Search/Filter Lead | Tìm kiếm và lọc danh sách lead |

### 2. Customer Module
| Chức năng | Mô tả |
|-----------|-------|
| Tạo Customer | Tạo mới khách hàng |
| Cập nhật Customer | Chỉnh sửa thông tin khách hàng |
| Quản lý Contacts | Quản lý danh bạ liên hệ của khách hàng |
| Theo dõi Revenue | Theo dõi doanh thu từ khách hàng |
| Active/Deactive | Kích hoạt/vô hiệu hóa khách hàng |

### 3. Opportunity Module
| Chức năng | Mô tả |
|-----------|-------|
| Tạo Opportunity | Tạo mới cơ hội bán hàng |
| Cập nhật Stage | Cập nhật giai đoạn pipeline |
| Close Won/Lost | Đóng cơ hội thắng/thua |
| Assign Opportunity | Phân công cơ hội |
| Forecast | Dự báo doanh thu |

### 4. Activity Module
| Chức năng | Mô tả |
|-----------|-------|
| Log Activity | Ghi nhận hoạt động |
| Schedule Meeting | Lên lịch họp |
| Schedule Call | Lên lịch cuộc gọi |
| Create Task | Tạo công việc |
| Complete Activity | Đánh dấu hoàn thành |

### 5. Team Module
| Chức năng | Mô tả |
|-----------|-------|
| Tạo Team | Tạo mới đội ngũ |
| Quản lý Members | Thêm/xóa thành viên |
| Assign Leader | Gán trưởng nhóm |

---

## Tài liệu Use Case chi tiết

### Lead Management
- [UC-LEAD-001: Tạo Lead mới](./leads/UC_LEAD_001_CREATE_LEAD.md)
- [UC-LEAD-002: Cập nhật Lead](./leads/UC_LEAD_002_UPDATE_LEAD.md)
- [UC-LEAD-003: Qualify Lead](./leads/UC_LEAD_003_QUALIFY_LEAD.md)
- [UC-LEAD-004: Chuyển đổi Lead](./leads/UC_LEAD_004_CONVERT_LEAD.md)
- [UC-LEAD-005: Phân công Lead](./leads/UC_LEAD_005_ASSIGN_LEAD.md)
- [UC-LEAD-006: Tìm kiếm Lead](./leads/UC_LEAD_006_SEARCH_LEAD.md)
- [UC-LEAD-007: Xem chi tiết Lead](./leads/UC_LEAD_007_VIEW_LEAD.md)

### Customer Management
- [UC-CUST-001: Tạo Customer mới](./customers/UC_CUST_001_CREATE_CUSTOMER.md)
- [UC-CUST-002: Cập nhật Customer](./customers/UC_CUST_002_UPDATE_CUSTOMER.md)
- [UC-CUST-003: Quản lý Contacts](./customers/UC_CUST_003_MANAGE_CONTACTS.md)
- [UC-CUST-004: Kích hoạt/Vô hiệu hóa Customer](./customers/UC_CUST_004_ACTIVATE_DEACTIVATE.md)
- [UC-CUST-005: Cập nhật Credit Limit](./customers/UC_CUST_005_UPDATE_CREDIT_LIMIT.md)
- [UC-CUST-006: Tìm kiếm Customer](./customers/UC_CUST_006_SEARCH_CUSTOMER.md)
- [UC-CUST-007: Xem chi tiết Customer](./customers/UC_CUST_007_VIEW_CUSTOMER.md)

### Opportunity Management
- [UC-OPP-001: Tạo Opportunity mới](./opportunities/UC_OPP_001_CREATE_OPPORTUNITY.md)
- [UC-OPP-002: Cập nhật Opportunity](./opportunities/UC_OPP_002_UPDATE_OPPORTUNITY.md)
- [UC-OPP-003: Cập nhật Stage](./opportunities/UC_OPP_003_UPDATE_STAGE.md)
- [UC-OPP-004: Đóng Opportunity Won](./opportunities/UC_OPP_004_CLOSE_WON.md)
- [UC-OPP-005: Đóng Opportunity Lost](./opportunities/UC_OPP_005_CLOSE_LOST.md)
- [UC-OPP-006: Phân công Opportunity](./opportunities/UC_OPP_006_ASSIGN_OPPORTUNITY.md)
- [UC-OPP-007: Xem Pipeline](./opportunities/UC_OPP_007_VIEW_PIPELINE.md)

### Activity Management
- [UC-ACT-001: Ghi nhận Activity](./activities/UC_ACT_001_LOG_ACTIVITY.md)
- [UC-ACT-002: Lên lịch Meeting](./activities/UC_ACT_002_SCHEDULE_MEETING.md)
- [UC-ACT-003: Lên lịch Call](./activities/UC_ACT_003_SCHEDULE_CALL.md)
- [UC-ACT-004: Tạo Task](./activities/UC_ACT_004_CREATE_TASK.md)
- [UC-ACT-005: Hoàn thành Activity](./activities/UC_ACT_005_COMPLETE_ACTIVITY.md)
- [UC-ACT-006: Hủy Activity](./activities/UC_ACT_006_CANCEL_ACTIVITY.md)

### Contact Management
- [UC-CONT-001: Tạo Contact mới](./contacts/UC_CONT_001_CREATE_CONTACT.md)
- [UC-CONT-002: Cập nhật Contact](./contacts/UC_CONT_002_UPDATE_CONTACT.md)
- [UC-CONT-003: Đặt Primary Contact](./contacts/UC_CONT_003_SET_PRIMARY.md)

### Team Management
- [UC-TEAM-001: Tạo Team mới](./teams/UC_TEAM_001_CREATE_TEAM.md)
- [UC-TEAM-002: Quản lý Team Members](./teams/UC_TEAM_002_MANAGE_MEMBERS.md)

---

## Diagram tổng quan

```
                    ┌─────────────────────────────────────────────────────────────┐
                    │                        CRM SYSTEM                           │
                    └─────────────────────────────────────────────────────────────┘
                                                │
        ┌───────────────────────────────────────┼───────────────────────────────────────┐
        │                                       │                                       │
        ▼                                       ▼                                       ▼
┌───────────────┐                      ┌───────────────┐                      ┌───────────────┐
│     LEADS     │─────Convert──────────▶│  CUSTOMERS   │◀─────────────────────│   CONTACTS    │
│               │                      │               │                      │               │
│ NEW           │                      │ Account Info │                      │ Primary       │
│ CONTACTED     │                      │ Credit Limit │                      │ Secondary     │
│ NURTURING     │                      │ Revenue      │                      │               │
│ QUALIFIED     │                      │               │                      │               │
│ DISQUALIFIED  │                      │               │                      │               │
│ CONVERTED     │                      │               │                      │               │
└───────┬───────┘                      └───────┬───────┘                      └───────────────┘
        │                                      │
        │ Convert                              │
        ▼                                      ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────┐
│                                      OPPORTUNITIES                                            │
│                                                                                               │
│   PROSPECTING ──▶ QUALIFICATION ──▶ PROPOSAL ──▶ NEGOTIATION ──▶ CLOSED_WON/CLOSED_LOST      │
│       10%            25%              50%           75%              100%/0%                   │
│                                                                                               │
└───────────────────────────────────────────────────────────────────────────────────────────────┘
                                              │
                                              ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────┐
│                                        ACTIVITIES                                             │
│                                                                                               │
│                    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐                  │
│                    │  CALL   │    │ MEETING │    │  EMAIL  │    │  TASK   │                  │
│                    └─────────┘    └─────────┘    └─────────┘    └─────────┘                  │
│                                                                                               │
│                         Status: PLANNED ──▶ COMPLETED / CANCELLED                             │
│                                                                                               │
└───────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
