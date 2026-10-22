# PROMPT: XÂY DỰNG GIAO DIỆN HỆ THỐNG ĐĂNG KÝ MÔN HỌC (JAVA SPRING BOOT)

> Cách dùng: dán toàn bộ nội dung này vào công cụ AI lập trình. Nếu công cụ báo quá dài hoặc làm thiếu, hãy dán **Phần 0 đến Phần 3** trước, sau đó yêu cầu làm lần lượt từng nhóm màn hình ở **Phần 4** (ví dụ: "Làm tiếp nhóm màn hình Quản trị viên, mục 4.3").

---

## PHẦN 0. VAI TRÒ VÀ MỤC TIÊU

Bạn là lập trình viên Java Spring Boot có kinh nghiệm. Hãy xây dựng **hoàn chỉnh** ứng dụng web "Hệ thống đăng ký môn học" quy mô nhỏ cho đồ án sinh viên, gồm backend và giao diện, chạy được ngay bằng `mvn spring-boot:run`.

Yêu cầu chung:
- Viết **đầy đủ code**, không để `// TODO`, không bỏ qua file nào. Mọi màn hình liệt kê ở Phần 4 phải có controller, service, template và chạy được.
- Toàn bộ giao diện, thông báo, nhãn nút bằng **tiếng Việt có dấu**.
- Code sạch, chia lớp rõ ràng: `entity` → `repository` → `service` → `controller` → `templates`.
- Kiểm tra quy định nghiệp vụ ở **tầng service** (không chỉ ở giao diện).

---

## PHẦN 1. CÔNG NGHỆ VÀ CẤU TRÚC DỰ ÁN

### 1.1. Công nghệ bắt buộc
| Thành phần | Lựa chọn |
|---|---|
| Ngôn ngữ | Java 17 |
| Framework | Spring Boot 3.x |
| Build | Maven |
| Giao diện | Thymeleaf + Thymeleaf Layout Dialect + Bootstrap 5.3 + Bootstrap Icons |
| Bảo mật | Spring Security 6 (form login, phân quyền theo vai trò) |
| Truy cập dữ liệu | Spring Data JPA (Hibernate) |
| Cơ sở dữ liệu | MySQL 8 (ghi chú cách đổi sang SQL Server trong README) |
| Kiểm tra dữ liệu | Jakarta Bean Validation (`@NotBlank`, `@Size`, `@Min`, `@Max`…) |
| Tiện ích | Lombok |
| Mã hóa mật khẩu | `BCryptPasswordEncoder` |

### 1.2. Cấu trúc thư mục
```
src/main/java/com/dangkymonhoc/
├── DangKyMonHocApplication.java
├── config/          (SecurityConfig, WebConfig, DataSeeder)
├── entity/          (Khoa, Lop, SinhVien, GiangVien, MonHoc, DangKy, DangKyId,
│                     PhanCong, PhanCongId, Quyen, TaiKhoan)
├── repository/
├── service/         (mỗi nghiệp vụ một service; ném BusinessException khi vi phạm quy định)
├── controller/
│   ├── AuthController, HomeController
│   ├── admin/       (AdminDashboard, Khoa, Lop, SinhVien, GiangVien, MonHoc, PhanCong, TaiKhoan)
│   ├── sv/          (SvHome, DangKy, KetQua)
│   └── gv/          (GvHome, NhapDiem)
├── dto/             (form object cho từng màn hình)
├── exception/       (BusinessException, GlobalExceptionHandler)
└── security/        (CustomUserDetails, CustomUserDetailsService)

src/main/resources/
├── application.properties
├── static/css/app.css, static/js/app.js
└── templates/
    ├── layout/      (main.html, auth.html)
    ├── fragments/   (sidebar.html, header.html, alerts.html, pagination.html, confirm-modal.html)
    ├── auth/        (login.html, doi-mat-khau.html)
    ├── error/       (403.html, 404.html, 500.html)
    ├── admin/ , sv/ , gv/
```

### 1.3. Cấu hình `application.properties`
```
spring.datasource.url=jdbc:mysql://localhost:3306/dang_ky_mon_hoc?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
server.servlet.session.timeout=30m

app.hoc-ky-hien-tai=HK1-2026
app.tin-chi-toi-da=24
app.page-size=10
```
Đọc `app.*` bằng `@ConfigurationProperties` hoặc `@Value`.

