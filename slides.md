## Slide 1 — Bài toán

### Tiêu đề

> Doanh nghiệp cần một nền tảng thống nhất nhưng linh hoạt theo nhu cầu

### Nội dung hiển thị

**Nhu cầu quản trị tập trung**

- Doanh nghiệp có nhiều phòng ban và nghiệp vụ liên quan.
- Công cụ rời rạc làm phân tán dữ liệu, người dùng, quyền truy cập và quy trình.

**Nhu cầu sử dụng linh hoạt**

- Chức năng cần quản lý phụ thuộc vào lĩnh vực, cơ cấu và quy mô.
- Không phải doanh nghiệp nào cũng cần toàn bộ chức năng ERP.

**Bài toán đặt ra**

> Xây dựng một nền tảng SaaS cho phép mỗi doanh nghiệp đăng ký và sử dụng các module quản trị phù hợp.

### Bố cục đề xuất

```text
Nhiều nghiệp vụ                  Nhu cầu khác nhau
nhưng công cụ rời rạc            theo từng doanh nghiệp
        \                              /
         \                            /
          → NỀN TẢNG SAAS ĐA MODULE ←
```

### Lời trình bày

> Khi số lượng phòng ban và nghiệp vụ tăng, doanh nghiệp thường phải sử dụng nhiều
> công cụ quản lý khác nhau. Điều này có thể làm phân tán dữ liệu, tài khoản, quyền
> truy cập và quy trình. Trong khi đó, mỗi doanh nghiệp lại cần một tập chức năng
> khác nhau tùy theo lĩnh vực, cơ cấu và quy mô. Vì vậy, bài toán đặt ra là: làm thế
> nào xây dựng một nền tảng quản trị thống nhất nhưng vẫn cho phép từng doanh nghiệp
> lựa chọn và mở rộng chức năng theo nhu cầu? SERP tiếp cận bài toán này theo mô hình
> SaaS đa module.

### Câu chuyển

> Từ bài toán này, hệ thống cần đáp ứng bốn nhóm yêu cầu nền tảng.

---

## Slide 2 — Yêu cầu hệ thống

### Tiêu đề

> Từ bài toán, hệ thống cần đáp ứng bốn yêu cầu nền tảng

### Nội dung hiển thị

**Y1. Linh hoạt về phạm vi chức năng**

- Doanh nghiệp lựa chọn các module phù hợp với nhu cầu.
- Có thể bổ sung module khi phạm vi quản trị mở rộng.

**Y2. Hỗ trợ nhiều doanh nghiệp trên cùng nền tảng**

- Mỗi doanh nghiệp có không gian quản trị riêng.
- Dữ liệu giữa các doanh nghiệp phải được cô lập.

**Y3. Kiểm soát quyền sử dụng**

- Quyền sử dụng phù hợp với phòng ban, chức vụ và trách nhiệm.
- Người dùng chỉ được thao tác trong phạm vi được giao.

**Y4. Có khả năng mở rộng theo module**

- Module mới có thể được bổ sung mà không làm gián đoạn các module hiện có.
- Các module có thể phát triển và mở rộng theo đặc điểm nghiệp vụ và tải sử dụng.
- Người dùng vẫn có một điểm truy cập và trải nghiệm thống nhất.

### Bố cục đề xuất

```text
┌──────────────────────────┬──────────────────────────┐
│ Y1. Phạm vi chức năng    │ Y2. Nhiều doanh nghiệp  │
│ linh hoạt                │ trên cùng nền tảng      │
├──────────────────────────┼──────────────────────────┤
│ Y3. Kiểm soát quyền      │ Y4. Mở rộng theo module │
│ sử dụng                  │                          │
└──────────────────────────┴──────────────────────────┘
```

### Lời trình bày

> Từ bài toán trên, SERP cần đáp ứng bốn yêu cầu. Ở cấp doanh nghiệp, nền tảng phải
> cho phép lựa chọn module theo nhu cầu và phải cô lập không gian quản trị, dữ liệu
> của từng doanh nghiệp. Ở cấp người dùng, hệ thống phải kiểm soát chức năng và phạm
> vi dữ liệu mà mỗi người được phép thao tác. Cuối cùng, ở cấp kiến trúc, nền tảng
> phải tiếp nhận và mở rộng các module tương đối độc lập mà vẫn duy trì một điểm
> truy cập và trải nghiệm thống nhất.

### Câu chuyển

> Bốn yêu cầu này được chuyển thành bốn mục tiêu và bốn nhóm giải pháp tương ứng
> của SERP.

---

## Slide 3 — Mục tiêu và giải pháp

### Tiêu đề

> Bốn yêu cầu được chuyển thành bốn mục tiêu và giải pháp tương ứng

### Nội dung hiển thị

| Yêu cầu | Mục tiêu của SERP | Giải pháp |
|---|---|---|
| **Y1. Phạm vi chức năng linh hoạt** | Cho phép doanh nghiệp đăng ký và bổ sung module theo nhu cầu | Quản lý danh mục module, gói dịch vụ và phạm vi module của từng doanh nghiệp |
| **Y2. Hỗ trợ nhiều doanh nghiệp** | Tổ chức hệ thống theo mô hình SaaS multi-tenant và cô lập dữ liệu | Mô hình hóa mỗi doanh nghiệp thành một tenant; duy trì ngữ cảnh tenant và giới hạn dữ liệu theo tenant |
| **Y3. Kiểm soát quyền sử dụng** | Bảo đảm đúng người dùng thực hiện đúng chức năng trong đúng phạm vi | Định danh tập trung; cấp quyền module và vai trò; service kiểm tra quyền đối với thao tác và tài nguyên nghiệp vụ |
| **Y4. Mở rộng theo module** | Cho phép module phát triển, triển khai và mở rộng tương đối độc lập nhưng vẫn vận hành thống nhất | Kiến trúc microservices; module sở hữu logic và dữ liệu; dùng chung điểm truy cập, giao diện và quy ước tích hợp |

### Chuỗi ánh xạ rút gọn

```text
Y1 → M1. Lựa chọn module theo nhu cầu
   → Danh mục module · Gói dịch vụ · Module của doanh nghiệp

Y2 → M2. Vận hành SaaS multi-tenant
   → Không gian tenant · Ngữ cảnh tenant · Cô lập dữ liệu

Y3 → M3. Kiểm soát đúng người và đúng phạm vi
   → Định danh · Quyền module · Vai trò · Kiểm tra nghiệp vụ

Y4 → M4. Mở rộng hệ thống theo module
   → Microservices · Sở hữu dữ liệu · Nền tảng dùng chung
```

**Phạm vi minh chứng**

> Quản trị dự án · Trao đổi nội bộ · CRM

**Giới hạn phạm vi**

> Xây dựng khung nền tảng và các module minh chứng, không nhằm hoàn thiện toàn bộ chức năng của một ERP thương mại.

### Lời trình bày

> SERP ánh xạ từng yêu cầu vào một cơ chế thiết kế cụ thể. Tính linh hoạt chức năng
> được hiện thực bằng danh mục module, gói dịch vụ và phạm vi module của từng doanh
> nghiệp. Khả năng phục vụ nhiều doanh nghiệp dựa trên mô hình tenant, trong đó ngữ
> cảnh và dữ liệu được giới hạn theo từng tenant. Việc kiểm soát sử dụng kết hợp
> định danh, quyền module, vai trò và kiểm tra phạm vi nghiệp vụ. Cuối cùng, ranh
> giới service và quyền sở hữu dữ liệu giúp các module phát triển tương đối độc lập,
> trong khi Gateway, định danh và giao diện chung duy trì trải nghiệm thống nhất.
> Các cơ chế này được minh chứng qua ba module: quản trị dự án, trao đổi nội bộ và
> CRM.

### Câu chuyển sang phân tích và thiết kế

> Bốn nhóm giải pháp xác định các vấn đề cần giải quyết. Phần tiếp theo phân tích
> cách SERP hiện thực hóa từng giải pháp trong thiết kế hệ thống.

