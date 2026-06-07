# Tài liệu Thiết kế Ma trận Phân quyền (Role & Permission Matrix) - Module School Bus

Tài liệu này định nghĩa chi tiết cấu trúc phân quyền 3 lớp (Function permission, Data permission, UI permission) cho module School Bus, đồng thời chuẩn bị kế hoạch tích hợp người dùng (Account User Integration) cho các phase tiếp theo.

---

## 1. Tổng quan Roles của Module School Bus

Hệ thống School Bus định nghĩa và sử dụng chính xác 5 Role sau đây từ hệ thống Identity / Keycloak:

*   **`SCHOOL_BUS_ADMIN`**: Quản trị viên hệ thống xe buýt. Có toàn quyền quản lý cấu hình danh mục, thiết lập hệ thống, và giám sát toàn bộ hoạt động trong phạm vi Tenant của mình.
*   **`SCHOOL_BUS_DISPATCHER`**: Điều hành viên đội xe. Chịu trách nhiệm duyệt yêu cầu đưa đón, quản lý gói đăng ký, lập kế hoạch tuyến đường, phân công tài xế/giám hộ/xe buýt, theo dõi vận hành chuyến đi, điểm danh học sinh và xem báo cáo.
*   **`SCHOOL_BUS_DRIVER`**: Tài xế xe buýt. Thực hiện vận hành các chuyến đi được phân công (bắt đầu chuyến đi, ghi nhận đến điểm dừng, hoàn thành chuyến đi).
*   **`SCHOOL_BUS_ATTENDANT`**: Giám hộ học sinh trên xe buýt. Thực hiện điểm danh học sinh (boarded, absent, no-show, drop-off) cho các chuyến đi được phân công.
*   **`SCHOOL_BUS_PARENT`**: Phụ huynh học sinh. Tạo yêu cầu đưa đón cho con, đăng ký dịch vụ, theo dõi hành trình và lịch sử điểm danh của con mình.

---

## 2. Ma trận Hiển thị Menu (Menu Visibility Matrix)

Căn cứ vào danh sách các Menu hiện tại của module School Bus trên Frontend (`serp_web`), dưới đây là quyền hiển thị của từng Role:

*   **`Visible`**: Menu hiển thị đầy đủ và có quyền thao tác theo phân quyền chức năng.
*   **`Readonly`**: Menu hiển thị nhưng chỉ cho phép xem thông tin, các nút tạo/sửa/xóa bị khóa.
*   **`Limited`**: Chỉ hiển thị dữ liệu giới hạn (ví dụ: Driver chỉ thấy Trip của mình, Parent chỉ thấy con của mình).
*   **`Hidden`**: Menu hoàn toàn bị ẩn khỏi thanh điều hướng và giao diện.

| Tên Menu | `SCHOOL_BUS_ADMIN` | `SCHOOL_BUS_DISPATCHER` | `SCHOOL_BUS_DRIVER` | `SCHOOL_BUS_ATTENDANT` | `SCHOOL_BUS_PARENT` |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Dashboard** | `Visible` | `Visible` | `Limited` (Chuyến đi hôm nay) | `Limited` (Chuyến đi hôm nay) | `Limited` (Trạng thái xe đón con) |
| **Schools** | `Visible` | `Readonly` | `Hidden` | `Hidden` | `Hidden` |
| **Students** | `Visible` | `Visible` | `Hidden` | `Hidden` | `Limited` (Chỉ con của mình) |
| **Parents** | `Visible` | `Visible` | `Hidden` | `Hidden` | `Hidden` |
| **Fleet** | `Visible` | `Visible` | `Hidden` | `Hidden` | `Hidden` |
| **Requests** | `Visible` | `Visible` | `Hidden` | `Hidden` | `Limited` (Chỉ yêu cầu của mình) |
| **Subscriptions** | `Visible` | `Visible` | `Hidden` | `Hidden` | `Limited` (Chỉ gói của mình) |
| **Dispatch** | `Visible` | `Visible` | `Hidden` | `Hidden` | `Hidden` |
| **Trips** | `Visible` | `Visible` | `Limited` (Chỉ chuyến được phân công) | `Limited` (Chỉ chuyến được phân công) | `Limited` (Readonly lịch trình con) |
| **Attendance** | `Visible` | `Visible` | `Hidden` | `Limited` (Chỉ chuyến được phân công) | `Limited` (Readonly điểm danh con) |
| **Reports** | `Visible` | `Visible` | `Hidden` | `Hidden` | `Hidden` |

