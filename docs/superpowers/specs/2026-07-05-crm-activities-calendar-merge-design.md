# Design Spec: CRM Activities and Calendar View Consolidation

* **Author:** Antigravity (AI Assistant)
* **Date:** 2026-07-05
* **Status:** Draft / Pending Review

---

## 1. Goal Description

Hiện tại, phân hệ CRM trong dự án SERP có hai menu/trang riêng biệt:
1. **Activities**: Hiển thị danh sách các hoạt động (Call, Email, Meeting, Task) dưới dạng bảng, hỗ trợ phân trang và thao tác hàng loạt (Bulk Actions).
2. **Calendar**: Hiển thị các hoạt động trên giao diện lịch biểu tháng/tuần sử dụng thư viện bên thứ ba `react-big-calendar`. Trực quan nhưng cồng kềnh và thiếu đồng bộ bộ lọc với trang danh sách.

Mục tiêu của thiết kế này là **hợp nhất hai trang trên thành một trang duy nhất** (menu "Hoạt động" / "Activities"), hỗ trợ chuyển đổi linh hoạt (Toggle View Mode) giữa dạng danh sách (List) và dạng lưới lịch tự dựng (Calendar Grid) lấy cảm hứng từ [PMProjectCalendarGrid.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/projects/calendar/PMProjectCalendarGrid.tsx). Đồng thời thiết kế lại biểu mẫu Tạo/Sửa hoạt động thành dạng Form động chuyên nghiệp.

*Lưu ý:* Phân hệ **Meeting Requests** vẫn giữ nguyên là một menu riêng biệt và nằm ngoài phạm vi chỉnh sửa của phase này.

---

## 2. Proposed Changes

Đề xuất sửa đổi và tạo mới các file thành phần thuộc module `crm` trong `serp_web`:

```
serp_web/src/modules/crm/
├── components/
│   ├── calendar/
│   │   ├── CRMCalendarGrid.tsx         [NEW] Lưới lịch biểu tự dựng bằng CSS Grid
│   │   ├── CRMCalendarChips.tsx        [NEW] Các thẻ hoạt động nhỏ hiển thị trong ô ngày
│   │   └── crmCalendar.utils.ts        [NEW] Các hàm tiện ích tính toán khoảng ngày hiển thị
│   └── forms/
│       └── ActivityForm.tsx            [MODIFY] Cập nhật giao diện động theo loại
└── pages/
    └── activities/
        └── ActivityListPage.tsx        [MODIFY] Hợp nhất giao diện Danh sách và Lưới lịch
```

### 2.1. Cấu trúc trang hợp nhất [ActivityListPage.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/pages/activities/ActivityListPage.tsx)

* **Bộ lọc dùng chung (Shared Filters State)**:
  * Trang quản lý một bộ state bộ lọc tập trung bao gồm: `searchQuery`, `typeFilter`, `statusFilter`, `priorityFilter`, và khoảng ngày `dueDateFrom` - `dueDateTo`.
  * Bộ lọc này hoạt động đồng thời trên cả hai chế độ xem (List/Calendar). Khi chuyển đổi qua lại, các lựa chọn lọc của người dùng không bị mất.
* **Chuyển đổi Chế độ xem (View Mode Toggle)**:
  * Thêm nút chuyển đổi dạng Segmented Control ở góc phải trên thanh tiêu đề: `[List View]` / `[Calendar View]`.
  * Khi `viewMode === 'calendar'`, hệ thống tự động đồng bộ khoảng ngày lọc (`dueDateFrom` / `dueDateTo`) khớp với ngày bắt đầu và ngày kết thúc của Tháng lịch đang hiển thị để nạp dữ liệu đầy đủ.

### 2.2. Lưới lịch biểu mới [CRMCalendarGrid.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/calendar/CRMCalendarGrid.tsx) [NEW]

* Thay thế hoàn toàn `react-big-calendar`.
* Tự xây dựng lưới ngày bằng Tailwind CSS Grid: `grid-cols-7` (từ Thứ 2 đến Chủ nhật).
* Ô ngày (`CRMCalendarDayCell`):
  * Hiển thị số ngày ở góc.
  * Hiển thị danh sách các thẻ hoạt động (`CRMCalendarChip`) diễn ra trong ngày đó.
  * Rê chuột vào ô ngày sẽ xuất hiện nút cộng nhanh `[+]` để tạo mới hoạt động.