Ba slide này tạo đúng chuỗi:

```text
Vì sao cần SERP?
→ SERP phải đáp ứng điều gì?
→ SERP đặt mục tiêu gì và giải quyết bằng cách nào?
```

---

## Slide 4 — Phạm vi chức năng và tác nhân

### Tiêu đề

> SERP phân tách ba phạm vi sử dụng: nền tảng, tổ chức và nghiệp vụ

### Hình sử dụng

`thesis/Hinhve/usecase_tong_quan.png`

Ảnh chiếm phần lớn slide. Chỉ bổ sung ba nhãn ngắn:

- **Quản trị viên SERP:** quản lý danh mục module và dịch vụ nền tảng.
- **Quản trị viên tổ chức:** quản lý doanh nghiệp, người dùng và quyền sử dụng.
- **Người dùng tổ chức:** thực hiện nghiệp vụ trong các module được cấp quyền.

### Nội dung trình bày

> Trước khi phân tích bốn nhóm giải pháp, cần xác định biên hệ thống và các tác
> nhân sử dụng ở từng cấp. Ở cấp nền tảng, quản trị viên SERP quản trị hệ thống và
> danh mục dịch vụ dùng chung. Ở cấp tổ chức, quản trị viên tổ chức thực hiện các
> cấu hình dành riêng cho doanh nghiệp. Ở cấp nghiệp vụ, người dùng tổ chức làm
> việc trong các module được cấp quyền như quản trị dự án, trao đổi nội bộ và CRM.
> Sự phân tách ba cấp trách nhiệm này là cơ sở để thiết kế cơ chế quản trị module,
> cô lập tenant và kiểm soát quyền truy cập.

Không đọc lần lượt từng use case trong hình.

### Câu chuyển

> Ba module nghiệp vụ phục vụ những miền và quy tắc khác nhau. Vì vậy, cần xác định
> phần nào phải thuộc riêng từng module và phần nào nên được nền tảng cung cấp
> thống nhất.

---

## Slide 5 — Độc lập nghiệp vụ, thống nhất nền tảng

### Tiêu đề

> Mỗi module sở hữu nghiệp vụ riêng nhưng cùng kế thừa các năng lực nền tảng của SERP

### Nội dung hiển thị

```text
                    NGHIỆP VỤ ĐỘC LẬP

┌────────────────────┐ ┌────────────────────┐ ┌────────────────────┐
│ QUẢN TRỊ DỰ ÁN     │ │ TRAO ĐỔI NỘI BỘ   │ │ CRM                │
│ Workflow           │ │ Kênh và thành viên│ │ Khách hàng         │
│ Vai trò, nguồn lực │ │ Tin nhắn           │ │ Cơ hội bán hàng    │
│ Ràng buộc công việc│ │ Cập nhật realtime  │ │ Pipeline           │
│                    │ │                    │ │                    │
│ Logic và dữ liệu   │ │ Logic và dữ liệu   │ │ Logic và dữ liệu   │
│ thuộc module       │ │ thuộc module       │ │ thuộc module       │
└─────────┬──────────┘ └─────────┬──────────┘ └─────────┬──────────┘
          └───────────────────────┼──────────────────────┘
                                  ↓
                    NỀN TẢNG SERP THỐNG NHẤT

 Quản trị module · Ngữ cảnh tenant · Định danh và phân quyền
       Điểm truy cập chung · Trải nghiệm giao diện thống nhất
```

### Kết luận

> SERP thống nhất các năng lực nền tảng, không đồng nhất hóa nghiệp vụ của các module.

### Nội dung trình bày

> Ba module minh chứng thuộc ba miền nghiệp vụ khác nhau. Module quản trị dự án xử
> lý workflow, nguồn lực và ràng buộc công việc; module trao đổi nội bộ quản lý
> thành viên, tin nhắn và cập nhật gần thời gian thực; CRM quản lý khách hàng, cơ
> hội và pipeline bán hàng. Để hạn chế phụ thuộc và cho phép từng miền phát triển
> tương đối độc lập, SERP lựa chọn để mỗi module sở hữu logic và dữ liệu nghiệp vụ
> của mình. Những năng lực dùng chung như quản trị module, ngữ cảnh tenant, định
> danh, phân quyền, điểm truy cập và giao diện được cung cấp ở cấp nền tảng. Như
> vậy, SERP thống nhất các hợp đồng nền tảng nhưng không đồng nhất hóa nghiệp vụ.

### Câu chuyển

> Nguyên tắc “độc lập ở nghiệp vụ, thống nhất ở nền tảng” được hiện thực hóa
> trong kiến trúc tổng thể sau đây.

---

## Slide 6 — Kiến trúc tổng thể

### Tiêu đề

> SERP thống nhất ở năng lực nền tảng và phân tách theo miền nghiệp vụ

### Hình sử dụng

`thesis/Hinhve/serp_architecture.png`

Ảnh chiếm toàn bộ phần nội dung. Chỉ bổ sung ba callout đánh số:

1. **Điểm truy cập thống nhất:** Web → API Gateway.
2. **Ranh giới nghiệp vụ:** Account, Project Management, Discuss, CRM và
   Notification.
3. **Hạ tầng hỗ trợ:** Keycloak, Kafka, Redis và PostgreSQL theo service.

### Nội dung trình bày

> Nguyên tắc độc lập nghiệp vụ và thống nhất nền tảng được thể hiện trong kiến trúc
> tổng thể. Mọi request từ Web đi qua API Gateway trước khi được định tuyến tới
> service phù hợp. Account Service quản lý các năng lực cấp nền tảng như doanh
> nghiệp, người dùng, module và quyền. Project Management, Discuss và CRM sở hữu
> logic cùng dữ liệu của từng miền nghiệp vụ, còn Notification Service đảm nhiệm
> việc phân phối thông báo. Ở lớp hạ tầng, Keycloak cung cấp định danh và xác thực
> JWT, Kafka hỗ trợ giao tiếp bất đồng bộ, Redis cung cấp bộ nhớ đệm, còn dữ liệu
> PostgreSQL được quản lý theo ranh giới sở hữu của từng service. Các service có
> thể dùng chung một PostgreSQL instance khi triển khai, nhưng không truy cập trực
> tiếp bảng thuộc service khác. Nhờ đó, kiến trúc duy trì một điểm truy cập thống
> nhất trong khi vẫn bảo toàn ranh giới nghiệp vụ và dữ liệu.

### Điểm cần diễn đạt chính xác

- “Database per service” thể hiện ranh giới sở hữu dữ liệu về mặt logic.
- Các service có thể dùng chung một PostgreSQL instance trong mô hình triển khai
  hiện tại nhưng không đọc hoặc ghi bảng của nhau.

### Câu chuyển

> Trên khung kiến trúc này, các slide tiếp theo lần lượt phân tích bốn cơ chế: quản
> trị phạm vi module, cô lập tenant, kiểm soát quyền và mở rộng module tương đối
> độc lập.

---

## Slide 7 — Giải pháp 1 (M1): Quản trị phạm vi module

### Tiêu đề

> Account Service chuyển gói dịch vụ thành phạm vi module của từng doanh nghiệp

### Nội dung hiển thị

```text
Danh mục module
Module nào tồn tại?
        ↓
Module thuộc gói dịch vụ
Gói cung cấp những module nào?
        ↓
Đăng ký gói dịch vụ của doanh nghiệp
Doanh nghiệp đang sử dụng gói nào?
        ↓
Phạm vi module của doanh nghiệp
Những module nào được phép kích hoạt?
```

Tên dữ liệu cốt lõi tương ứng:

```text
modules
→ subscription_plan_modules
→ organization_subscriptions
```

### Nguyên tắc

- Module phải tồn tại trong danh mục và thuộc gói dịch vụ đã đăng ký.
- Doanh nghiệp chỉ kích hoạt module nằm trong phạm vi gói của mình.
- Quyền module của người dùng chỉ được cấp bên trong phạm vi của doanh nghiệp;
  cơ chế này được trình bày tại M3.