---

## 3. Ma trận Quyền Chức năng (API/Function Permission Matrix)

Dưới đây là ma trận ánh xạ các nhóm chức năng thực tế của hệ thống xe buýt với các hành động cụ thể trên API:

*   **`Y`**: Được phép thao tác.
*   **`N`**: Bị cấm thao tác.
*   **`Own`**: Chỉ thao tác trên dữ liệu thuộc sở hữu/được phân công (Data Scope).

| Nhóm chức năng | Hành động | `SCHOOL_BUS_ADMIN` | `SCHOOL_BUS_DISPATCHER` | `SCHOOL_BUS_DRIVER` | `SCHOOL_BUS_ATTENDANT` | `SCHOOL_BUS_PARENT` |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| **Master Data** *(Schools, Fleet, Depots, PickupPoints)* | View | `Y` | `Y` | `N` | `N` | `N` |
| | Create/Update/Delete | `Y` | `N` | `N` | `N` | `N` |
| **Students/Parents Profiles** | View | `Y` | `Y` | `N` | `N` | `Own` (Chỉ xem hồ sơ bản thân & con) |
| | Create/Update/Delete | `Y` | `Y` | `N` | `N` | `N` |
| **Transport Requests** | View | `Y` | `Y` | `N` | `N` | `Own` |
| | Create/Update/Delete | `Y` | `Y` | `N` | `N` | `Own` (Tạo/Hủy yêu cầu đưa đón) |
| | Approve/Reject | `Y` | `Y` | `N` | `N` | `N` |
| **Subscriptions** | View | `Y` | `Y` | `N` | `N` | `Own` |
| | Create/Update/Delete | `Y` | `Y` | `N` | `N` | `Own` (Đăng ký/Yêu cầu tạm dừng) |
| | Activate/Stop/Approve | `Y` | `Y` | `N` | `N` | `N` |
| **Route Planning** | View/Create/Update | `Y` | `Y` | `N` | `N` | `N` |
| | Publish (Công bộ tuyến) | `Y` | `Y` | `N` | `N` | `N` |
| **Route Assignment** *(Phân tài xế/giám hộ/xe)* | View | `Y` | `Y` | `N` | `N` | `N` |
| | Assign/Update | `Y` | `Y` | `N` | `N` | `N` |
| **Trip Operation** *(Start/End/Stop trip)* | View | `Y` | `Y` | `Own` (Xem chuyến được gán) | `Own` (Xem chuyến được gán) | `Own` (Readonly chuyến của con) |
| | Operate (Bắt đầu/Kết thúc) | `Y` | `Y` | `Own` | `N` | `N` |
| **Attendance** *(Mark Boarded, Absent,...)* | View | `Y` | `Y` | `Own` (Readonly) | `Own` (Readonly) | `Own` (Readonly điểm danh con) |
| | Mark Attendance | `Y` | `Y` | `N` | `Own` (Ghi nhận điểm danh chuyến gán) | `N` |
| **Dashboard/Reports** | View/Export | `Y` | `Y` | `N` | `N` | `N` |
| **User/Profile Extension** | Link/Sync User | `Y` | `N` | `N` | `N` | `N` |
| **Config** | Edit configs | `Y` | `N` | `N` | `N` | `N` |

---

## 4. Thiết kế Phạm vi Dữ liệu (Data Scope Matrix) & Backend Enforcement

