# CRM Activities Form Redesign Design Specification

## Overview

**Goal:** Redesign the CRM Create/Edit Activity forms to replace the current cramped, single-column dialog and edit layouts with a modern, clean, two-column workspace. The redesign optimizes visual hierarchy, integrates segmented controls, and dynamically shows type-specific fields and collapsible completion details to ensure a premium user experience.

**Scope:**
1. Unified Create Dialog: [QuickAddActivityDialog.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/dialogs/QuickAddActivityDialog.tsx)
2. Unified Edit Form: [ActivityForm.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/forms/ActivityForm.tsx)

---

## Bố cục & Giao diện (Layout & Styling)

### 1. Không gian làm việc 2 cột (Two-Column Workspace Grid)
Cả Dialog tạo nhanh và Form chỉnh sửa chi tiết sẽ chuyển sang bố cục dạng lưới 2 cột đối xứng trên desktop:
- Kích thước Dialog tăng lên `max-w-4xl` (khoảng 896px) để hiển thị song song 2 cột mà không bị co cụm.
- Trên màn hình di động (mobile/tablet), bố cục tự động co về 1 cột đơn (`grid-cols-1 md:grid-cols-2`).

### 2. Cột bên trái (General Information)
Tập trung vào nội dung công việc và thông tin đối tượng:
- **Loại hoạt động (Activity Type Segmented Selector):** 
  - Nền mờ (`bg-muted rounded-lg p-1 w-full flex gap-1`).
  - Các button có icon (`Phone`, `Mail`, `Video`, `CheckSquare`) tương ứng. Button được chọn sẽ có nền trắng (`bg-background`), chữ màu đậm, viền mờ và đổ bóng nhẹ.
- **Tiêu đề (Subject Input):**
  - Ô nhập văn bản rộng tối đa, nổi bật.
- **Đối tượng liên kết (Related To Section):**
  - **Dòng trên (Segmented buttons):** `Customer` | `Lead` | `Opportunity` nằm trong thanh chuyển hướng mượt mà.
  - **Dòng dưới (Searchable combobox):** Tìm kiếm và lựa chọn bản ghi cụ thể của đối tượng đã chọn ở dòng trên.
- **Địa điểm họp (Meeting Location):**
  - *Chỉ hiển thị khi hoạt động là MEETING*. Ô input tích hợp kèm icon để nhập link họp online hoặc địa điểm phòng họp.
- **Mô tả (Description):**
  - Textarea rộng rãi để nhập chương trình nghị sự hoặc nội dung chính.

### 3. Cột bên phải (Scheduling & Assignee)
Tập trung vào điều phối thời gian và nhân sự:
- **Trạng thái (Status):**
  - Dropdown hiển thị trạng thái đi kèm Badge màu tương ứng.
- **Lưới Lập lịch (Scheduling Grid):**
  - Bố cục lưới 3 cột: Ngày (DatePicker), Giờ (Time Input), Thời lượng (Duration Select với các option định sẵn: 15p, 30p, 1h, 2h).
- **Mức độ ưu tiên (Priority):**
  - Hộp chọn đi kèm Bullet điểm màu nhận diện mức độ khẩn cấp (Low = Green, Medium = Blue, High = Orange, Urgent = Red).
- **Tiến độ % (Progress %):**
  - *Chỉ hiển thị khi hoạt động là TASK*. Thanh trượt Slider trực quan cập nhật nhanh tiến trình công việc từ 0% đến 100%.
- **Người phụ trách (Assigned To):**
  - Dropdown tìm kiếm người dùng (CRMUserSelect) có tích hợp Avatar.

### 4. Bảng Hoàn thành (Completion Panel - Dưới cùng)
- *Chỉ hiển thị khi Status được đổi thành COMPLETED*.
- Mở rộng ở chân trang/Dialog với hiệu ứng transition trượt và mờ dần (`animate-fade-in transition-all`).
- Khung màu nền mờ nhạt (`bg-muted/30 border border-dashed rounded-lg p-4 space-y-4`).
- Chứa dropdown chọn Kết quả hoạt động (`Outcome`) và ô nhập Ghi chú hoàn thành (`Completion Notes/Summary`).

---

## Chi tiết linh hồn Linh kiện (Component Breakdown)

### 1. [QuickAddActivityDialog.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/dialogs/QuickAddActivityDialog.tsx)
- Thay đổi `DialogContent className="max-w-lg"` thành `DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto"`.
- Bọc phần thân form bằng:
  ```tsx
  <div className="grid grid-cols-1 md:grid-cols-2 gap-6 py-4">
    {/* Cột trái */}
    <div className="space-y-4">...</div>
    {/* Cột phải */}
    <div className="space-y-4">...</div>
  </div>
  ```

### 2. [ActivityForm.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/forms/ActivityForm.tsx)
- Thay đổi `Card className="w-full max-w-4xl"` thành `Card className="w-full max-w-5xl"`.
- Bố cục tương đương với Dialog tạo nhanh nhằm đồng bộ hóa hoàn hảo trải nghiệm người dùng trên tất cả các trang.

---

## Kịch bản Kiểm thử & Xác thực (Verification Plan)

### 1. Kiểm tra hiển thị
- Bấm "Create Activity" trên Calendar hoặc Activity List. Xác minh Dialog hiển thị ở dạng 2 cột rộng rãi.
- Bấm chọn các tab `Customer`, `Lead`, `Opportunity` trong Related To. Xác minh danh sách dropdown ô bên dưới thay đổi theo.
- Đổi loại hoạt động giữa `Call` và `Meeting`. Xác minh trường `Location` tự động ẩn/hiện hợp lý.

### 2. Kiểm tra nghiệp vụ hoàn thành
- Chuyển trạng thái hoạt động thành `Completed`. Xác minh Completion Panel tự động mở rộng ở dưới cùng.
- Chọn Outcome và nhập Notes, sau đó Lưu. Kiểm tra dữ liệu gửi lên API có đầy đủ `outcome` và `notes`.

### 3. Kiểm thử tự động & Build
- Kiểm tra tính tương thích TypeScript: `npm run type-check`.
- Kiểm tra định dạng code: `npm run lint`.