### Kết luận

> M1 trả lời doanh nghiệp được sử dụng module nào; chưa quyết định từng người
> dùng được thực hiện thao tác gì.

### Nội dung trình bày

> Trước hết, hệ thống cần xác định doanh nghiệp được sử dụng những module nào.
> Danh mục module cho biết nền tảng có thể cung cấp những module nào. Gói dịch vụ
> liên kết các module thành một phạm vi đăng ký, còn bản ghi đăng ký gói đang có
> hiệu lực xác định gói áp dụng cho từng doanh nghiệp. Từ các quan hệ này, Account
> Service xác định những module doanh nghiệp được phép kích hoạt. Như vậy, giải
> pháp này xác định phạm vi module ở cấp doanh nghiệp; quyền truy cập của từng
> người dùng trong phạm vi đó được xử lý riêng ở phần phân quyền.

### Câu chuyển

> Sau khi xác định doanh nghiệp được sử dụng module nào, hệ thống phải bảo đảm
> dữ liệu của doanh nghiệp đó không bị trộn với tenant khác.

---

## Slide 8 — Giải pháp 2 (M2): Thiết kế multi-tenant

### Tiêu đề

> Ngữ cảnh tenant giới hạn mọi dữ liệu nghiệp vụ trong đúng doanh nghiệp

### Nội dung hiển thị

```text
Người dùng thuộc doanh nghiệp
             ↓
Ngữ cảnh xác thực xác định tenantId
             ↓
Business Service tiếp nhận ngữ cảnh tenant
             ↓
Đọc và ghi dữ liệu trong phạm vi tenantId
```

### Ba nguyên tắc

1. Mỗi dữ liệu nghiệp vụ phải xác định được tenant sở hữu.
2. `tenantId` được lấy từ ngữ cảnh xác thực, không tin giá trị client tự khai báo.
3. Mọi thao tác đọc và ghi đều bị giới hạn theo tenant.

### Điều kiện kiểm chứng

```text
tenantId tài nguyên = tenantId ngữ cảnh
→ Chuyển sang kiểm tra quyền

tenantId tài nguyên ≠ tenantId ngữ cảnh
→ Từ chối truy cập
```

### Kết luận

> Multi-tenant trả lời “dữ liệu thuộc doanh nghiệp nào”, chưa trả lời “người
> dùng được làm gì với dữ liệu đó”.

### Nội dung trình bày

> Sau khi xác định phạm vi chức năng, hệ thống phải bảo vệ ranh giới dữ liệu giữa
> các doanh nghiệp. Mỗi request được xử lý trong ngữ cảnh một tổ chức đang hoạt
> động. Ngữ cảnh này phải được xác định từ danh tính đã xác thực và quan hệ thành
> viên hợp lệ, không lấy trực tiếp tenantId do client tự khai báo. Business service
> sử dụng tenantId đó để giới hạn mọi thao tác đọc, ghi và đối chiếu tenant sở hữu
> tài nguyên. Nếu tenant của tài nguyên không khớp với ngữ cảnh, request bị từ
> chối. Cơ chế này loại bỏ việc lựa chọn tenant khác chỉ bằng cách thay đổi tham
> số và tạo lớp cô lập dữ liệu trước khi kiểm tra quyền người dùng.

### Câu chuyển

> Trong đúng tenant, hệ thống tiếp tục xác định người dùng được thực hiện thao
> tác nào và trên tài nguyên nào.

---

## Slide 9 — Giải pháp 3 (M3): Xác thực và phân quyền nghiệp vụ

### Tiêu đề

> Service quyết định quyền từ danh tính, quyền module, thao tác và tài nguyên nghiệp vụ

### Nội dung hiển thị

```text
Client gửi JWT
       ↓
API Gateway kiểm tra token và định tuyến
       ↓
Business Service lấy từ security context:
userId · tenantId · roles
       ↓
Kiểm tra user_module_access
       ↓
Kiểm tra vai trò hoặc quyền đối với thao tác
       ↓
Kiểm tra quyền trên tài nguyên nghiệp vụ
       ↓
Thực hiện hoặc từ chối
```

### Bốn điểm kiểm soát

1. **Danh tính:** người dùng đã được xác thực chưa?
2. **Quyền module:** người dùng có được truy cập module trong phạm vi doanh nghiệp không?
3. **Quyền thao tác:** vai trò có cho phép thực hiện hành động được yêu cầu không?
4. **Quyền tài nguyên:** người dùng có quyền trên đối tượng nghiệp vụ cụ thể không?

### Phân chia trách nhiệm

| Thành phần | Trách nhiệm |
|---|---|
| Keycloak | Xác thực người dùng và cấp token |
| API Gateway | Kiểm tra token ở lớp biên và định tuyến |
| Account Service | Quản lý phạm vi module của tổ chức và người dùng |
| Business Service | Kiểm tra quyền module, thao tác và tài nguyên khi xử lý nghiệp vụ |

### Nội dung trình bày

> Trong đúng phạm vi tenant, hệ thống tiếp tục xác định người dùng được thực hiện
> thao tác nào. Quyền được kiểm tra theo bốn lớp: danh tính, quyền truy cập module,
> quyền thực hiện thao tác và quyền trên tài nguyên cụ thể. API Gateway loại bỏ
> token không hợp lệ tại lớp biên; Account Service quản lý phạm vi module, vai trò
> và quyền của người dùng; còn business service thực thi quyết định phân quyền tại
> thời điểm xử lý nghiệp vụ. Vì vậy, có quyền truy cập module Quản trị dự án chỉ
> cho phép người dùng đi vào module, chưa đồng nghĩa người đó được cập nhật mọi dự
> án. Cách phân lớp này giúp phân biệt quyền cấp ứng dụng với quyền trên từng đối
> tượng nghiệp vụ.

### Câu chuyển

> Ba giải pháp đầu xác định ba lớp phạm vi: module doanh nghiệp được sử dụng,
> tenant sở hữu dữ liệu và thao tác người dùng được thực hiện. Vấn đề tiếp theo là
> xác định ranh giới để các module có thể phát triển tương đối độc lập.

---

## Slide 10 — Giải pháp 4a (M4): Ranh giới module và dữ liệu

### Tiêu đề

> Mỗi miền nghiệp vụ có một service sở hữu logic, dữ liệu và vòng đời thay đổi

### Nội dung hiển thị

| Service | Miền nghiệp vụ và dữ liệu sở hữu |
|---|---|
| Account | Tổ chức, người dùng, module và quyền |
| Project Management | Dự án, work item, workflow và kế hoạch |
| Discuss | Kênh, thành viên và tin nhắn |
| CRM | Lead, customer, opportunity và activity |

### Nguyên tắc

- Mỗi service quản lý logic, dữ liệu và migration của mình.
- Service khác không đọc hoặc ghi trực tiếp bảng nội bộ.
- Trao đổi thông qua hợp đồng được xác định.

### Hệ quả

> Service có thể thay đổi, triển khai và mở rộng theo đặc điểm tải mà không buộc
> toàn bộ hệ thống thay đổi cùng lúc.

### Kết luận

> Ranh giới nghiệp vụ và quyền sở hữu dữ liệu tạo ra phần “độc lập” của M4; hợp
> đồng giao tiếp và frontend chung tạo ra phần “vận hành thống nhất”.

### Nội dung trình bày

> Khả năng phát triển độc lập bắt đầu từ ranh giới sở hữu. Mỗi service là nguồn dữ
> liệu có thẩm quyền đối với logic, dữ liệu và migration của miền nghiệp vụ mà nó
> phụ trách. Service khác không đọc hoặc ghi trực tiếp bảng nội bộ, mà phải trao
> đổi thông qua hợp đồng tích hợp được xác định rõ. Nhờ đó, từng module có thể thay
> đổi, triển khai hoặc mở rộng tương đối độc lập, với điều kiện vẫn duy trì các hợp
> đồng đã công bố. Hai slide tiếp theo trình bày cách các module phối hợp và cung
> cấp trải nghiệm thống nhất trên cùng nền tảng.