Việc ẩn menu ở giao diện Frontend chỉ phục vụ mục đích tăng trải nghiệm người dùng (UX) và **không thể** thay thế cho cơ chế bảo mật ở Backend. Backend bắt buộc phải kiểm tra quyền sở hữu dữ liệu dựa trên thông tin người dùng được giải mã từ JWT Token.

### Quy định Data Scope cho từng Role:

1.  **`SCHOOL_BUS_ADMIN`**:
    *   **Scope**: `tenant_id = current tenant` (Toàn bộ dữ liệu thuộc Tenant của Admin).
    *   **Backend Enforcement**: Tự động lọc tất cả các câu truy vấn cơ sở dữ liệu theo ID của Tenant hiện tại lấy từ Security Context.

2.  **`SCHOOL_BUS_DISPATCHER`**:
    *   **Scope**: `tenant_id = current tenant` (Toàn bộ dữ liệu vận hành thuộc Tenant).
    *   **Backend Enforcement**: Tương tự Admin, lọc toàn bộ dữ liệu theo `tenant_id`.

3.  **`SCHOOL_BUS_DRIVER`**:
    *   **Scope**: `tenant_id = current tenant` AND `driver_profile.user_id = current authenticated user id`.
    *   **Backend Enforcement**:
        *   Khi lấy danh sách Trips: Chỉ hiển thị các Trips được gán cho Driver này thông qua ID của tài xế (`driver_id` liên kết với user đăng nhập).
        *   Khi truy cập chi tiết Trip/Stop: Validate `trip.driver_id` phải khớp với ID tài xế đăng nhập. Nếu không khớp, trả về lỗi `403 Forbidden`.
        *   Thông tin học sinh trên tuyến chỉ hiển thị dưới dạng Readonly ở màn hình chuyến đi đang thực hiện.

4.  **`SCHOOL_BUS_ATTENDANT`**:
    *   **Scope**: `tenant_id = current tenant` AND `attendant_profile.user_id = current authenticated user id`.
    *   **Backend Enforcement**:
        *   Khi lấy danh sách Trips/Attendance: Chỉ hiển thị các Trips được phân công cho Giám hộ này (`attendant_id`).
        *   Khi thực hiện ghi nhận điểm danh (Mark Attendance): Kiểm tra học sinh đó có thuộc điểm dừng của chuyến đi đang chạy và chuyến đi đó có do Attendant này phụ trách hay không.

5.  **`SCHOOL_BUS_PARENT`**:
    *   **Scope**: `tenant_id = current tenant` AND `parent_profile.user_id = current authenticated user id`.
    *   **Backend Enforcement**:
        *   Hồ sơ Phụ huynh: Chỉ được xem/sửa hồ sơ có `user_id` trùng với user đăng nhập.
        *   Học sinh: Chỉ được xem danh sách học sinh có `parent_id` liên kết với hồ sơ phụ huynh hiện tại.
        *   Yêu cầu & Gói dịch vụ (Requests & Subscriptions): Lọc bắt buộc theo `parent_id` của phụ huynh hiện tại. Cấm truyền `parentId` tùy ý từ Client.
        *   Điểm danh/Trip: Chỉ xem được lịch sử điểm danh và trạng thái chuyến đi thời gian thực của chính con mình.

---

## 5. Thiết kế Trải nghiệm Người dùng theo Role (Role-specific UI Behavior)

### 5.1. Màn hình tạo yêu cầu của Phụ huynh (Parent Create Request UI)
*   **Hành vi**:
    *   Không hiển thị dropdown chọn Phụ huynh (`Parent`). Hệ thống tự động thiết lập phụ huynh tạo là Current Authenticated User ở Backend.
    *   Dropdown Học sinh (`Student`) chỉ tải và hiển thị danh sách con của Phụ huynh hiện tại.
    *   Khóa/Ẩn toàn bộ khả năng nhập thủ công hoặc chỉnh sửa `parentId` trên Form.
    *   Chỉ hiển thị các trường thông tin cơ bản: Chọn học sinh (con mình), Trường học, Điểm đón đề xuất, Lịch trình mong muốn.