---

## PHẦN 2. CƠ SỞ DỮ LIỆU VÀ QUY ĐỊNH NGHIỆP VỤ

### 2.1. Chín bảng (giữ đúng tên bảng và tên cột)
| Bảng | Cột (PK gạch chân ý nghĩa, FK ghi rõ) |
|---|---|
| KHOA | MaKhoa varchar(10) PK, TenKhoa nvarchar(100) NOT NULL |
| LOP | MaLop varchar(10) PK, TenLop nvarchar(50) NOT NULL, KhoaHoc varchar(10), MaKhoa FK→KHOA |
| SINH_VIEN | MaSV varchar(10) PK, HoTen nvarchar(50) NOT NULL, NgaySinh date, GioiTinh nvarchar(5) (Nam/Nữ), MaLop FK→LOP |
| GIANG_VIEN | MaGV varchar(10) PK, HoTen nvarchar(50) NOT NULL, HocVi nvarchar(30), MaKhoa FK→KHOA |
| MON_HOC | MaMH varchar(10) PK, TenMH nvarchar(100) NOT NULL, SoTinChi int (1–6), MaKhoa FK→KHOA |
| DANG_KY | **PK (MaSV, MaMH, HocKy)**, MaSV FK→SINH_VIEN, MaMH FK→MON_HOC, HocKy varchar(10), NgayDangKy date, Diem float NULL (0–10) |
| PHAN_CONG | **PK (MaGV, MaMH, HocKy)**, MaGV FK→GIANG_VIEN, MaMH FK→MON_HOC, HocKy varchar(10) |
| QUYEN | MaQuyen varchar(10) PK (ADMIN, GV, SV), TenQuyen nvarchar(50) |
| TAI_KHOAN | TenDangNhap varchar(30) PK, MatKhau varchar(255) (BCrypt), TrangThai bit DEFAULT 1, MaQuyen FK→QUYEN, MaSV FK→SINH_VIEN NULL UNIQUE, MaGV FK→GIANG_VIEN NULL UNIQUE |

Dùng `@EmbeddedId` cho `DangKyId` và `PhanCongId`. Dùng `@Table(name = "...")` và `@Column(name = "...")` để giữ đúng tên.

### 2.2. Quy định nghiệp vụ phải kiểm tra trong service
| Mã | Quy định | Thông báo lỗi hiển thị |
|---|---|---|
| QĐ3 | Chỉ đăng ký môn đã được mở trong học kỳ (có trong PHAN_CONG) | "Môn học này chưa được mở trong học kỳ." |
| QĐ4 | Không đăng ký một môn hai lần trong cùng học kỳ | "Bạn đã đăng ký môn {TenMH} trong học kỳ này." |
| QĐ5 | Tổng tín chỉ trong học kỳ ≤ `app.tin-chi-toi-da` | "Vượt quá {24} tín chỉ. Hiện có {x} tín chỉ, môn này {y} tín chỉ." |
| QĐ6 | Chỉ hủy đăng ký khi Diem còn NULL | "Môn đã có điểm, không thể hủy." |
| QĐ7 | Điểm 0–10; chỉ GV được phân công môn đó trong học kỳ mới nhập | "Điểm phải từ 0 đến 10." / "Bạn không được phân công dạy môn này." |
| QĐ8 | Số tín chỉ 1–6 | "Số tín chỉ phải từ 1 đến 6." |
| QĐ9 | Tài khoản bị khóa không đăng nhập được | "Tài khoản đã bị khóa. Liên hệ phòng đào tạo." |
| Xóa | Không xóa bản ghi đang được tham chiếu (khoa còn lớp, lớp còn SV, môn đã có đăng ký…) | "Không thể xóa: {đối tượng} đang được sử dụng." |
| Trùng mã | Thêm mới với mã đã tồn tại | "Mã {X} đã tồn tại." |