### Lưu ý khi trình bày

> Dùng chung một PostgreSQL instance là lựa chọn triển khai vật lý; không làm thay
> đổi quyền sở hữu dữ liệu về mặt logic của từng service.

### Câu chuyển

> Khi không chia sẻ trực tiếp dữ liệu, các service cần cơ chế giao tiếp phù hợp
> với từng loại tương tác.

---

## Slide 11 — Giải pháp 4b (M4): Giao tiếp qua hợp đồng

### Tiêu đề

> SERP chọn cơ chế giao tiếp theo đặc điểm của từng loại tương tác

### Nội dung hiển thị

| Cơ chế | Bản chất | Hợp đồng cần ổn định |
|---|---|---|
| **REST/HTTP** | Yêu cầu–phản hồi đồng bộ; bên gọi chờ kết quả | Method, path, schema request/response và status code |
| **Kafka** | Producer ghi record vào topic; broker lưu theo chính sách retention; consumer đọc bất đồng bộ và quản lý offset | Topic, key, loại sự kiện, schema/version, quy tắc phân vùng và giao nhận |
| **WebSocket + STOMP** | WebSocket duy trì kết nối lâu dài, hai chiều; hai phía có thể chủ động gửi dữ liệu. STOMP bổ sung destination và ngữ nghĩa send/subscribe | Endpoint, xác thực, destination và schema của frame/payload |

### Bố cục đề xuất

```text
REST — yêu cầu và phản hồi trực tiếp
Caller → HTTP request → Service → Response

Kafka — luồng sự kiện bất đồng bộ có lưu giữ
Producer → append → Topic (retention)
                           ↓
                    Consumer group(s)
Offset · phát lại trong retention · thứ tự trong từng partition

WebSocket/STOMP — phiên giao tiếp hai chiều
Client ⇄ kết nối WebSocket duy trì lâu dài ⇄ Server
STOMP: SEND /app/... · SUBSCRIBE /user/queue/...
```

### Điểm phân biệt

- **REST:** phù hợp khi bên gọi cần kết quả trực tiếp để tiếp tục xử lý.
- **Kafka:** producer không chờ consumer xử lý; record được lưu theo retention và
  có thể được đọc lại. Thứ tự chỉ được bảo đảm trong từng partition; record có thể
  được giao lại, nên consumer cần xử lý idempotent.
- **WebSocket:** trong SERP là kết nối client–server hai chiều, độ trễ thấp. Client
  có thể gửi lệnh và server có thể chủ động đẩy sự kiện trên cùng kết nối.
- **STOMP:** là giao thức nhắn tin chạy trên WebSocket, xác định destination,
  thao tác send/subscribe và cấu trúc thông điệp ở cấp ứng dụng.

WebSocket không tự cung cấp lịch sử bền vững hoặc phát lại sau khi mất kết nối.
Dữ liệu cần lưu giữ phải được ghi vào cơ sở dữ liệu trước khi phát sự kiện thời
gian thực. Với sự kiện Kafka phát sinh từ thay đổi dữ liệu, chỉ công bố sau khi
giao dịch nguồn commit; dùng outbox nếu cần bảo đảm chặt chẽ việc chuyển giao từ
cơ sở dữ liệu sang broker.

### Kết luận

> Kafka phục vụ tích hợp backend bất đồng bộ có lưu giữ; WebSocket phục vụ phiên
> tương tác hai chiều với client. Hai cơ chế bổ sung cho nhau và đều cần hợp đồng
> thông điệp rõ ràng.

### Nội dung trình bày

> Khi không chia sẻ trực tiếp dữ liệu, các thành phần phối hợp thông qua hợp đồng
> tích hợp. REST thực hiện mô hình yêu cầu–phản hồi đồng bộ, phù hợp khi bên gọi cần
> kết quả trực tiếp. Với Kafka, producer ghi sự kiện vào topic và không phải chờ
> consumer xử lý. Broker lưu record theo chính sách retention; mỗi consumer group
> theo dõi offset riêng và có thể đọc lại dữ liệu còn được lưu. Kafka chỉ bảo đảm
> thứ tự trong từng partition và một record có thể được giao lại, nên key, schema,
> version và khả năng xử lý idempotent là các phần quan trọng của hợp đồng. Ngược
> lại, WebSocket thiết lập một kết nối lâu dài và hai chiều: client và server đều
> có thể chủ động gửi dữ liệu trên cùng kết nối. Trong module Discuss, STOMP định
> nghĩa các destination để client gửi lệnh qua `/app/...` và đăng ký nhận sự kiện
> qua `/user/queue/...`. WebSocket không tự lưu lịch sử; tin nhắn phải được ghi nhận
> trước khi sự kiện được phát tới các client đang trực tuyến. Vì vậy, Kafka giải
> quyết tích hợp backend bất đồng bộ có lưu giữ, còn WebSocket giải quyết tương tác
> hai chiều, độ trễ thấp với người dùng.

### Câu chuyển

> Backend giữ ranh giới module; ở phía người dùng, frontend phải đồng thời giữ
> trải nghiệm thống nhất và phản ánh đúng các ranh giới đó.

---

## Slide 12 — Giải pháp 4c (M4): Trải nghiệm frontend thống nhất

### Tiêu đề

> Frontend dùng chung năng lực nền tảng nhưng tổ chức không gian theo module

### Nội dung hiển thị

```text
┌────────────── Khung frontend dùng chung ──────────────┐
│ Layout · Providers · Menu theo quyền · UI dùng chung  │
│                                                       │
│  Quản trị dự án   Trao đổi nội bộ   CRM   Module khác │
│  UI · Logic · API  UI · Logic · API  UI · Logic · API │
│                                                       │
│ Xác thực · Refresh token · Định tuyến · Xử lý lỗi     │
└──────────────────────────┬────────────────────────────┘
                           ↓
                      API Gateway
```

### Ba nguyên tắc

- Route, UI, state và API client được tổ chức theo từng module.
- Layout, xác thực, điều hướng và xử lý lỗi được dùng chung.
- Menu phản ánh quyền; backend thực thi quyết định phân quyền cuối cùng.

### Kết luận

> Người dùng làm việc trong một ứng dụng thống nhất, còn mã nguồn giao diện vẫn
> giữ ranh giới theo module.

### Nội dung trình bày

> Ở phía backend, các module được phân tách; còn ở phía người dùng, hệ thống vẫn
> phải cung cấp một trải nghiệm thống nhất. Frontend cung cấp một khung chung cho
> layout, xác thực, điều hướng, menu và xử lý lỗi. Bên trong khung đó, mỗi module
> vẫn sở hữu route, UI, state và API client của mình. Cách tổ chức này giúp người
> dùng làm việc trong một ứng dụng thống nhất, trong khi mã nguồn vẫn giữ ranh giới
> theo module. Menu và route guard chỉ phản ánh quyền để cải thiện trải nghiệm;
> chúng không phải ranh giới bảo mật. Business service vẫn là nơi thực thi quyết
> định phân quyền cuối cùng.

### Câu chuyển

> Sau khi trách nhiệm của từng thành phần đã rõ, các công nghệ có thể được ánh
> xạ vào đúng giải pháp mà chúng hiện thực hóa.

---

## Slide 13 — Công nghệ hiện thực giải pháp

### Tiêu đề

> Công nghệ được lựa chọn sau khi trách nhiệm thiết kế đã được xác định

### Nội dung hiển thị