* Hỗ trợ kéo thả đổi ngày bằng `@dnd-kit/core`:
  * Mỗi `CRMCalendarChip` là một `Draggable` component.
  * Mỗi ô ngày `CRMCalendarDayCell` là một `Droppable` component.
  * Thao tác kéo thả thành công sẽ gọi API `rescheduleActivity(activityId, newDate)` trực tiếp và hiện Toast thông báo thành công kèm nút **Undo (Hoàn tác)**.

### 2.3. Thẻ hoạt động biểu diễn [CRMCalendarChips.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/calendar/CRMCalendarChips.tsx) [NEW]

* Mỗi thẻ đại diện cho một Activity trên Lịch biểu.
* Cấu trúc: Icon loại hoạt động + Thời gian bắt đầu + Tiêu đề rút gọn.
* Màu sắc được quy định đồng bộ bằng HSL biến thể Light/Dark:
  * **CALL**: Nền xanh dương dịu (`text-blue-700 bg-blue-50 dark:bg-blue-950/40 dark:text-blue-300`).
  * **MEETING**: Nền xanh lá dịu (`text-emerald-700 bg-emerald-50 dark:bg-emerald-950/40 dark:text-emerald-300`).
  * **EMAIL**: Nền tím nhạt (`text-purple-700 bg-purple-50 dark:bg-purple-950/40 dark:text-purple-300`).
  * **TASK**: Nền cam đất ấm (`text-amber-700 bg-amber-50 dark:bg-amber-950/40 dark:text-amber-300`).

### 2.4. Biểu mẫu tạo mới/chỉnh sửa nâng cấp [ActivityForm.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/forms/ActivityForm.tsx)

Đóng gói lại thiết kế form dưới dạng panel trượt Slide-over Drawer hoặc Modal rộng, sử dụng logic trường dữ liệu động:

1. **Segmented Control trên cùng**: Chuyển đổi nhanh 4 loại `Call`, `Email`, `Meeting`, `Task`.
2. **Trường liên kết đối tượng (Related To)**:
   * Chuyển đổi từ `<Select>` tĩnh sang bộ đôi Combobox Autocomplete. Người dùng chọn loại đối tượng (`Lead`/`Customer`/`Opportunity`), sau đó gõ để tìm kiếm trực tiếp qua API.
3. **Hiển thị trường động theo Phân loại**:
   * **Meeting**: Hiện Địa điểm (`location`), thời lượng (`durationMinutes`) và checkbox Tự tạo Google Meet.
   * **Task**: Hiện Hạn chót (`dueDate`), Độ ưu tiên (`priority`) và Thanh kéo tiến độ (`progressPercent`).
   * **Call**: Hiện Số điện thoại và kết quả cuộc gọi.
   * **Email**: Hiện email người nhận và Chọn mẫu email template.
4. **Khu vực Hoàn thành (Completion Section)**:
   * Chỉ hiển thị khi trạng thái được chuyển sang `COMPLETED`.
   * Chứa trường Kết quả hoạt động (`outcome`), Ghi chú kết quả (`closingNotes`), và vùng tải lên Tệp đính kèm (`attachments`).

---

## 3. Verification Plan

Sau khi hoàn thành code, các bước kiểm tra sau sẽ được thực thi để đảm bảo chất lượng:

### 3.1. Automated Verification
Chạy lệnh kiểm tra tính toàn vẹn của mã nguồn TypeScript, ESLint và định dạng Prettier từ thư mục `serp_web/`:
```bash
npm run lint
npm run type-check
npm run format:check
```

### 3.2. Manual Verification
* **Đồng bộ bộ lọc**: Chọn một bộ lọc (ví dụ: Loại = Meeting), chuyển qua lại giữa chế độ List và Calendar để đảm bảo dữ liệu trên cả 2 view đều phản ánh đúng bộ lọc đó.
* **Kéo thả xếp lại lịch**: Kéo thả một thẻ hoạt động từ ngày 6 sang ngày 8 trên Calendar Grid. Xác nhận Toast hiển thị thành công. Click nút "Undo" trên Toast và xác nhận hoạt động quay lại ngày 6.
* **Kiểm tra Form Động**: Mở form tạo hoạt động, click chuyển đổi qua lại giữa Task và Meeting để xem các trường thay đổi tương ứng. Chọn trạng thái Completed và xác nhận phần nhập kết quả trượt hiển thị ra.