### 2.3. Dữ liệu mẫu (`DataSeeder`, chỉ chạy khi bảng QUYEN rỗng)
- 3 quyền: ADMIN "Quản trị viên", GV "Giảng viên", SV "Sinh viên".
- 2 khoa (CNTT, KT), 3 lớp, 10 sinh viên, 4 giảng viên, 8 môn học (2–4 tín chỉ).
- Phân công 6 môn cho học kỳ `HK1-2026`.
- Tài khoản: `admin / admin123` (ADMIN); `gv01 / 123456` gắn GV01; `sv01 / 123456` gắn SV01; mỗi SV, GV còn lại có tài khoản là mã của họ viết thường, mật khẩu `123456`.
- SV01 đã đăng ký sẵn 2 môn, trong đó 1 môn đã có điểm (để thử QĐ6).

---

## PHẦN 3. PHONG CÁCH GIAO DIỆN CHUẨN (ÁP DỤNG CHO MỌI MÀN HÌNH)

### 3.1. Bộ nhận diện
- Font: **"Be Vietnam Pro"** từ Google Fonts (hỗ trợ tiếng Việt tốt), cỡ chữ gốc 15px.
- Màu chính `#1F3F60` (xanh đậm), màu nhấn `#2F80ED`, nền trang `#F5F7FA`, nền thẻ `#FFFFFF`, viền `#E3E8EF`.
- Màu trạng thái dùng chuẩn Bootstrap: thành công xanh lá, cảnh báo vàng, lỗi đỏ.
- Bo góc thẻ 12px, nút và ô nhập 8px; đổ bóng nhẹ `0 1px 3px rgba(0,0,0,.06)`.
- Khai báo màu bằng biến CSS trong `app.css` (`--primary`, `--accent`…) và ghi đè biến Bootstrap tương ứng.

### 3.2. Bố cục chung (`layout/main.html`)
```
┌───────────────┬─────────────────────────────────────────────┐
│               │ HEADER: tiêu đề trang · Học kỳ: HK1-2026 ·   │
│   SIDEBAR     │         [avatar] Họ tên (Vai trò) ▾          │
│   (260px)     ├─────────────────────────────────────────────┤
│   Logo + tên  │ Breadcrumb: Trang chủ / Quản lý / Môn học    │
│   hệ thống    │ ┌─────────────────────────────────────────┐ │
│   Menu theo   │ │ Thông báo flash (thành công / lỗi)      │ │
│   vai trò     │ └─────────────────────────────────────────┘ │
│               │ NỘI DUNG TRANG                              │
│               │                                             │
└───────────────┴─────────────────────────────────────────────┘
```
- **Sidebar** nền `#1F3F60`, chữ trắng; mục đang chọn có nền sáng hơn và vạch trái màu nhấn. Menu hiển thị theo vai trò bằng `sec:authorize`:
  - ADMIN: Tổng quan · Khoa · Lớp · Sinh viên · Giảng viên · Môn học · Phân công giảng dạy · Tài khoản
  - SV: Trang chủ · Đăng ký môn học · Kết quả học tập
  - GV: Trang chủ · Môn được phân công
  - Cuối sidebar (mọi vai trò): Đổi mật khẩu · Đăng xuất
- **Header** nền trắng, cao 64px. Dropdown người dùng gồm: Đổi mật khẩu, Đăng xuất (POST `/logout` có CSRF).
- **Responsive**: dưới 992px, sidebar thu thành offcanvas mở bằng nút ☰ trên header. Bảng bọc trong `.table-responsive`.