| Trách nhiệm kỹ thuật | Công nghệ | Giải pháp được hỗ trợ |
|---|---|---|
| Định danh, token và ngữ cảnh người dùng | Keycloak, JWT | Multi-tenant; phân quyền |
| Logic nghiệp vụ, transaction và dữ liệu | Spring Boot, PostgreSQL | Quản trị module; multi-tenant; ranh giới module |
| Điểm truy cập thống nhất | Go, Gin | Phân quyền; nền tảng module dùng chung |
| Sự kiện và trạng thái truy cập nhanh | Kafka, Redis | Giao tiếp và mở rộng service theo tải |
| Trải nghiệm web theo module | Next.js, React, TypeScript | Quyền module; frontend thống nhất |
| Đóng gói và cập nhật từng service | Docker, Nginx, GitHub Actions | Triển khai độc lập và vận hành hệ thống |

### Lập luận lựa chọn microservices

> SERP lựa chọn microservices từ sự kết hợp của ba yêu cầu: ranh giới nghiệp vụ,
> vòng đời triển khai độc lập và khả năng mở rộng chọn lọc khi tải giữa các
> module không đồng đều.

Kiến trúc này làm tăng độ phức tạp về giao tiếp, dữ liệu và vận hành. Vì vậy,
việc lựa chọn microservices phải đi cùng Gateway, data ownership, hợp đồng giao
tiếp và quy trình triển khai rõ ràng.

### Nội dung trình bày

> Công nghệ không phải điểm xuất phát của thiết kế. Keycloak và JWT hiện thực
> định danh và ngữ cảnh người dùng. Spring Boot và PostgreSQL phục vụ logic,
> transaction và dữ liệu theo service. Go và Gin tạo điểm truy cập chung. Kafka
> và Redis hỗ trợ các nhu cầu tích hợp và trạng thái chuyên biệt. Next.js, React
> và TypeScript tạo khung frontend; Docker, Nginx và GitHub Actions hỗ trợ đóng
> gói và cập nhật từng service. Mỗi công nghệ vì vậy gắn với một trách nhiệm đã
> được xác định từ bốn giải pháp.

### Câu chuyển

> Các quyết định trên được tổng hợp thành một quy trình để tiếp nhận module mới
> vào SERP.

---

## Slide 14 — Quy trình tích hợp module mới

### Tiêu đề

> Một module mới được tích hợp vào SERP qua sáu bước có thể kiểm chứng

### Nội dung hiển thị

```text
1. Xác định ranh giới
Nghiệp vụ · Dữ liệu sở hữu · API/Event · Role
                    ↓
2. Xây dựng service
Logic · Migration · Tenant isolation · Authorization
                    ↓
3. Kết nối hạ tầng
Gateway route · REST/Kafka/WebSocket · Cấu hình triển khai
                    ↓
4. Đăng ký với nền tảng
Danh mục module · Keycloak client/role · Gói dịch vụ · Menu
                    ↓
5. Tích hợp frontend
Module UI · Route · API client · Điều hướng theo quyền
                    ↓
6. Kiểm thử và phát hành
Đúng quyền · Đúng tenant · API/Event · Health · Triển khai
```

Nên trình bày thành hai hàng, mỗi hàng ba bước, để người nghe quan sát được toàn
bộ quy trình mà không phải đọc một chuỗi dọc quá dài.

### Điều kiện hoàn tất

> Module chỉ được xem là tích hợp hoàn tất khi người dùng đúng tenant và đúng
> quyền có thể truy cập xuyên suốt từ menu đến service và dữ liệu; người dùng
> không có quyền hoặc khác tenant bị từ chối.

### Đối chiếu bốn mục tiêu

- Module được khai báo trong danh mục, gói dịch vụ và phạm vi tổ chức.
- Dữ liệu được giới hạn theo tenant.
- Vai trò, thao tác và tài nguyên nghiệp vụ được kiểm soát.
- Service sở hữu dữ liệu, có hợp đồng giao tiếp và có thể triển khai độc lập.

### Phân biệt hai quy trình

> **Tích hợp module** tạo khả năng cung cấp module trên SERP.  
> **Đăng ký gói/module** kích hoạt khả năng đó cho từng doanh nghiệp.

### Nội dung trình bày

> Slide này mô tả việc tích hợp kỹ thuật một module mới, khác với kích hoạt
> module đã có cho một doanh nghiệp. Trước hết, nhóm phát triển xác định ranh
> giới nghiệp vụ, dữ liệu sở hữu và hợp đồng API hoặc sự kiện. Tiếp theo, backend
> được xây dựng với cơ chế cô lập tenant và phân quyền nghiệp vụ tại
> service. API phục vụ frontend được định tuyến qua Gateway; Kafka hoặc WebSocket
> chỉ được bổ sung khi có nhu cầu giao tiếp bất đồng bộ hoặc tương tác hai chiều
> gần thời gian thực. Sau đó, module được đăng ký trong Account Service về danh
> mục, gói dịch vụ, quyền truy cập và menu; client và role tương ứng được cấu hình
> với Keycloak. Frontend bổ sung workspace, route và API client. Cuối cùng, kiểm
> thử xuyên suốt phải chứng minh người dùng đúng tenant, đúng quyền có thể truy
> cập, còn yêu cầu sai quyền hoặc khác tenant bị từ chối trước khi phát hành.
> Hiện các điểm nối này vẫn được cấu hình thủ công; giá trị của quy trình là
> chuẩn hóa trách nhiệm và tránh bỏ sót, chưa phải tự động hóa hoàn toàn.

### Câu chuyển sang kết quả

> Quy trình trên xác định các điều kiện để một module trở thành một phần của
> SERP. Phần tiếp theo đối chiếu các điều kiện này với hệ thống và các module đã
> được xây dựng.

---

## Slide 15 — Tổng hợp kết quả theo mục tiêu

### Tiêu đề

> Các kết quả đạt được đối chiếu trực tiếp với bốn mục tiêu của SERP

### Nội dung hiển thị

| Mục tiêu | Kết quả đạt được | Bằng chứng |
|---|---|---|
| Lựa chọn module theo nhu cầu | Xây dựng danh mục, gói dịch vụ và phạm vi module của từng tổ chức | Giao diện quản trị module SaaS |
| Vận hành SaaS multi-tenant | Duy trì ngữ cảnh tenant và giới hạn dữ liệu nghiệp vụ theo tổ chức | Xử lý tenant tại các service nghiệp vụ |
| Kiểm soát đúng người và đúng phạm vi | Quản lý quyền module, vai trò; kiểm tra thao tác và tài nguyên nghiệp vụ | Module access và kiểm thử phân quyền |
| Mở rộng hệ thống theo module | PM, Discuss, CRM và các hệ thống thành viên có nghiệp vụ riêng nhưng dùng chung năng lực nền tảng | Các module vận hành trong cùng SERP |

### Kết luận

> SERP đã hiện thực hóa khung nền tảng và các module minh chứng; chưa nhằm đạt độ
> bao phủ của một ERP thương mại hoàn chỉnh.

### Nội dung trình bày

> Phần kết quả được đánh giá theo đúng bốn mục tiêu đã đặt ra. Thứ nhất, hệ thống
> đã xây dựng cơ chế quản trị module từ danh mục và gói dịch vụ đến phạm vi sử
> dụng của tổ chức. Thứ hai, ngữ cảnh tenant được duy trì và dữ liệu nghiệp vụ
> được giới hạn theo tổ chức. Thứ ba, quyền module, vai trò, thao tác và tài
> nguyên nghiệp vụ được kiểm soát theo người dùng. Cuối cùng, PM, Discuss, CRM và
> các hệ thống thành viên giữ nghiệp vụ riêng nhưng cùng vận hành trên các năng
> lực nền tảng của SERP. Các slide tiếp theo trình bày bằng chứng theo hai chuỗi
> liên tục: từ gói dịch vụ đến workspace người dùng, sau đó từ dữ liệu nghiệp vụ
> đến các chức năng của từng module và khả năng mở rộng hệ sinh thái.

---

## Slide 16 — Xác lập phạm vi module của tổ chức

### Tiêu đề

> Gói dịch vụ xác định các module mà tổ chức có thể sử dụng

### Nội dung hiển thị

