## 1. Phân Loại Tuyến Đường (Route Classification)

Hệ thống tự động xác định loại tuyến vận chuyển dựa trên thông tin địa chỉ của **Người Gửi (Sender)** và **Người Nhận (Receiver)**:

| Loại tuyến (Route Type) | Quy tắc xác định (Matching Rules)                                                     | Ví dụ thực tế                                                              |
| :---------------------- | :------------------------------------------------------------------------------------ | :------------------------------------------------------------------------- |
| **Nội tỉnh - Nội cụm**  | Cùng `province_code` **VÀ** Cùng mã cụm xã/phường (`phan_loai`)                       | Từ Phường A sang Phường B (cùng thuộc Trung tâm Quận 1, TP.HCM)            |
| **Nội tỉnh - Liên cụm** | Cùng `province_code` **NHƯNG** Khác mã cụm xã/phường (`phan_loai`)                    | Từ Quận 1 sang Huyện Cần Giờ (TP.HCM)                                      |
| **Nội miền**            | Khác `province_code` **NHƯNG** Cùng vùng miền (`mien`)                                | Từ Hà Nội đi Hải Phòng (Cùng miền Bắc)                                     |
| **Liên miền**           | Khác miền (`mien`) hoàn toàn                                                          | Từ Hà Nội đi Nghệ An (Bắc -> Trung) hoặc Đà Nẵng đi Cần Thơ (Trung -> Nam) |
| **Liên miền đặc biệt**  | Tuyến chạy thẳng kết nối giữa 3 trục đô thị lớn: **Hà Nội, Đà Nẵng, TP. Hồ Chí Minh** | Từ Hà Nội đi TP. Hồ Chí Minh                                               |

---

## 2. Quy Tắc Xác Định Trọng Lượng Tính Cước (Chargeable Weight)

Trọng lượng dùng để tính tiền (`chargeable_weight`) là giá trị lớn nhất giữa **Trọng lượng thực tế** và **Trọng lượng quy đổi thể tích**.

### 2.1. Công thức quy đổi thể tích

$$Trọng\ lượng\ quy\ đổi\ (kg) = \frac{Dài\ (cm) \times Rộng\ (cm) \times Cao\ (cm)}{5000}$$
_(Kết quả sau đó được đổi sang đơn vị Gram để đồng bộ dữ liệu)._

### 2.2. Điều kiện chặn & Loại trừ

- **Giới hạn tối đa:** Dịch vụ hỏa tốc chỉ chấp nhận đơn hàng có trọng lượng **< 15 kg**.
- **Kích thước tối thiểu:** Nếu bất kỳ chiều nào (Dài, Rộng, Cao) < 10cm, hệ thống tự động làm tròn chiều đó lên **10cm** trước khi tính quy đổi.
- **Hàng cồng kềnh nhỏ:** Nếu tổng kích thước 3 chiều (Dài + Rộng + Cao) **< 100cm**, bỏ qua bước quy đổi thể tích, lấy luôn trọng lượng thực tế làm trọng lượng tính cước.

### 2.3. Quy tắc làm tròn nấc trọng lượng (Weight Rounding)

Quy tắc làm tròn chỉ áp dụng khi trọng lượng đạt **từ 2.000 gram (2kg) trở lên**. Phần trọng lượng lẻ vượt mức sẽ được làm tròn tiến theo nấc **500 gram (0.5kg)**:

- Trọng lượng $\le$ 2000g: Giữ nguyên để tính cước nấc đầu.
- Trọng lượng > 2000g: Phần dôi dư được làm tròn lên bội số của 500g gần nhất.

_Ví dụ minh họa làm tròn:_

- _Hàng 1.200g (dưới 2kg) $
ightarrow$ Tính nấc đầu (2.000g)._
- _Hàng 2.100g $
ightarrow$ Làm tròn thành **2.500g** (thừa 100g tính thành 1 nấc 500g)._
- _Hàng 2.650g $
ightarrow$ Làm tròn thành **3.000g** (thừa 650g tính thành 2 nấc 500g)._

---

## 3. Công Thức Tính Cước Chính (Base Freight)

Mỗi loại tuyến đường sẽ có cấu hình gồm 2 mức giá: **Giá nấc đầu (cho 2.000g đầu tiên)** và **Giá mỗi 0.5kg tiếp theo**.

### Công thức:

- **Trường hợp Trọng lượng tính cước $\le$ 2.000g:**
  $$Tổng\ cước\ chính = Giá\ nấc\ đầu$$
- **Trường hợp Trọng lượng tính cước > 2.000g:**
  $$Tổng\ cước\ chính = Giá\ nấc\ đầu + \left( \frac{Trọng\ lượng\ tính\ cước - 2000g}{500g} \right) \times Giá\ mỗi\ 0.5kg\ tiếp\ theo$$

---

## 4. Quy Tắc Tính Phụ Phí (Surcharges) & Dịch Vụ Gia Tăng (VAS)

Phụ phí được tính riêng và cộng dồn vào tổng tiền thanh toán của đơn hàng.

### 4.1. Phụ phí Vùng sâu - Vùng xa

Áp dụng khi địa chỉ nhận thuộc danh mục vùng sâu, vùng xa (`loai_tuyen` = 'VUNG_XA'):

- **Đơn hàng $\le$ 5.000g (5kg):** Phụ thu cố định **+7.000đ / đơn hàng**.
- **Đơn hàng > 5.000g (5kg):** Phụ thu 7.000đ + cộng thêm **500đ cho mỗi 500g** dôi dư (áp dụng quy tắc làm tròn nấc 500g tương tự cước chính).

### 4.2. Phí thu hộ (COD)

- **Miễn phí hoàn toàn** tiền công thu hộ tiền mặt (Khách hàng chỉ trả tiền cước vận chuyển, không phát sinh phí xử lý tiền COD).

### 4.3. Phụ phí Hàng hóa đặc biệt (Chọn theo loại hàng)

Khi người dùng tick chọn thuộc tính hàng hóa đặc biệt, hệ thống áp dụng các mức thu sau:

1. **Hàng giá trị cao (Giá trị khai báo > 3.000.000đ):**
   - Phí bảo hiểm = **0.5%** giá trị khai báo.
   - Số tiền thu tối thiểu: **5.000đ**.
2. **Hóa đơn, Giấy tờ chứng từ quan trọng:**
   - Phụ thu cố định **+5.000đ / đơn hàng**.
3. **Hàng dễ vỡ / Chất lỏng:**
   - Phụ thu theo trọng lượng: **+1.000đ / mỗi kg** trọng lượng tính cước.
4. **Hàng quá khổ (Có chiều dài từ 1.2 mét trở lên):**
   - Phụ thu theo trọng lượng: **+2.000đ / mỗi kg** trọng lượng tính cước.

---