### 3.3. Thành phần dùng chung
- **Thông báo flash** (`fragments/alerts.html`): đọc `successMessage` / `errorMessage` từ `RedirectAttributes`; dạng `alert alert-dismissible` có icon, tự ẩn sau 4 giây (JS).
- **Thanh công cụ danh sách**: bên trái ô tìm kiếm (icon kính lúp, placeholder "Tìm theo mã hoặc tên…") và các bộ lọc dạng select; bên phải nút chính "**+ Thêm mới**" (`btn-primary`).
- **Bảng dữ liệu**: `table table-hover align-middle`, tiêu đề nền `#F1F4F8`; cột đầu là STT; cột cuối "Thao tác" căn phải gồm nút icon Sửa (`bi-pencil`, outline primary) và Xóa (`bi-trash`, outline danger), có `title` mô tả.
- **Trạng thái rỗng**: khi không có dữ liệu, thay bảng bằng khối căn giữa gồm icon lớn màu xám, dòng "Chưa có dữ liệu" và nút "+ Thêm mới".
- **Phân trang** (`fragments/pagination.html`): dùng `Pageable` của Spring Data, hiển thị "Hiển thị 1–10 trên 45 bản ghi" và nút trang; giữ nguyên tham số tìm kiếm khi chuyển trang.
- **Form thêm/sửa**: hiển thị trong **modal Bootstrap** cho danh mục ít trường (Khoa, Lớp, Môn học), trang riêng cho danh mục nhiều trường (Sinh viên, Giảng viên, Tài khoản). Nhãn đặt trên ô nhập, trường bắt buộc có dấu `*` đỏ. Lỗi validation hiển thị ngay dưới ô (`is-invalid` + `invalid-feedback`) bằng `th:errors`. Khi sửa, ô mã (khóa chính) ở chế độ `readonly`. Nút: "Lưu" (primary) và "Hủy" (secondary).
- **Xác nhận xóa/hủy** (`fragments/confirm-modal.html`): modal chung, nội dung "Bạn có chắc muốn xóa {tên đối tượng} **{mã – tên}**?", nút "Xóa" đỏ. Thao tác xóa gửi bằng form POST có CSRF, không dùng link GET.
- **Mẫu PRG**: mọi POST thành công đều `redirect:` về trang danh sách kèm flash message.
- **Định dạng**: ngày `dd/MM/yyyy`; điểm hiển thị 1 chữ số thập phân; điểm rỗng hiển thị "—".

### 3.4. Phân quyền URL
| URL | Quyền |
|---|---|
| `/login`, `/css/**`, `/js/**`, `/error/**` | Mọi người |
| `/admin/**` | ROLE_ADMIN |
| `/gv/**` | ROLE_GV |
| `/sv/**` | ROLE_SV |
| `/doi-mat-khau` | Đã đăng nhập |

Sau đăng nhập chuyển hướng theo vai trò: ADMIN → `/admin`, GV → `/gv`, SV → `/sv` (viết `AuthenticationSuccessHandler`). Truy cập sai quyền → trang 403.

---

## PHẦN 4. ĐẶC TẢ TỪNG MÀN HÌNH

### 4.1. Nhóm màn hình chung

#### MH01. Đăng nhập — `GET/POST /login` (UC01)
- Bố cục `layout/auth.html`: chia đôi màn hình. Bên trái (ẩn trên điện thoại) nền `#1F3F60`, tên hệ thống "Hệ thống đăng ký môn học" cỡ lớn, một câu mô tả ngắn và minh họa đơn giản bằng icon. Bên phải là thẻ đăng nhập rộng tối đa 400px, căn giữa dọc.
- Thẻ gồm: tiêu đề "Đăng nhập", ô **Tên đăng nhập** (icon người), ô **Mật khẩu** (icon khóa, nút con mắt ẩn/hiện mật khẩu), checkbox "Ghi nhớ đăng nhập" (remember-me), nút "Đăng nhập" rộng hết thẻ.
- Thông báo dưới tiêu đề:
  - `?error`: "Sai tên đăng nhập hoặc mật khẩu." (đỏ)
  - Tài khoản khóa (`DisabledException`): "Tài khoản đã bị khóa. Liên hệ phòng đào tạo." (đỏ)
  - `?logout`: "Bạn đã đăng xuất." (xanh)
- Dưới thẻ, chữ nhỏ màu xám: "Tài khoản dùng thử: admin/admin123 · gv01/123456 · sv01/123456" (chỉ hiển thị khi profile `dev`).

#### MH02. Đổi mật khẩu — `GET/POST /doi-mat-khau` (UC02)
- Thẻ rộng 480px trong layout chính. Ba ô: Mật khẩu hiện tại, Mật khẩu mới, Nhập lại mật khẩu mới (đều có nút ẩn/hiện).
- Kiểm tra: mật khẩu hiện tại đúng; mật khẩu mới tối thiểu 6 ký tự, khác mật khẩu cũ; hai ô mới trùng nhau. Lỗi hiển thị dưới từng ô.
- Thành công: flash "Đổi mật khẩu thành công." và ở lại trang.