```text
Quản trị viên SERP cấu hình module trong gói
                        ↓
             Doanh nghiệp đăng ký gói
                        ↓
        Tổ chức được cấp các module tương ứng
```

### Hình sử dụng

1. `thesis/Hinhve/chapter4_ui_plan_modules.png`
2. `thesis/Hinhve/chapter4_ui_org_modules.png`

Đặt hai giao diện theo thứ tự trái sang phải:

1. **Định nghĩa phạm vi:** gói dịch vụ gồm những module nào.
2. **Kết quả cấp phát:** tổ chức được sử dụng những module nào.

### Kết luận

> SERP kiểm soát phạm vi chức năng ở cấp tổ chức, thay vì mặc định cung cấp toàn
> bộ module cho mọi doanh nghiệp.

### Nội dung trình bày

> Quản trị viên SERP cấu hình tập module thuộc từng gói dịch vụ. Khi doanh nghiệp
> đăng ký một gói, hệ thống xác định các module khả dụng với tổ chức tương ứng.
> Việc phê duyệt hiện còn thủ công vì hệ thống đang được cung cấp miễn phí, nhưng
> phạm vi sử dụng vẫn được quản lý theo mô hình SaaS. Kết quả của bước này là tập
> module mà tổ chức được quyền phân phối cho người dùng nội bộ.

### Câu chuyển

> Tổ chức có quyền sử dụng module chưa có nghĩa mọi thành viên đều được truy cập.

---

## Slide 17 — Cá nhân hóa workspace theo quyền người dùng

### Tiêu đề

> Quyền người dùng quyết định module xuất hiện trong workspace

### Nội dung hiển thị

```text
Module khả dụng với tổ chức
            ↓
Cấp quyền và role cho người dùng
            ↓
Workspace chỉ hiển thị module được phép truy cập
```

### Hình sử dụng

1. `thesis/Hinhve/chapter4_ui_module_access_dialog.png`
2. `thesis/Hinhve/chapter4_ui_modules_home.png`

Ảnh cấp quyền thể hiện thao tác; ảnh workspace thể hiện kết quả. Chỉ bổ sung hai
callout:

1. **Phạm vi truy cập:** module và role được cấp cho người dùng.
2. **Giao diện tương ứng:** workspace chỉ hiển thị module được phép sử dụng.

### Kết luận

> Phạm vi sử dụng được kiểm soát liên tiếp ở hai cấp: tổ chức và người dùng.

### Nội dung trình bày

> Trong tập module đã cấp cho tổ chức, quản trị viên tiếp tục xác định người dùng
> nào được truy cập và giữ role nào. Kết quả được phản ánh trực tiếp trên
> workspace: người dùng chỉ nhìn thấy các module thuộc phạm vi của tổ chức và đã
> được cấp cho chính mình. Hai slide 16 và 17 vì vậy chứng minh đầy đủ chuỗi từ
> gói dịch vụ đến không gian làm việc cá nhân.

### Câu chuyển

> Sau khi được cấp quyền, người dùng bắt đầu làm việc trong một module nghiệp vụ
> cụ thể — Quản trị dự án.

---

## Slide 18 — Khởi tạo phạm vi quản lý dự án

### Tiêu đề

> Dự án tạo ra phạm vi chung cho thành viên và công việc

### Nội dung hiển thị

```text
Tạo dự án
    ↓
Thiết lập thông tin và thành viên
    ↓
Hình thành không gian quản lý công việc
```

### Hình sử dụng

1. `thesis/Hinhve/chapter4_ui_project_list.png`
2. `thesis/Hinhve/chapter4_ui_project_overview.png`

Ảnh danh sách thể hiện dự án được tạo; ảnh tổng quan thể hiện không gian quản lý
sau khi truy cập. Chỉ callout:

1. Thông tin và trạng thái dự án.
2. Thành viên tham gia.
3. Phạm vi quản lý công việc.

### Kết luận

> Dự án là ranh giới nghiệp vụ để tổ chức thành viên, công việc và quyền truy cập.

### Nội dung trình bày

> Người dùng khởi tạo dự án và thiết lập các thông tin quản lý ban đầu. Khi dự án
> được tạo, hệ thống hình thành một phạm vi riêng cho thành viên, công việc và các
> thao tác liên quan. Đây là điểm bắt đầu của flow Quản trị dự án; bước tiếp theo
> là phân rã kế hoạch thành các work item có thể theo dõi.

### Câu chuyển

> Trong phạm vi dự án, kế hoạch được chuyển thành các work item cụ thể.

---

## Slide 19 — Tạo và tổ chức work item

### Tiêu đề

> Work item chuyển kế hoạch dự án thành công việc có thể theo dõi

### Nội dung hiển thị

```text
Nhập nội dung công việc
        ↓
Người phụ trách · Ưu tiên · Deadline · Dependency
        ↓
Work item xuất hiện trên workflow board
```

### Hình sử dụng

1. `thesis/Hinhve/chapter4_ui_create_work_item.png`
2. `thesis/Hinhve/chapter4_ui_work_item_board.png`

Ảnh tạo work item là đầu vào; ảnh board là kết quả. Làm nổi bật ba nhóm dữ liệu:

1. Người thực hiện và kỹ năng cần thiết.
2. Thời gian, deadline và mức ưu tiên.
3. Trạng thái và quan hệ phụ thuộc.

### Kết luận

> Work item vừa là đơn vị theo dõi tiến độ, vừa cung cấp dữ liệu đầu vào cho bài
> toán phân công và lập lịch.

### Nội dung trình bày

> Người quản lý tạo work item, mô tả nội dung, gắn người phụ trách, mức ưu tiên,
> deadline và quan hệ phụ thuộc. Sau khi tạo, công việc xuất hiện trên board và
> được theo dõi theo workflow. Những dữ liệu này không chỉ phục vụ quản lý tiến
> độ mà còn tạo thành đầu vào cho chức năng hỗ trợ phân công và lập lịch ở bước
> tiếp theo.

### Câu chuyển

> Từ tập work item và dữ liệu nguồn lực, người quản lý thiết lập bài toán cần hệ
> thống hỗ trợ.

---

## Slide 20 — Thiết lập bài toán phân công và lập lịch

### Tiêu đề

> Người quản lý xác định phạm vi và mục tiêu trước khi sinh phương án

### Nội dung hiển thị

```text
Work item · Kỹ năng · Năng lực · Lịch · Deadline · Dependency
                              ↓
                    Chọn phạm vi xử lý
                              ↓
                   Chọn mục tiêu ưu tiên
                              ↓
                   Chạy thuật toán tham lam
```

### Hình sử dụng

1. `thesis/Hinhve/chapter4_ui_optimization_scope.png`
2. `thesis/Hinhve/chapter4_ui_optimization_objective.png`

Hai ảnh trả lời hai câu hỏi liên tiếp:

1. **Phạm vi:** những work item và nguồn lực nào được đưa vào xử lý.
2. **Mục tiêu:** tiêu chí nào được ưu tiên khi đánh giá phương án.

### Kết luận

> Thuật toán không tự quyết định mục tiêu; phương án được sinh theo phạm vi và
> tiêu chí do người quản lý lựa chọn.

### Nội dung trình bày

> Hệ thống tổng hợp work item, kỹ năng, năng lực, lịch tài nguyên, deadline và
> dependency. Trước khi chạy, người quản lý chọn phạm vi công việc và mục tiêu ưu
> tiên. Thuật toán tham lam sử dụng các dữ liệu đó để sinh một phương án khả thi
> theo mục tiêu đã chọn. Việc tách rõ đầu vào, phạm vi và mục tiêu giúp kết quả có
> thể được giải thích và đánh giá ở bước sau.

### Câu chuyển

> Phương án được sinh ra không tự động thay đổi kế hoạch mà được chuyển sang bước
> rà soát.

---

## Slide 21 — Rà soát và áp dụng phương án

### Tiêu đề

> Phương án chỉ được áp dụng sau khi người quản lý rà soát

