# Báo Cáo Audit Bổ Sung Phase 0: Quy Trình Request & Route Planning

Tài liệu này trình bày kết quả rà soát chi tiết luồng xử lý từ yêu cầu đăng ký dịch vụ (Transport Request), phê duyệt (Approve Request), quản lý thuê bao (Student Subscription), cấu hình khung thời gian (Time Window), cho đến bộ lọc học sinh đủ điều kiện (Eligible Students) trong quy trình lập kế hoạch lộ trình (Route Planning).

---

## 1. Request Payload & FE Form State

### 1.1. Component và Page Chính
* **Page hiển thị chính**: `SchoolBusRequestFormPage` trong file [SchoolBusRequestFormPage.tsx](file:///d:/Web%20dev/DATN/Serp/serp_web/src/modules/school-bus/pages/SchoolBusRequestFormPage.tsx).
* **Component Form chính**: `TransportRequestForm` trong file [SchoolBusWorkflowForms.tsx](file:///d:/Web%20dev/DATN/Serp/serp_web/src/modules/school-bus/components/SchoolBusWorkflowForms.tsx).

### 1.2. Các API Client được gọi để Load Dữ Liệu
Khi khởi tạo màn hình tạo request mới (`/school-bus/requests/new`), hệ thống gọi các API sau từ `schoolBusApi.ts`:
* **Phụ huynh (Parents)**: `useGetParentsQuery(...)` gọi endpoint `GET /parents` để load danh sách phụ huynh.
* **Trường học (Schools)**: `useGetSchoolsQuery(...)` gọi endpoint `GET /schools` để load danh sách trường.
* **Học sinh (Students)**: `useGetStudentsQuery(...)` gọi endpoint `GET /students` để lọc học sinh theo phụ huynh.
* **Lịch học (Schedules)**: `useGetActiveSchoolSchedulesQuery(schoolId)` gọi endpoint `GET /school-schedules/by-school/active` để load lịch học của trường đã chọn.
* **Điểm đón/trả (Pickup/Drop-off)**: `useGetActiveSchoolPickupPointsQuery(schoolId)` gọi endpoint `GET /school-pickup-points/by-school/active` để lấy các điểm đón/trả được liên kết hoạt động với trường.
* **Thuê bao mục tiêu (Target Subscriptions)**: `useGetSubscriptionsQuery({ schoolId })` gọi endpoint `GET /subscriptions` (áp dụng khi loại yêu cầu khác `NEW_SERVICE` nhằm thay đổi/tạm ngưng dịch vụ hiện có).

### 1.3. Cơ Chế Tự Động Điền (Auto-fill) Khi Chọn Học Sinh
* **Hiện trạng**: **Không có cơ chế tự động điền.** 
* Khi người dùng chọn học sinh từ dropdown, form chỉ cập nhật giá trị `studentId`. Form không tự động gọi API lấy thông tin điểm đón/trả mặc định của học sinh từ profile của học sinh đó để điền vào form.
* **Hệ quả**: Nếu học sinh đã cấu hình điểm đón/trả mặc định trong hồ sơ học sinh, người dùng ở giao diện frontend vẫn **bắt buộc phải chọn lại thủ công** từ dropdown hoặc từ bản đồ.

### 1.4. Form State & Payload Submit Thực Tế

#### Form State của mỗi dòng học sinh (`students` array):
```typescript
{
  studentId: number;              // ID học sinh được chọn
  pickupPointId: string;          // ID điểm đón (dạng string từ select option)
  dropoffPointId: string;         // ID điểm trả
  schoolScheduleId: string;       // ID lịch học áp dụng
  tripOption: string;             // Hướng đăng ký dịch vụ (MORNING, AFTERNOON, ROUND_TRIP)
  monday: boolean;                // Cờ đi thứ Hai (mặc định: true)
  tuesday: boolean;               // Cờ đi thứ Ba (mặc định: true)
  wednesday: boolean;             // Cờ đi thứ Tư (mặc định: true)
  thursday: boolean;              // Cờ đi thứ Năm (mặc định: true)
  friday: boolean;                // Cờ đi thứ Sáu (mặc định: true)
  saturday: boolean;              // Cờ đi thứ Bảy (mặc định: false)
  sunday: boolean;                // Cờ đi Chủ Nhật (mặc định: false)
  targetSubscriptionId: string;   // ID subscription gốc (dùng cho dừng/tạm dừng/thay đổi)
  studentNote: string;            // Ghi chú riêng cho học sinh
}
```

#### Payload Submit lên API thực tế:
Khi bấm lưu, dữ liệu được chuẩn hóa và gửi lên dưới dạng:
* **Endpoint**: `POST /transport-requests` hoặc `PATCH /transport-requests/{id}`
* **Payload body**:
```json
{
  "parentProfileId": 1,
  "schoolId": 1,
  "requestType": "NEW_SERVICE",
  "effectiveFrom": "2026-06-01",
  "effectiveTo": null,
  "notes": "Ghi chú chung cho yêu cầu",
  "changeReason": null,
  "students": [
    {
      "studentId": 12,
      "pickupPointId": 5,
      "dropoffPointId": 5,
      "schoolScheduleId": 2,
      "tripOption": "ROUND_TRIP",
      "monday": true,
      "tuesday": true,
      "wednesday": true,
      "thursday": true,
      "friday": true,
      "saturday": false,
      "sunday": false,
      "targetSubscriptionId": null,
      "studentNote": "Cháu hay đi trễ"
    }
  ],
  "isActive": true
}
```

### 1.5. Logic Validation ở Frontend (FE)
* **Visual Validation (Readiness Badge)**: Component sử dụng hàm `getRowReadiness(rowValues, needsTarget)` để kiểm tra trạng thái nhập liệu trực quan:
  * Nếu chưa chọn học sinh -> Hiển thị badge `Missing student` màu đỏ.
  * Nếu chọn hướng đi cần đón (`MORNING` hoặc `ROUND_TRIP`) mà chưa có điểm đón -> Hiển thị badge `Missing pickup`.
  * Nếu chọn hướng đi cần trả (`AFTERNOON` hoặc `ROUND_TRIP`) mà chưa có điểm trả -> Hiển thị badge `Missing drop-off`.
  * Nếu chưa chọn lịch học -> Hiển thị badge `Missing schedule`.
  * Nếu yêu cầu thay đổi dịch vụ mà chưa chọn subscription gốc -> Hiển thị badge `Missing subscription`.
* **Zod Schema Validation**: Zod schema `transportRequestSchema` trong file `SchoolBusWorkflowForms.tsx` định nghĩa các trường `pickupPointId`, `dropoffPointId`, `schoolScheduleId`, `tripOption`, và `targetSubscriptionId` là **`.optional()`**.
* **Khả Năng Chặn Submit**: Nút submit form **chỉ bị disable khi đang loading (`isLoading = true`)**. Do Zod schema thiết lập các trường đón/trả/lịch học/chuyến đi là tùy chọn, form **không chặn người dùng submit** kể cả khi các trường này đang để trống. Việc chặn lưu hoàn toàn phụ thuộc vào kiểm tra tính hợp lệ ở phía backend.
* **Nếu student đã có default pickup/dropoff thì FE có bắt chọn lại không?**: Có. Vì form không tự động điền các giá trị mặc định của học sinh từ profile, nên người dùng bắt buộc phải chọn lại thủ công từ dropdown hoặc click trên bản đồ.

---

## 2. Backend Create Request

### 2.1. Controller và Service xử lý
* **Endpoint**: `POST /transport-requests` trong class `TransportRequestController`
* **Service Method xử lý**: `createTransportRequest` trong class `TransportRequestServiceImpl` (gọi tiếp hàm `replaceRequestStudents` để lưu chi tiết học sinh đăng ký).
* **Bảng cơ sở dữ liệu**:
  * Yêu cầu cha: `school_bus_transport_request` (thực thể `TransportRequestEntity`).
  * Chi tiết học sinh đăng ký: `school_bus_request_student` (thực thể `RequestStudentEntity`).

### 2.2. Các Trường Thông Tin Lưu Trực Tiếp Trong `RequestStudentEntity`
Khi lưu chi tiết học sinh đăng ký xe buýt, các trường sau được lưu xuống bảng `school_bus_request_student`:
* `student_id`: Liên kết đến học sinh (`StudentEntity`).
* `pickup_point_id`: Điểm đón yêu cầu (`PickupPointEntity`, có thể null).
* `default_dropoff_point_id`: Điểm trả yêu cầu (`PickupPointEntity`, có thể null).
* `school_schedule_id`: Lịch học yêu cầu (`SchoolScheduleEntity`, có thể null).
* `trip_option`: Hướng đăng ký dịch vụ (`TripOption` enum: `MORNING`, `AFTERNOON`, `ROUND_TRIP`).
* Các cột thứ từ thứ Hai đến Chủ Nhật (`monday` ... `sunday`): Kiểu dữ liệu Boolean.
* `target_subscription_id`: ID của thuê bao cũ bị tác động (nếu có).
* `subscription_id`: ID thuê bao được sinh ra sau khi phê duyệt yêu cầu này (ban đầu là null).
* `student_note`: Ghi chú riêng cho học sinh.

### 2.3. Các Validation Rule Chạy Khi Tạo/Cập Nhật Request
Các quy tắc nghiệp vụ sau được kiểm tra nghiêm ngặt trong phương thức `replaceRequestStudents` của `TransportRequestServiceImpl.java`:
1. **Lịch học thuộc trường học**: Kiểm tra `schedule.getSchool().getId()` trùng khớp với `request.getSchoolId()`. Lỗi ném ra: `AppErrorCode.Request.INVALID_REQUEST` ("*Schedule #... does not belong to the selected school*").
2. **Khoảng ngày hiệu lực chồng lấn**: Khoảng ngày hiệu lực của yêu cầu (`effectiveFrom` đến `effectiveTo`) bắt buộc phải giao cắt hoặc nằm trong khoảng ngày hoạt động của lịch học được chọn. Lỗi ném ra: `AppErrorCode.Request.INVALID_REQUEST` ("*Request effective range [...] does not overlap schedule effective range*").
3. **Phải chọn ít nhất 1 ngày**: Các cờ ngày từ thứ Hai đến Chủ Nhật phải có ít nhất 1 cờ được set `true`. Lỗi ném ra: `AppErrorCode.Request.INVALID_REQUEST` ("*At least one day of week must be selected for student #...*").
4. **Ngày đăng ký là tập con của ngày của lịch học**: Các ngày chọn đi xe phải nằm trong danh sách các ngày hoạt động của lịch học (`scheduleDays`). Lỗi ném ra: `AppErrorCode.Request.INVALID_REQUEST` ("*Day ... is not part of schedule #...*").
5. **Chọn điểm đón/trả theo TripOption**:
   * Nếu đăng ký chiều đi (`MORNING` hoặc `ROUND_TRIP`), bắt buộc phải truyền `pickupPointId`.
   * Nếu đăng ký chiều về (`AFTERNOON` hoặc `ROUND_TRIP`), bắt buộc phải truyền `dropoffPointId`.
   * Lỗi ném ra: `AppErrorCode.Request.INVALID_REQUEST` ("*Trip option ... requires a pickup/drop-off point...*").
6. **Điểm đón/trả liên kết với trường học**: Kiểm tra điểm đón/trả có liên kết hoạt động với trường thông qua `schoolPickupPointService.isPickupPointLinkedToSchool`. Lỗi ném ra: `AppErrorCode.Request.INVALID_REQUEST` ("*Pickup/Drop-off point #... is not linked to the selected school*").
7. **Kiểm tra loại sử dụng (Usage Type) của điểm đón/trả**:
   * Điểm đón yêu cầu bắt buộc phải có cấu hình loại sử dụng cho phép đón học sinh (`PICKUP_ONLY` hoặc `PICKUP_DROPOFF`).
   * Điểm trả yêu cầu bắt buộc phải có cấu hình loại sử dụng cho phép trả học sinh (`DROPOFF_ONLY` hoặc `PICKUP_DROPOFF`).
   * Lỗi ném ra: `AppErrorCode.Request.INVALID_REQUEST` ("*... does not support PICKUP/DROPOFF*").
8. **Yêu cầu Subscription gốc (Target Subscription)**: Với các loại yêu cầu dịch vụ không phải đăng ký mới (khác `NEW_SERVICE`), bắt buộc phải truyền `targetSubscriptionId`, subscription đó phải thuộc về cùng một học sinh và trường học, và không ở trạng thái dừng hoạt động (`STOPPED`, `EXPIRED`). Lỗi ném ra: `AppErrorCode.Request.INVALID_REQUEST` hoặc `INVALID_STATE`.
9. **Cảnh Báo Thiếu Khung Thời Gian (Time Window - KHÔNG CHẶN)**:
   * Nếu điểm đón/trả được chọn chưa được thiết lập khung giờ xe chạy (Window) tương ứng với lịch học (`schoolScheduleId`) và hướng tương ứng (`PICKUP_TO_SCHOOL`/`DROPOFF_FROM_SCHOOL`), hệ thống **vẫn cho phép lưu request thành công**, chỉ ghi log cảnh báo mức `WARN` vào bảng nhật ký kiểm toán (Audit Log).

### 2.4. Các Lỗ Hổng Validation (Thiếu Kiểm Tra)
Qua rà soát mã nguồn, hệ thống backend đang thiếu các kiểm tra nghiệp vụ quan trọng sau:
* ⚠️ **Học sinh không thuộc Phụ huynh**: Hệ thống không đối chiếu xem `studentId` trong danh sách đăng ký có thực sự trực thuộc tài khoản phụ huynh `parentProfileId` đang tạo yêu cầu hay không. Điều này cho phép phụ huynh tạo request cho học sinh bất kỳ trong hệ thống.
* ⚠️ **Học sinh không thuộc Trường học**: Hệ thống không kiểm tra xem học sinh đăng ký có đúng là học sinh đang học tại trường `schoolId` của request hay không (`student.getSchool().getId()` so với `request.getSchoolId()`).
* ⚠️ **Ngày hiệu lực trong quá khứ**: Không chặn việc thiết lập ngày bắt đầu hiệu lực `effectiveFrom` ở quá khứ.
* ⚠️ **Trùng lặp trạng thái đăng ký**: Cho phép tạo yêu cầu tạm ngưng (`PAUSE_SERVICE`) cho một subscription vốn đang tạm ngưng (`PAUSED`), hoặc yêu cầu kích hoạt lại (`RESUME_SERVICE`) cho subscription đang hoạt động bình thường (`ACTIVE`).

---

## 3. Approve Request -> Subscription

### 3.1. Điểm Khởi Chạy Phê Duyệt
* **Method xử lý**: `approveTransportRequest` trong file [TransportRequestServiceImpl.java](file:///d:/Web%20dev/DATN/Serp/school_bus_service/src/main/java/serp/project/school_bus_service/service/impl/TransportRequestServiceImpl.java).
* **Điều kiện tiên quyết**: Bản ghi Transport Request phải đang ở trạng thái **`RequestStatus.SUBMITTED`**. Nếu ở trạng thái khác, hệ thống sẽ chặn và ném lỗi `AppErrorCode.Request.ONLY_SUBMITTED_APPROVED`.

### 3.2. Ánh Xạ Dữ Liệu Sang Subscription
Khi duyệt yêu cầu, hệ thống gọi dịch vụ `IStudentSubscriptionService` để tạo hoặc thay đổi thông tin đăng ký dịch vụ của học sinh. Các thông tin được lấy trực tiếp từ bản ghi snapshot học sinh trong yêu cầu (`RequestStudentEntity` - viết tắt là `rs`):
* **Lộ trình đón trả & Khung lịch học**:
  * `tripOption`: `rs.getTripOption()` (nếu trống mặc định là `ROUND_TRIP`).
  * `pickupPoint`: `rs.getPickupPoint()` (nếu trống lấy từ điểm đón mặc định của hồ sơ học sinh).
  * `dropoffPoint`: `rs.getDropoffPoint()` (nếu trống lấy từ điểm trả mặc định hoặc điểm đón của học sinh).
  * `schoolSchedule`: `rs.getSchoolSchedule()`.
  * `days`: Sao chép toàn bộ các cờ từ thứ Hai đến Chủ Nhật của `rs` sang subscription.
* **Thời gian hiệu lực**: Lấy từ ngày hiệu lực của yêu cầu cha (`request.getEffectiveFrom()` và `request.getEffectiveTo()`), không lấy từ bản ghi học sinh con.

### 3.3. Liên Kết Thông Tin Gốc và Check Trùng Lặp
* **Liên kết ngược**: Bản ghi subscription lưu khóa ngoại `source_request_id` liên kết đến `TransportRequestEntity` gốc. Bản ghi subscription **không lưu trực tiếp** ID dòng học sinh của yêu cầu (`rs.getId()`), khóa ngoại này chỉ được lưu trong bảng lịch sử thay đổi `school_bus_student_subscription_history`.
* **Kiểm tra trùng lặp thời gian (Overlap Check)**: Hệ thống chạy query `existsOverlappingActiveSubscription` để kiểm tra học sinh đó đã có subscription nào khác đang ở trạng thái `ACTIVE` mà khoảng hiệu lực chồng chéo với khoảng hiệu lực của yêu cầu này hay không (áp dụng cho cùng một hướng đi `tripOption`). Nếu trùng lặp, hệ thống ném lỗi `AppErrorCode.Subscription.OVERLAP` và dừng quy trình duyệt.

### 3.4. Cơ Chế Xử Lý Cụ Thể Theo Loại Yêu Cầu (Request Type)
Mỗi loại yêu cầu được điều phối (`dispatchApprove`) để áp dụng các thay đổi dịch vụ khác nhau:

| Loại Yêu Cầu | Cơ Chế Xử Lý Chi Tiết |
| :--- | :--- |
| **`NEW_SERVICE`** | Tạo mới một bản ghi thuê bao (`StudentSubscriptionEntity`) ở trạng thái `ACTIVE`. |
| **`CHANGE_SERVICE`** | 1. **Đóng thuê bao cũ**: Cập nhật trạng thái của thuê bao gốc thành `STOPPED`, đặt lại ngày kết thúc hiệu lực `effectiveTo = ngày bắt đầu yêu cầu mới - 1 ngày`. <br>2. **Tạo thuê bao mới**: Tạo một bản ghi thuê bao mới hoàn toàn với mã code mới, lấy thông tin lộ trình mới và có trạng thái `ACTIVE` bắt đầu từ ngày `request.getEffectiveFrom()`. |
| **`STOP_SERVICE`** | Đóng thuê bao cũ bằng cách cập nhật trạng thái thành `STOPPED` và cập nhật ngày kết thúc hiệu lực `effectiveTo = request.getEffectiveFrom()`. Không tạo mới. |
| **`PAUSE_SERVICE`** | 1. Tạo bản ghi tạm dừng trong bảng `school_bus_subscription_pause_period` (trạng thái là `ACTIVE` nếu tạm dừng ngay hoặc `SCHEDULED` nếu tạm dừng trong tương lai). <br>2. **Chuyển đổi trạng thái**: Nếu thời gian tạm dừng có hiệu lực ngay lập tức (ngày bắt đầu pause <= ngày hiện tại), cập nhật trạng thái thuê bao gốc sang `PAUSED`. Nếu tạm dừng trong tương lai, trạng thái thuê bao vẫn giữ nguyên là `ACTIVE`. |
| **`RESUME_SERVICE`** | 1. Tìm tất cả các khoảng tạm dừng có trạng thái `ACTIVE` hoặc `SCHEDULED` của thuê bao đó và cập nhật trạng thái của chúng thành `CANCELLED`. <br>2. Cập nhật trạng thái của thuê bao gốc trở lại thành `ACTIVE`. |
| **`RENEW_SERVICE`** | Tạo mới một bản ghi thuê bao (`ACTIVE`), kế thừa các thông tin cấu hình từ thuê bao cũ đối với các trường bị để trống trong yêu cầu gia hạn. |

### 3.5. Lưu Lịch Sử Thay Đổi (Subscription History)
Tất cả các hành động cập nhật, đóng, tạm ngưng hoặc tạo mới thuê bao khi duyệt yêu cầu đều được lưu tự động vào bảng lịch sử `school_bus_student_subscription_history` (thực thể `StudentSubscriptionHistoryEntity`). Bản ghi lịch sử lưu lại chi tiết: loại thay đổi (`changeType`), trạng thái cũ/mới, thông tin đón trả mới, khung giờ mới, thời gian hiệu lực mới và người thực hiện phê duyệt.

### 3.6. Kết Luận: StudentSubscription có đáng tin cậy làm nhu cầu (Demand) chính thức?
* **Có, nhưng cần xử lý nghiệp vụ bổ sung về lịch tạm ngưng (Pause Periods)**.
* Bản ghi `StudentSubscriptionEntity` là nguồn dữ liệu chuẩn nhất để làm đầu vào (demand) cho việc gom chuyến lập lộ trình (Route Planning) vì nó chứa thông tin lộ trình đón trả thực tế, lịch học và ngày hiệu lực của học sinh.
* **Tuy nhiên**: Do thuê bao tạm dừng trong tương lai vẫn có trạng thái `status = ACTIVE` trong cơ sở dữ liệu (chỉ đổi sang `PAUSED` khi ngày thực tế chạm mốc bắt đầu tạm dừng), bộ lọc lập lộ trình **bắt buộc** phải thực hiện truy vấn kiểm tra bảng phụ `school_bus_subscription_pause_period` để loại bỏ các học sinh đang trong giai đoạn tạm dừng, tránh gom nhầm học sinh không đi xe.

---

## 4. Window Model & Validation

### 4.1. Cấu Trúc Thực Thể `SchoolPickupPointWindowEntity`
Lưu trữ khung giờ xe chạy tại các điểm đón/trả tương ứng với từng lịch học của trường:
* **schoolPickupPoint**: Khóa ngoại trỏ đến bảng liên kết `school_bus_school_pickup_point` (không trỏ trực tiếp đến `school_bus_pickup_point` chung). Điều này có nghĩa là khung giờ đón/trả của một điểm là cấu hình riêng biệt cho từng trường học.
* **schoolSchedule**: Khóa ngoại trỏ đến lịch học (`SchoolScheduleEntity`) của trường.
* **direction**: Hướng đi (chuỗi ký tự, nhận giá trị `PICKUP_TO_SCHOOL` cho chiều đi sáng hoặc `DROPOFF_FROM_SCHOOL` cho chiều về chiều).
* **windowStart** / **windowEnd**: Kiểu dữ liệu `LocalTime`, xác định khoảng giờ xe đón học sinh hoặc trả học sinh tại điểm.
* **estimatedDistanceToSchoolKm**: Khoảng cách ước tính từ điểm đến trường (kiểu Double).
* **estimatedDurationToSchoolMin**: Thời gian di chuyển ước tính đến trường bằng phút (kiểu Integer).

### 4.2. Logic Validation Khung Giờ (Time Window)
* **Quy tắc tương thích giờ học** (hàm `validateWindowAgainstSchedule`):
  * **Chiều đi (`PICKUP_TO_SCHOOL`)**: Giờ kết thúc đón (`windowEnd`) cộng với thời gian di chuyển ước tính (`durationMin`) bắt buộc phải trước hoặc bằng giờ quy định học sinh phải có mặt tại trường (`arrivalDeadline` của lịch học).
  * **Chiều về (`DROPOFF_FROM_SCHOOL`)**: Giờ bắt đầu trả (`windowStart`) bắt buộc phải sau giờ tan trường (`departureTime` của lịch học) cộng thêm thời gian di chuyển ước tính.

### 4.3. Sự Hiện Diện của Bộ Lọc Window ở các Khâu
* **Khi Tạo Request**: Chỉ kiểm tra và ghi nhận cảnh báo `WARN` nếu thiếu Window, **không chặn** việc lưu yêu cầu.
* **Khi Phê Duyệt**: Không chạy kiểm tra Window.
* **Khi Lập Lộ Trình (Route Planning)**: **Bắt buộc**. Hệ thống lọc loại bỏ hoàn toàn học sinh có điểm đón/trả chưa được cấu hình Window hợp lệ cho lịch học và hướng đi tương ứng.

### 4.4. Điểm Thiếu Sót về API Đối Với Giao Diện Người Dùng (FE)
* ⚠️ **Không có API trả về điểm đón/trả tương thích đã lọc sẵn Window.**
* Hiện tại, các endpoint chỉ trả về danh sách điểm liên kết chung (`GET /school-pickup-points/by-school/active`) hoặc lấy danh sách Window độc lập (`GET /school-pickup-point-windows/by-schedule`).
* **Hậu quả**: Khi tạo yêu cầu ở frontend, dropdown điểm đón/trả hiển thị toàn bộ các điểm liên kết mà không lọc theo Window của lịch học đã chọn. Người dùng có thể chọn một điểm không có Window hợp lệ. Request được tạo và duyệt thành công, nhưng học sinh sẽ bị loại bỏ khỏi danh sách lập lộ trình một cách âm thầm mà không rõ nguyên nhân.

---

## 5. Route Planning Session & Eligibility

### 5.1. Quy Trình Tính Toán Học Sinh Đủ Điều Kiện (Eligible Students)
Quy trình tính toán danh sách học sinh tham gia gom chuyến lập lộ trình được thực hiện bởi phương thức `findEligible` trong class `RouteEligibilityServiceImpl.java` (gọi bởi API `GET /route-planning-sessions/{id}/eligible-students` hoặc khi xem trước cấu hình phiên lập kế hoạch):
1. **Lấy ứng viên từ cơ sở dữ liệu**: Lấy các subscription `ACTIVE` của trường tương ứng, thời gian hiệu lực bao phủ ngày chạy xe, thứ trong tuần trùng khớp và có chọn điểm đón/trả.
2. **Loại bỏ thuê bao tạm dừng**: Chạy batch query tìm các subscription có lịch tạm ngưng (`SubscriptionPausePeriod`) đang hoạt động và loại bỏ chúng.
3. **Lọc theo Time Window**: Lấy tất cả Point ID của các học sinh còn lại, truy vấn xem điểm nào đã có cấu hình Window cho lịch học và hướng đi tương ứng, sau đó loại bỏ các học sinh có điểm đón/trả thiếu Window.

### 5.2. Các Điều Kiện Lọc Cơ Sở Dữ Liệu (JPA Query)
Phương thức `findEligibleForPlanning` trong repository `StudentSubscriptionRepository.java` thực hiện truy vấn SQL để lấy các ứng viên ban đầu:
* Trạng thái hoạt động: `status = 'ACTIVE'` và `isDeleted = false`.
* Hiệu lực ngày: Ngày chạy xe (`serviceDate`) phải nằm trong khoảng hiệu lực của thuê bao (`effectiveFrom <= serviceDate` và `effectiveTo IS NULL` hoặc `effectiveTo >= serviceDate`).
* Thứ trong tuần: Thuê bao phải đăng ký ngày đi xe tương ứng với ngày trong tuần của `serviceDate` (ví dụ: ngày chạy xe là thứ Hai thì trường `monday` phải bằng `true`).
* Hướng đi và Điểm đón trả:
  * Nếu lập lộ trình chiều đi (`isOutbound = true`), trường `tripOption` phải là `MORNING` hoặc `ROUND_TRIP`, đồng thời `pickupPoint` phải khác `null`.
  * Nếu lập lộ trình chiều về (`isOutbound = false`), trường `tripOption` phải là `AFTERNOON` hoặc `ROUND_TRIP`, đồng thời `dropoffPoint` phải khác `null`.

### 5.3. Loại Bỏ Học Sinh Tạm Ngưng (Pause Period Check)
Hệ thống lấy danh sách ID học sinh ứng viên, chạy truy vấn `findPausedSubscriptionIds` trong `SubscriptionPausePeriodRepository.java`:
```sql
SELECT DISTINCT p.subscription.id
  FROM SubscriptionPausePeriodEntity p
 WHERE p.subscription.id IN :subscriptionIds
   AND p.tenantId = :tenantId
   AND p.isDeleted = false
   AND p.status IN ('ACTIVE', 'SCHEDULED')
   AND p.pauseFrom <= :serviceDate
   AND (p.pauseTo IS NULL OR p.pauseTo >= :serviceDate)
```
Bất kỳ thuê bao nào nằm trong danh sách ID tạm ngưng này sẽ bị loại khỏi bộ lọc lập kế hoạch cho ngày `serviceDate`.

### 5.4. Lọc Theo Điểm Đón/Trả Có Khung Giờ (Window Check)
Hệ thống thu thập tất cả các ID điểm đón/trả của các thuê bao còn lại, truy vấn các điểm có khung giờ hợp lệ:
* `windowService.findPointIdsWithWindow(schoolId, pointIds, schoolScheduleId, expectedDir, tenantId)`.
* Học sinh nào có điểm đón/trả không nằm trong danh sách điểm có khung giờ đón/trả hợp lệ sẽ bị loại bỏ khỏi danh sách cuối cùng.

### 5.5. Hiện Tượng Học Sinh "Biến Mất" (Silent Disappearance)
* ⚠️ **Học sinh bị loại bỏ âm thầm mà không có bất kỳ thông báo lỗi nào.**
* Mặc dù thực thể API trả về `PlanningPreviewResponse` có cấu trúc trường `issues` (dự kiến chứa danh sách các vấn đề phát sinh như thiếu thông tin học sinh, lỗi điểm đón trả...), phương thức `buildPreview` trong `RouteEligibilityServiceImpl` **không hề triển khai bất kỳ logic nào để thêm cảnh báo** khi học sinh bị loại bỏ do thiếu Window hoặc đang bị tạm dừng.
* **Các nguyên nhân chính khiến học sinh không xuất hiện trong phiên lập lộ trình**:
  1. **Thiếu cấu hình Time Window**: Điểm đón/trả được chọn của học sinh chưa được tạo Window cho lịch học hoặc hướng đi tương ứng. Đây là nguyên nhân phổ biến nhất.
  2. **Trùng lịch Tạm dừng (Pause Period)**: Học sinh có lịch tạm dừng bao phủ ngày chạy xe (dù trạng thái thuê bao ở màn hình danh sách vẫn hiển thị là `ACTIVE`).
  3. **Không đăng ký ngày chạy xe**: Ngày lập kế hoạch chạy xe rơi vào ngày học sinh không đăng ký (ví dụ: xe chạy thứ Bảy nhưng học sinh chỉ tích chọn thứ Hai - thứ Sáu).
  4. **Hết hạn hiệu lực**: Ngày lập kế hoạch nằm ngoài khoảng ngày hiệu lực của thuê bao (`effectiveFrom` và `effectiveTo`).
  5. **Thay đổi dịch vụ chưa được duyệt**: Học sinh đã gửi yêu cầu gia hạn hoặc thay đổi dịch vụ nhưng chưa được ban quản trị phê duyệt sang trạng thái `APPROVED` để tạo/cập nhật subscription mới.