#### MH03–MH05. Trang lỗi 403, 404, 500
- Căn giữa: mã lỗi cỡ rất lớn màu nhạt, một câu giải thích ("Bạn không có quyền truy cập trang này." / "Không tìm thấy trang." / "Đã xảy ra lỗi, vui lòng thử lại."), nút "Về trang chủ" (dẫn theo vai trò).

---

### 4.2. Nhóm màn hình Sinh viên

#### MH06. Trang chủ sinh viên — `GET /sv`
- Lời chào "Xin chào, {HoTen}" và dòng phụ "Mã SV: {MaSV} · Lớp: {TenLop} · Khoa: {TenKhoa}".
- Hàng 3 thẻ thống kê (icon + số lớn + nhãn): **Số môn đã đăng ký** (học kỳ hiện tại) · **Tổng tín chỉ / 24** (kèm thanh tiến độ, chuyển vàng khi ≥ 20, đỏ khi đạt 24) · **Số môn đã có điểm**.
- Hai nút lối tắt lớn: "Đăng ký môn học" và "Xem kết quả học tập".

#### MH07. Đăng ký môn học — `GET /sv/dang-ky?hocKy=` (UC03, UC04, UC05, UC06) — **MÀN HÌNH QUAN TRỌNG NHẤT**
- **Thanh trên cùng**: select "Học kỳ" (mặc định học kỳ hiện tại, đổi giá trị thì tự submit). Bên phải là **ô tổng tín chỉ** nổi bật: "Đã đăng ký: **x / 24** tín chỉ" với thanh tiến độ cùng quy tắc màu như MH06.
- **Khối 1 – Môn học được mở** (thẻ có tiêu đề và ô tìm kiếm nhanh lọc bằng JS phía client). Bảng cột: STT · Mã môn · Tên môn · Số TC · Khoa phụ trách · Giảng viên (nhiều GV nối bằng dấu phẩy) · Thao tác.
  - Nút "Đăng ký" (`btn-sm btn-primary`, icon `bi-plus-circle`) gửi POST `/sv/dang-ky` với `maMH`, `hocKy`.
  - Môn **đã đăng ký**: thay nút bằng badge xanh "Đã đăng ký", cả dòng nền xanh rất nhạt.
  - Môn mà nếu thêm sẽ **vượt tín chỉ**: nút bị vô hiệu hóa, có tooltip "Vượt quá số tín chỉ tối đa". Service vẫn phải kiểm tra lại QĐ5 khi nhận POST.
- **Khối 2 – Môn đã đăng ký trong học kỳ**. Bảng cột: STT · Mã môn · Tên môn · Số TC · Ngày đăng ký · Điểm · Thao tác. Dòng cuối bảng in đậm: "Tổng cộng: x tín chỉ".
  - Nút "Hủy" (`btn-sm btn-outline-danger`, icon `bi-x-circle`) mở modal xác nhận "Hủy đăng ký môn **{MaMH – TenMH}**?", xác nhận thì POST `/sv/dang-ky/huy`.
  - Môn đã có điểm: không có nút Hủy, hiển thị badge xám "Đã có điểm" (QĐ6).
  - Rỗng: "Bạn chưa đăng ký môn nào trong học kỳ này."
- Sau mỗi thao tác: redirect về `/sv/dang-ky?hocKy=...` với flash message đúng nội dung ở bảng 2.2, ví dụ thành công "Đăng ký môn {TenMH} thành công." / "Đã hủy đăng ký môn {TenMH}.".
- Trên màn hình rộng: hai khối đặt trên – dưới (không chia cột) để bảng đủ rộng.

#### MH08. Kết quả học tập — `GET /sv/ket-qua?hocKy=`
- Select "Học kỳ" với lựa chọn đầu là "Tất cả học kỳ".
- Bảng cột: STT · Học kỳ · Mã môn · Tên môn · Số TC · Điểm · Kết quả. Cột Kết quả: badge xanh "Đạt" (điểm ≥ 5), đỏ "Chưa đạt" (< 5), xám "Chưa có điểm".
- Khi chọn tất cả học kỳ, nhóm dòng theo học kỳ (dòng tiêu đề nhóm nền xám nhạt).
- Thẻ tổng kết phía trên bảng: Tổng tín chỉ đăng ký · Tín chỉ đạt · **Điểm trung bình** (trung bình có trọng số theo tín chỉ, chỉ tính môn đã có điểm, làm tròn 2 chữ số).
- Nút "In" (`window.print()`) và CSS `@media print` ẩn sidebar, header.