### Nội dung hiển thị

```text
Thuật toán sinh phương án
          ↓
So sánh hiện tại và đề xuất
          ↓
Điểm số · Lý do · Cảnh báo
          ↓
Accept · Reject · Override
          ↓
Áp dụng các thay đổi đã duyệt
```

### Hình sử dụng

1. `thesis/Hinhve/chapter4_ui_optimization_result.png`
2. `thesis/Hinhve/chapter4_ui_optimization_review_apply.png`
3. `thesis/Hinhve/chapter4_ui_optimization_warning.png` — dùng như một crop nhỏ
   nếu còn không gian.

Làm nổi bật ba điểm:

1. So sánh trạng thái hiện tại với đề xuất.
2. Hiển thị điểm số, lý do và cảnh báo ràng buộc.
3. Chấp nhận, từ chối hoặc điều chỉnh trước khi áp dụng.

### Kết luận

> Hệ thống hỗ trợ ra quyết định nhưng quyền kiểm soát cuối cùng vẫn thuộc về
> người quản lý; kết quả không được khẳng định là tối ưu toàn cục.

### Nội dung trình bày

> Kết quả được lưu thành Optimization Run, gồm trạng thái hiện tại, đề xuất,
> điểm số, lý do và cảnh báo. Người quản lý có thể chấp nhận, từ chối hoặc điều
> chỉnh từng thay đổi trước khi áp dụng. Vì vậy, thuật toán đóng vai trò hỗ trợ
> ra quyết định, không tự động thay đổi kế hoạch. Flow từ slide 18 đến 21 chứng
> minh PM vừa quản lý workflow nghiệp vụ, vừa có khả năng xử lý nâng cao ngoài
> các thao tác CRUD thông thường.

### Câu chuyển

> PM đại diện cho nghiệp vụ quản lý có workflow; module tiếp theo minh chứng một
> dạng xử lý khác — trao đổi theo thời gian thực.

---

## Slide 22 — Kết quả module Trao đổi nội bộ

### Tiêu đề

> Tin nhắn được kiểm soát theo thành viên và cập nhật theo thời gian thực

### Nội dung hiển thị

```text
Kiểm tra tư cách thành viên
            ↓
       Lưu tin nhắn
            ↓
Cập nhật tới người dùng trực tuyến
```

### Hình sử dụng

1. `thesis/Hinhve/chapter4_ui_discuss_channel_details.png`
2. `thesis/Hinhve/chapter4_ui_discuss_messages.png`

Ảnh thông tin kênh làm rõ phạm vi thành viên; ảnh hội thoại thể hiện kết quả gửi
và nhận. Chỉ đặt ba callout:

1. Thành viên hợp lệ của kênh.
2. Nội dung được lưu trước khi phát sự kiện.
3. Các phiên trực tuyến nhận cập nhật qua WebSocket.

### Kết luận

> Module có đặc tính thời gian thực vẫn sử dụng chung định danh, tenant và kiểm
> soát truy cập của SERP.

### Nội dung trình bày

> Discuss chứng minh một loại module khác với PM. Khi người dùng gửi tin nhắn,
> service kiểm tra tư cách thành viên và lưu nội dung trong transaction. Chỉ sau
> khi giao dịch hoàn tất, sự kiện mới được truyền qua Kafka và cập nhật tới các
> client bằng WebSocket. Module giữ quy tắc thành viên riêng nhưng dùng chung
> định danh, tenant và Gateway của nền tảng.

### Câu chuyển

> Sau PM và Discuss, CRM tiếp tục kiểm chứng khả năng đưa một miền nghiệp vụ mới
> vào cùng nền tảng.

---

## Slide 23 — Kết quả module CRM

### Tiêu đề

> CRM đưa một miền nghiệp vụ độc lập vào cùng nền tảng SERP

### Nội dung hiển thị

```text
Lead
  ↓
Customer và Contact
  ↓
Opportunity
  ↓
Pipeline bán hàng và Activity
```

### Hình sử dụng

1. `thesis/Hinhve/chapter4_ui_crm_lead_management.png`
2. `thesis/Hinhve/chapter4_ui_crm_opportunity_pipeline.png`

Đặt hai ảnh theo flow từ đầu mối đến cơ hội bán hàng. Chỉ đặt hai callout:

1. **Nghiệp vụ riêng:** lead, khách hàng, cơ hội và pipeline.
2. **Nền tảng dùng chung:** tenant, Gateway, quyền và frontend shell.

### Kết luận

> CRM giữ logic và dữ liệu riêng nhưng tham gia SERP qua cùng các hợp đồng nền
> tảng như các module trước.

### Nội dung trình bày

> CRM mở rộng phạm vi minh chứng sang miền quản lý quan hệ khách hàng. Lead được
> quản lý và phát triển thành khách hàng, liên hệ và cơ hội trong pipeline bán
> hàng. Toàn bộ logic và dữ liệu CRM nằm trong service riêng, trong khi định danh,
> tenant, Gateway, quyền và frontend shell được dùng chung. Kết quả này chứng minh
> chuỗi tích hợp ở Slide 14 có thể áp dụng cho một miền nghiệp vụ mới.

### Câu chuyển

> Khả năng mở rộng không chỉ được kiểm chứng bằng các module trình bày chi tiết mà
> còn qua các hệ thống do những thành viên khác phát triển.

---

## Slide 24 — Kết quả tích hợp các hệ thống thành viên

### Tiêu đề

> Nhiều nhóm phát triển đã tích hợp các miền nghiệp vụ khác vào khung SERP

### Nội dung hiển thị

```text
                              KHUNG SERP
             Định danh · Gateway · Tenant · Module access
                    Frontend shell · Triển khai chung
                                  │
              ┌───────────┬───────┴───────┬────────────┐
              ↓           ↓               ↓            ↓
             WMS         TMS        School Bus     Lập lịch vận tải
```

| Hệ thống | Miền nghiệp vụ | Thành viên thực hiện |
|---|---|---|
| WMS | Mua hàng, bán hàng, quản lý kho và giao vận | Phạm Trung Kiên |
| TMS | Quản lý giao vận qua ba chặng vận chuyển | Nguyễn Thế Anh |
| School Bus Operations | Quản lý hoạt động vận chuyển học sinh | Nguyễn Thế Anh |
| Lập lịch vận tải container | Hỗ trợ lập lịch xe đầu kéo–rơ mooc | Trần Ngọc Hưng |

### Bố cục đề xuất

Đặt **Khung SERP** làm trục chung ở phía trên hoặc trung tâm; bốn hệ thống thành
viên là bốn nhánh cùng cấp. Không trình diễn chi tiết giao diện hoặc chức năng
của từng hệ thống trên slide này.

### Kết luận

> Khả năng mở rộng module đã được kiểm chứng qua nhiều miền nghiệp vụ và nhiều
> nhóm phát triển độc lập.

### Nội dung trình bày

> Bên cạnh PM, Discuss và CRM, SERP còn tiếp nhận các hệ thống thuộc kho vận, vận
> tải và điều phối phương tiện do những thành viên khác phát triển. Các kết quả
> này không được xem là phần hiện thực hóa cá nhân của tôi. Chúng là bằng chứng
> độc lập rằng khung SERP có thể hỗ trợ nhiều nhóm phát triển tích hợp các miền
> nghiệp vụ khác nhau vào một nền tảng chung.

### Câu chuyển

> Các giao diện trên chứng minh chức năng và khả năng mở rộng module; kết quả tiếp
> theo cho thấy toàn bộ hệ thống đã được đóng gói và vận hành end-to-end.

---

## Slide 25 — Mô hình triển khai thực tế

### Tiêu đề

> SERP đã được đóng gói và vận hành end-to-end trên máy chủ Ubuntu

### Nội dung hiển thị