### 5.2. Giao diện của Tài xế (Driver UI)
*   **Hành vi**:
    *   **Landing Page**: Sau khi đăng nhập thành công, tự động điều hướng thẳng vào trang danh sách chuyến đi được phân công trong ngày ("Trips assigned to me").
    *   **Giao diện vận hành**: Màn hình thiết kế dạng tối giản, nút bấm to để dễ thao tác trên thiết bị di động khi di chuyển. Chỉ có các nút hành động: *Start Trip*, *Arrive Stop*, *Depart Stop*, *Complete Trip*.
    *   **Điều hướng ẩn**: Ẩn hoàn toàn thanh Sidebar chứa các menu quản trị (Planning, Dispatch, Fleet, Schools, v.v.).

### 5.3. Giao diện của Giám hộ (Attendant UI)
*   **Hành vi**:
    *   **Landing Page**: Tự động chuyển hướng vào trang Điểm danh hoặc Danh sách chuyến đi được phân công.
    *   **Màn hình điểm danh**: Danh sách học sinh tại điểm đón/trả hiển thị kèm ảnh, tên và các nút bấm điểm danh nhanh (*Boarded*, *Absent*, *No Show*, *Drop-off*).
    *   **Giới hạn**: Không thấy các tính năng lập lịch, phân công, báo cáo tổng quan hay cấu hình.

### 5.4. Giao diện của Điều hành viên (Dispatcher UI)
*   **Hành vi**:
    *   Thấy đầy đủ giao diện quản trị điều hành chuyên nghiệp: Bảng điều khiển giám sát (Dashboard Cockpit), duyệt yêu cầu, tạo gói dịch vụ, kéo thả tuyến đường (Planning Workspace), phân công tài nguyên và xuất báo cáo.

---

## 6. Kế hoạch tích hợp người dùng (Account User Integration Notes)

Hệ thống quản lý tài khoản người dùng tập trung (Core Account Module) là **Source of Truth** duy nhất về thông tin tài khoản và Role Keycloak. Module School Bus chỉ lưu trữ các thông tin mở rộng của hồ sơ (Profile Extension) và một bảng shadow lưu thông tin cơ bản của người dùng phục vụ liên kết khóa ngoại.

### 6.1. Thiết kế liên kết Profile và User ở các Phase sau

*   Bảng shadow **`school_bus_user`** sẽ được tạo ra để lưu thông tin đồng bộ từ Core Account bao gồm: `id` (Khóa chính trùng với Account User ID), `username`, `email`, `full_name`, `tenant_id`, và các trường trạng thái.
*   Các bảng profile hiện tại sẽ được thêm trường liên kết `user_id` để map với người dùng hệ thống:
    *   `school_bus_parent_profile.user_id` -> Tham chiếu đến `school_bus_user.id`
    *   `school_bus_driver_profile.user_id` -> Tham chiếu đến `school_bus_user.id`
    *   `school_bus_attendant_profile.user_id` -> Tham chiếu đến `school_bus_user.id`

### 6.2. Lộ trình triển khai các Phase tiếp theo:

*   **Phase 2: Thiết kế Database & Profile Linking (Đã hoàn thành)**:
    *   Tạo migration script `V28` khởi tạo bảng shadow `school_bus_user`.
    *   Thống nhất `profile.user_id = school_bus_user.account_user_id` (Chưa tạo khóa ngoại FK ở phase này để tránh rủi ro dữ liệu lịch sử chưa đồng bộ, FK sẽ xem xét ở các phase sau).
*   **Phase 3: Tích hợp sự kiện qua Kafka (Kafka Consumer)**:
    *   Xây dựng Kafka Consumer lắng nghe các sự kiện thay đổi người dùng từ Core Account: `UserCreatedEvent`, `UserUpdatedEvent`, `UserDeletedEvent` để tự động thêm/sửa/xóa tương ứng trong bảng shadow `school_bus_user`.