---

### 4.3. Nhóm màn hình Giảng viên

#### MH09. Trang chủ giảng viên — `GET /gv`
- Lời chào "Xin chào, {HocVi} {HoTen}", dòng phụ "Mã GV · Khoa".
- Thẻ thống kê học kỳ hiện tại: Số môn được phân công · Tổng số sinh viên · Số sinh viên chưa có điểm.

#### MH10. Môn được phân công — `GET /gv/phan-cong?hocKy=` (UC08)
- Select học kỳ. Hiển thị dạng **lưới thẻ** (3 thẻ mỗi hàng trên màn rộng, 1 thẻ trên điện thoại). Mỗi thẻ: Mã môn (badge), Tên môn (đậm), Số TC, "Sĩ số: n sinh viên", "Đã nhập điểm: m/n" kèm thanh tiến độ, nút "Danh sách & nhập điểm".
- Rỗng: "Bạn chưa được phân công môn nào trong học kỳ này."

#### MH11. Danh sách sinh viên và nhập điểm — `GET/POST /gv/nhap-diem/{maMH}?hocKy=` (UC08, UC09)
- Kiểm tra quyền: GV đang đăng nhập phải có bản ghi PHAN_CONG với `maMH`, `hocKy`; nếu không → 403.
- Tiêu đề: "{TenMH} ({MaMH}) · {HocKy} · {SoTinChi} tín chỉ". Nút "← Quay lại".
- Một **form duy nhất** bao toàn bộ bảng. Cột: STT · Mã SV · Họ tên · Lớp · Ngày đăng ký · Điểm. Cột Điểm là `input type="number" min="0" max="10" step="0.1"`, rộng khoảng 90px, căn giữa; để trống nghĩa là chưa nhập.
- JS: ô nhập ngoài khoảng 0–10 viền đỏ ngay khi gõ; khi có thay đổi chưa lưu, hiện dòng cảnh báo vàng "Có thay đổi chưa lưu" và hỏi lại nếu rời trang (`beforeunload`).
- Thanh dưới cố định (sticky bottom): "Đã nhập m/n sinh viên" bên trái; nút "Lưu điểm" (primary) bên phải.
- Server kiểm tra lại QĐ7 cho từng ô. Nếu có ô sai: không lưu gì, trả lại form giữ nguyên giá trị đã nhập, đánh dấu đỏ các ô sai kèm thông báo tổng "Có {k} điểm không hợp lệ, vui lòng kiểm tra lại." Nếu hợp lệ: lưu trong một transaction, flash "Đã lưu điểm cho {m} sinh viên."
- Nút phụ "Xuất CSV" tải danh sách sinh viên kèm điểm (UTF-8 có BOM để Excel đọc đúng tiếng Việt).

---

### 4.4. Nhóm màn hình Quản trị viên

#### MH12. Tổng quan — `GET /admin`
- Hàng 4 thẻ thống kê có icon màu: Sinh viên · Giảng viên · Môn học · Lượt đăng ký trong học kỳ hiện tại.
- Bên dưới chia hai cột:
  - Trái: bảng "Top 5 môn nhiều sinh viên đăng ký nhất" (Mã môn · Tên môn · Số SV).
  - Phải: bảng "Môn được mở nhưng chưa có sinh viên đăng ký".

#### MH13. Quản lý khoa — `/admin/khoa` (UC11)
- Danh sách: tìm theo mã/tên. Cột: STT · Mã khoa · Tên khoa · Số lớp · Số giảng viên · Số môn học · Thao tác.
- Thêm/Sửa bằng **modal**: Mã khoa* (chữ in hoa, không dấu, tối đa 10 ký tự, regex `^[A-Z0-9]{1,10}$`), Tên khoa* (tối đa 100 ký tự).
- Khi lỗi validation từ server, mở lại modal tự động và hiển thị lỗi (truyền cờ `openModal` về view).
- Xóa: chặn nếu khoa còn lớp, giảng viên hoặc môn học.
- Route: `GET /admin/khoa`, `POST /admin/khoa/luu`, `POST /admin/khoa/{ma}/xoa`.