```text
GitHub Actions
Build · Test · Docker Image
          ↓
      Docker Hub
          ↓
Máy chủ Ubuntu — Docker Compose
┌─────────────────────────────────────┐
│ Internet → Nginx                    │
│              ├─ Frontend            │
│              ├─ API Gateway         │
│              └─ Keycloak            │
│                     ↓               │
│ PM · Discuss · CRM · Account        │
│                     ↓               │
│ PostgreSQL · Kafka · Redis · MinIO  │
│ Persistent Volumes                  │
└─────────────────────────────────────┘
```

### Kết quả đạt được

- Tách điểm truy cập cho frontend, API Gateway và Keycloak.
- Đóng gói và cập nhật các service bằng container.
- Giữ dữ liệu ngoài vòng đời container bằng persistent volume.
- Pipeline phát hiện service thay đổi và cập nhật thành phần liên quan.

### Giới hạn

> Môi trường hiện tại triển khai trên một node; chưa chứng minh khả năng chịu lỗi
> hoặc mở rộng ngang trên nhiều máy chủ.

### Nội dung trình bày

> SERP đã được triển khai trên máy chủ Ubuntu tự vận hành. Nginx tiếp nhận HTTPS
> và phân luồng tới frontend, API Gateway và Keycloak. Các service cùng hạ tầng
> được đóng gói bằng Docker và vận hành qua Docker Compose; persistent volume
> giữ dữ liệu khi container được thay thế. GitHub Actions phát hiện service thay
> đổi, thực hiện build, kiểm tra, phát hành image và cập nhật container tương ứng.
> Kết quả này chứng minh hệ thống có thể vận hành end-to-end, nhưng mô hình hiện
> tại vẫn chỉ gồm một node.

### Câu chuyển

> Bên cạnh việc triển khai được hệ thống, kết quả cần được đánh giá bằng các kiểm
> thử phù hợp với những rủi ro chính của nền tảng và module nghiệp vụ.

---

## Slide 26 — Kết quả kiểm thử

> **Để trống theo yêu cầu — người trình bày tự bổ sung nội dung.**

---

## Slide 27 — Mở rộng năng lực vận hành

### Tiêu đề

> Từ triển khai một node đến hệ thống phân tán có thể đo lường

### Điểm xuất phát và câu hỏi nghiên cứu

| Điểm xuất phát | Câu hỏi nghiên cứu |
|---|---|
| SERP đang vận hành end-to-end bằng Docker Compose trên một máy chủ | Khi tải tăng hoặc một instance gặp lỗi, hệ thống mở rộng và phục hồi như thế nào? |

### Giải pháp kỹ thuật

```text
Điều phối container đa node
        ↓
Nhân bản stateless service · Load balancing · Health check
        ↓
Mô hình sẵn sàng cao cho các thành phần stateful
        ↓
Logging · Metrics · Distributed tracing · Alerting
```

- Nhân bản Gateway và các business service để mở rộng ngang theo tải.
- Nghiên cứu mô hình sẵn sàng cao cho PostgreSQL, Kafka, Redis và Keycloak.
- Quản lý tập trung cấu hình, secret và quá trình cập nhật service.
- Liên kết log, metric và trace để quan sát xuyên suốt từ Gateway đến hạ tầng.

Kubernetes là công nghệ ứng viên cho điều phối container đa node, không phải
mục tiêu nghiên cứu tự thân.

### Phương pháp và tiêu chí đánh giá

| Phương pháp | Đại lượng đánh giá |
|---|---|
| Stress test | Throughput, p95 latency và tỷ lệ lỗi theo mức tải |
| Soak test | Độ ổn định và mức sử dụng tài nguyên trong thời gian dài |
| Failure injection | Tỷ lệ request thành công khi một instance bị dừng |
| Quan sát sự cố | MTTD, MTTR và khả năng truy vết request xuyên service |

### Kết luận

> SERP không chỉ cần chạy trên nhiều node mà phải chứng minh được khả năng mở
> rộng, phát hiện và phục hồi sau sự cố bằng các số liệu đo được.

### Nội dung trình bày

> Slide 25 cho thấy SERP đã vận hành end-to-end nhưng mới trên một máy chủ. Vì
> vậy, câu hỏi tiếp theo không chỉ là có thể chuyển sang nhiều node hay không,
> mà là hệ thống mở rộng và phục hồi như thế nào khi tải tăng hoặc một instance
> gặp lỗi. Hướng nghiên cứu gồm điều phối container đa node, nhân bản các thành
> phần stateless và xây dựng mô hình sẵn sàng cao cho các thành phần stateful.
> Logging, metrics, distributed tracing và cảnh báo được triển khai song song để
> quan sát hệ thống. Kết quả được kiểm chứng bằng stress test, soak test và
> failure injection trên các đại lượng về hiệu năng, tính sẵn sàng và thời gian
> phục hồi.

### Câu chuyển

> Năng lực vận hành giải quyết việc mở rộng hệ thống khi số người dùng tăng.
> Hướng phát triển tiếp theo giải quyết việc mở rộng hệ thống khi số lượng module
> và nhóm phát triển tăng.

---

## Slide 28 — Mở rộng năng lực phát triển module

### Tiêu đề

> Chuẩn hóa hợp đồng tích hợp module

### Điểm xuất phát và câu hỏi nghiên cứu

| Điểm xuất phát | Câu hỏi nghiên cứu |
|---|---|
| Quy trình sáu bước đã xác định cách tích hợp module nhưng còn cấu hình thủ công tại nhiều thành phần | Làm thế nào để tích hợp và nâng cấp module mà không sửa phần lõi hoặc module không liên quan? |

### Giải pháp kỹ thuật

```text
Module Manifest có phiên bản
Identity · Dependencies · Permissions · Navigation
OpenAPI · Event schemas · Health endpoint
                    ↓
Module SDK/CLI
Sinh khung · Mẫu tích hợp nền tảng · Cấu hình ban đầu
                    ↓
CI/CD Validation
Schema · Dependency · Compatibility · Contract test
                    ↓
Phát hành, nâng cấp và rollback theo phiên bản
```

- Manifest mô tả thống nhất danh tính, dependency và các hợp đồng của module.
- SDK/CLI sử dụng manifest để sinh khung service, frontend và cấu hình tích hợp
  ban đầu.
- CI kiểm tra schema, dependency, tương thích nền tảng và contract trước khi phát
  hành.
- Pipeline quản lý migration, nâng cấp và rollback theo phiên bản module.

### Tiêu chí đánh giá

| Thuộc tính cần chứng minh | Chỉ số đánh giá |
|---|---|
| Tính độc lập | Module mới không yêu cầu sửa mã nguồn lõi hoặc module không liên quan |
| Phát hiện sớm | Lỗi schema, dependency và contract được phát hiện trước triển khai |
| Khả năng lặp lại | Số điểm cấu hình thủ công và thời gian tích hợp module giảm |
| Khả năng nâng cấp | Contract test vẫn đạt khi thay đổi phiên bản module |

### Kết luận

> Chuẩn hóa hợp đồng giúp module mới được tích hợp, kiểm tra và nâng cấp mà không
> làm tăng phụ thuộc giữa các thành phần.

### Nội dung trình bày

> Hướng phát triển này xuất phát từ quy trình tích hợp module ở Slide 14. Quy
> trình hiện đã xác định đầy đủ các bước nhưng vẫn phải cấu hình tại nhiều thành
> phần. SERP sẽ mô tả module bằng một manifest có phiên bản, bao gồm dependency,
> quyền, điều hướng, API và event contract. SDK hoặc CLI sử dụng manifest để sinh
> khung và cấu hình ban đầu; CI kiểm tra tính hợp lệ và tương thích trước khi phát
> hành. Hiệu quả được đánh giá bằng mức giảm thao tác thủ công, khả năng phát hiện
> lỗi trước triển khai và khả năng nâng cấp mà không phá vỡ contract.

### Kết luận phần hướng phát triển

> SERP được phát triển theo hai chiều: vận hành tin cậy khi quy mô sử dụng tăng và
> tích hợp có kiểm soát khi hệ sinh thái module mở rộng.