*   **Phase 4: Đồng bộ dự phòng & Tích hợp Fallback API**:
    *   Xây dựng Sync Job hàng đêm quét đồng bộ dữ liệu người dùng nhằm xử lý các trường hợp Kafka bị mất tin nhắn hoặc trễ mạng.
    *   Thiết kế cơ chế Fallback gọi trực tiếp API của Core Account để lấy thông tin chi tiết user nếu bảng shadow chưa kịp cập nhật.
*   **Phase 5: Hiện thực hóa phân quyền Backend & Data Scope**:
    *   Áp dụng các annotation phân quyền `@PreAuthorize` tại Controller/Service Layer (ví dụ: `@PreAuthorize("hasRole('SCHOOL_BUS_DISPATCHER')")`).
    *   Thực hiện viết code kiểm tra quyền sở hữu dữ liệu (Data Scope validation) trong Service Implementation.
*   **Phase 6: Hoàn thiện bảo vệ Frontend (UI Route & Menu Guards)**:
    *   Cấu hình router guards trên frontend (`serp_web`) để chặn truy cập trái phép.
    *   Ẩn/Hiện menu điều hướng và các nút hành động trên giao diện động dựa trên Roles của token đăng nhập.

---

## 7. Các Quy tắc Bảo mật Bắt buộc (Mandatory Security Rules)

1.  **Cấm giả mạo định danh phụ huynh**: Phụ huynh tuyệt đối không được phép truyền bất kỳ tham số `parentId` nào từ Client lên. Backend bắt buộc phải tự resolve hồ sơ phụ huynh từ ID người dùng đăng nhập (`user_id` từ token).
2.  **Cấm truy cập chuyến đi chưa phân công**: Tài xế/Giám hộ gửi yêu cầu lấy thông tin chuyến đi phải đi qua bước kiểm tra sở hữu ở Service Layer. Tuyệt đối không trả về thông tin chuyến đi nếu chuyến đó không có `driver_id` hoặc `attendant_id` tương ứng với tài khoản đăng nhập.
3.  **Cấm điểm danh ngoài chuyến đi phân công**: Giám hộ thực hiện thao tác điểm danh học sinh phải được xác thực là đang làm việc trên đúng chuyến đi đó. Mọi ghi nhận điểm danh có ID Trip không do giám hộ đó phụ trách phải bị từ chối ngay lập tức.
4.  **Phân tách phạm vi Tenant triệt để**: Toàn bộ luồng thao tác của `SCHOOL_BUS_ADMIN` và `SCHOOL_BUS_DISPATCHER` đều phải được lọc tự động theo `tenant_id` từ token. Không cho phép truyền `tenantId` thủ công để truy cập chéo tenant khác.
5.  **Nguyên tắc "Frontend chỉ là UX"**: Mọi thao tác ghi (Create, Update, Delete, Operate, Mark Attendance) phải được kiểm tra phân quyền chặt chẽ ở Backend. Không bao giờ tin tưởng vào việc ẩn nút/giao diện ở Frontend làm giải pháp bảo mật duy nhất.

---

## 8. Các Câu hỏi Thảo luận (Open Questions)

1.  **Cơ chế gán ghép vai trò (Role Mapping)**: Khi một tài khoản được đồng bộ từ module Account, thông tin vai trò (Roles) sẽ được đồng bộ trực tiếp từ Keycloak/Token hay cần một bảng quản lý vai trò nội bộ trong module School Bus?
2.  **Xử lý trường hợp Tài xế kiêm Giám hộ**: Có cho phép một tài khoản người dùng liên kết với cả hồ sơ Driver và Attendant không? Nếu có, hệ thống UI sẽ chuyển chế độ landing như thế nào?
3.  **Cơ chế lưu trữ ảnh học sinh**: Để hỗ trợ điểm danh bằng hình ảnh cho Attendant, việc lưu trữ ảnh học sinh sẽ sử dụng chung dịch vụ MinIO của hệ thống hay một phân hệ lưu trữ riêng?