#### MH14. Quản lý lớp — `/admin/lop` (UC11)
- Bộ lọc: select Khoa (có "Tất cả khoa") + ô tìm kiếm.
- Cột: STT · Mã lớp · Tên lớp · Khóa học · Khoa · Sĩ số · Thao tác. Click vào sĩ số chuyển sang MH15 lọc sẵn theo lớp.
- Modal thêm/sửa: Mã lớp*, Tên lớp*, Khóa học (ví dụ K2024), Khoa* (select).
- Xóa: chặn nếu lớp còn sinh viên.

#### MH15. Quản lý sinh viên — `/admin/sinh-vien` (UC12)
- Bộ lọc: Khoa → Lớp (select lớp nạp lại theo khoa bằng `fetch` tới `GET /api/lop?maKhoa=` trả JSON) + tìm theo mã/họ tên. Có phân trang.
- Cột: STT · Mã SV · Họ tên · Ngày sinh · Giới tính · Lớp · Tài khoản (badge xanh "Đã cấp" hoặc nút nhỏ "Cấp tài khoản") · Thao tác.
- **Trang riêng** thêm/sửa `/admin/sinh-vien/them`, `/admin/sinh-vien/{ma}/sua`, bố cục 2 cột trên màn rộng:
  - Mã SV* · Họ tên* · Ngày sinh (`input type="date"`, phải trước ngày hiện tại) · Giới tính (radio Nam/Nữ) · Khoa* → Lớp* (select phụ thuộc).
  - Khi thêm mới: checkbox "Tạo tài khoản đăng nhập" (mặc định bật) — tạo TAI_KHOAN với tên đăng nhập = mã SV viết thường, mật khẩu mặc định `123456`, quyền SV.
- Xóa: chặn nếu sinh viên đã có đăng ký môn học; nếu chưa, xóa luôn tài khoản gắn kèm (trong một transaction).

#### MH16. Quản lý giảng viên — `/admin/giang-vien` (UC13)
- Giống MH15. Bộ lọc theo khoa. Cột: STT · Mã GV · Họ tên · Học vị · Khoa · Số môn được phân công (học kỳ hiện tại) · Tài khoản · Thao tác.
- Trang thêm/sửa: Mã GV* · Họ tên* · Học vị (select: Cử nhân, Thạc sĩ, Tiến sĩ, Phó giáo sư, Giáo sư) · Khoa* · checkbox tạo tài khoản (quyền GV).
- Xóa: chặn nếu giảng viên đã có phân công.

#### MH17. Quản lý môn học — `/admin/mon-hoc` (UC14)
- Bộ lọc theo khoa + tìm kiếm. Cột: STT · Mã môn · Tên môn · Số TC · Khoa phụ trách · Trạng thái học kỳ hiện tại (badge xanh "Đang mở" nếu có phân công, xám "Chưa mở") · Thao tác.
- Modal thêm/sửa: Mã môn* · Tên môn* · Số tín chỉ* (number 1–6) · Khoa phụ trách*.
- Xóa: chặn nếu môn đã có đăng ký hoặc phân công.

#### MH18. Phân công giảng dạy — `/admin/phan-cong` (UC15)
- Select học kỳ ở đầu trang (có thể nhập học kỳ mới theo mẫu `HK{1|2|3}-{năm}`, kiểm tra bằng regex).
- Bố cục hai cột:
  - **Trái (4/12)** – thẻ "Thêm phân công": Môn học* (select hiển thị "MaMH – TenMH (x TC)"), Khoa (lọc giảng viên), Giảng viên* (select theo khoa), nút "Phân công". Báo lỗi nếu trùng: "Giảng viên này đã được phân công môn này trong học kỳ."
  - **Phải (8/12)** – bảng phân công của học kỳ: STT · Mã môn · Tên môn · Số TC · Giảng viên · Số SV đã đăng ký · Thao tác (Xóa). Không cho xóa nếu môn đó trong học kỳ đã có sinh viên đăng ký và đây là giảng viên duy nhất dạy môn: "Không thể xóa: môn đã có sinh viên đăng ký."
- Route: `GET /admin/phan-cong`, `POST /admin/phan-cong/luu`, `POST /admin/phan-cong/xoa` (gửi `maGV`, `maMH`, `hocKy`).

#### MH19. Quản lý tài khoản — `/admin/tai-khoan` (UC10)
- Bộ lọc: Quyền (Tất cả / Quản trị viên / Giảng viên / Sinh viên) · Trạng thái (Tất cả / Hoạt động / Bị khóa) · tìm theo tên đăng nhập hoặc họ tên chủ tài khoản. Có phân trang.
- Cột: STT · Tên đăng nhập · Chủ tài khoản (họ tên SV/GV, hoặc "—" với admin) · Quyền (badge màu theo quyền) · Trạng thái (badge "Hoạt động"/"Bị khóa") · Thao tác.
- Thao tác trên mỗi dòng (nhóm nút icon có tooltip):
  - **Khóa / Mở khóa** (`bi-lock` / `bi-unlock`) — có modal xác nhận. Không cho admin tự khóa chính mình.
  - **Đặt lại mật khẩu** (`bi-key`) — đặt về `123456`, flash "Đã đặt lại mật khẩu cho {tenDangNhap} về 123456."
  - **Xóa** — chỉ cho xóa tài khoản không gắn SV/GV và không phải tài khoản đang đăng nhập.
- Trang thêm tài khoản `/admin/tai-khoan/them`: Tên đăng nhập* (3–30 ký tự, `^[a-z0-9_.]+$`) · Mật khẩu* + Nhập lại* · Quyền* (radio). Khi chọn quyền SV hoặc GV, hiện thêm select "Gắn với sinh viên/giảng viên" chỉ liệt kê người **chưa có tài khoản** (bắt buộc chọn). Khi chọn ADMIN thì ẩn select này.

---

## PHẦN 5. YÊU CẦU KỸ THUẬT BỔ SUNG

1. Mọi form POST có token CSRF (`th:action` tự thêm).
2. `GlobalExceptionHandler` bắt `BusinessException` → redirect về trang trước với flash lỗi; bắt lỗi khác → trang 500.
3. Service dùng `@Transactional` cho thao tác ghi; truy vấn danh sách dùng `JOIN FETCH` hoặc DTO projection để tránh N+1.
4. Đăng ký môn học: kiểm tra QĐ3 → QĐ4 → QĐ5 theo thứ tự, trả về thông báo đầu tiên bị vi phạm.
5. `CustomUserDetailsService` nạp tài khoản theo tên đăng nhập, gán `ROLE_{MaQuyen}`, đặt `enabled = TrangThai`. Lưu `maSV`/`maGV`, `hoTen` trong `CustomUserDetails` để hiển thị header và lọc dữ liệu theo người đang đăng nhập (SV chỉ thấy dữ liệu của mình).
6. Thêm `@ControllerAdvice` đưa `hocKyHienTai` và thông tin người dùng vào model mọi trang.
7. Viết `README.md`: yêu cầu cài đặt, tạo database, cách chạy, danh sách tài khoản mẫu, ánh xạ màn hình ↔ use case (MH01–MH19 ↔ UC01–UC15), cách đổi sang SQL Server (driver `mssql-jdbc`, URL và dialect).
8. Viết vài test cho `DangKyService`: đăng ký thành công; trùng môn; vượt tín chỉ; hủy môn đã có điểm.

## PHẦN 6. THỨ TỰ THỰC HIỆN VÀ CÁCH BÀN GIAO

Làm theo thứ tự, mỗi bước xong phải chạy được:
1. `pom.xml`, cấu hình, 9 entity, repository, `DataSeeder`.
2. Spring Security, layout chung, fragments, MH01–MH05.
3. Nhóm Quản trị viên MH12–MH19.
4. Nhóm Sinh viên MH06–MH08.
5. Nhóm Giảng viên MH09–MH11.
6. Exception handler, test, README.

Khi trả lời, in **đường dẫn đầy đủ của từng file** trước khối code của file đó. Cuối cùng liệt kê checklist 19 màn hình và đánh dấu đã hoàn thành.
