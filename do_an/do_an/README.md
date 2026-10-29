# Hệ thống đăng ký môn học

Ứng dụng web cho sinh viên đăng ký/hủy đăng ký môn học theo lớp, giảng viên nhập điểm và phòng đào tạo quản lý
danh mục, phân công giảng dạy, xếp lịch dạy, tài khoản.

**Công nghệ:** Java 17+, Spring Boot 4.1, Spring Security, Spring Data JPA (Hibernate 7), Thymeleaf +
Layout Dialect, Bootstrap 5.3, Bootstrap Icons, Lombok, MySQL 8.

## 1. Yêu cầu cài đặt

- JDK 17 trở lên (đã chạy thử với JDK 26)
- Maven 3.9+ (hoặc dùng `mvnw` đi kèm)
- MySQL 8.0.16+ (cần bản hỗ trợ ràng buộc `CHECK`)
- Trình duyệt có Internet (Bootstrap, font Be Vietnam Pro tải từ CDN)

## 2. Tạo cơ sở dữ liệu

Chọn **một** trong hai cách:

**Cách 1 – Import script (khuyên dùng, có đủ PK/FK/CHECK/UNIQUE):**

```bash
mysql -u root -p < database/dang_ky_mon_hoc.sql
```

Hoặc mở `database/dang_ky_mon_hoc.sql` bằng MySQL Workbench / phpMyAdmin và chạy toàn bộ.
Script tạo database `dang_ky_mon_hoc`, 11 bảng (có `HOC_KY`, `PHONG`, `LICH_HOC`) và dữ liệu mẫu.
**Script xóa và tạo lại bảng.** Quyền (ADMIN, GV, SV) gán cứng trong `common/Quyen.java`, không có bảng `QUYEN`.

**Cách 2 – Để ứng dụng tự tạo:** chỉ cần MySQL đang chạy. Nhờ `createDatabaseIfNotExist=true` và
`ddl-auto=update`, Hibernate tự tạo bảng, `DataSeeder` nạp dữ liệu mẫu khi bảng `TAI_KHOAN` còn rỗng.
Cách này **không** tạo các ràng buộc `CHECK` (chúng vẫn được kiểm tra ở tầng service).

**CSDL tạo trước khi có bảng `HOC_KY`:** nên chạy lại script SQL. Nếu giữ dữ liệu cũ, Hibernate tự thêm bảng `HOC_KY`
và cột `LICH_HOC.TuanBatDau/TuanKetThuc`; `DataSeeder` khai báo thời gian mặc định cho các học kỳ đã có phân công
(HK1 từ thứ 2 đầu tháng 9, HK2 từ thứ 2 đầu tháng 2, 18 tuần) và gán tuần 1–15 cho lịch học cũ. Khóa ngoại
`PHAN_CONG → HOC_KY` được tạo ở lần chạy sau. Bảng `QUYEN` cũ không còn dùng, có thể xóa.

**CSDL tạo trước khi có chức năng xếp lịch:** chỉ cần chạy ứng dụng. Hibernate tự thêm bảng `PHONG`, `LICH_HOC`
và cột `DANG_KY.MaGV`; `DataSeeder` nạp 6 phòng nếu bảng `PHONG` rỗng và gán lớp cho các đăng ký cũ (giảng viên có
mã nhỏ nhất dạy môn đó trong học kỳ).

Thông tin kết nối nằm trong `src/main/resources/application.properties`
(mặc định `root`, mật khẩu rỗng, cổng 3306). Sửa lại cho đúng máy của bạn.

## 3. Chạy ứng dụng

```bash
mvn spring-boot:run
```

Mở <http://localhost:8080>.

Chạy thử **không cần MySQL** (CSDL H2 trong bộ nhớ, mất dữ liệu khi tắt):

```bash
mvn spring-boot:run "-Dspring-boot.run.useTestClasspath=true" "-Dspring-boot.run.profiles=dev" "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:h2:mem:dkmh;MODE=MySQL;DB_CLOSE_DELAY=-1 --spring.datasource.username=sa --spring.datasource.driver-class-name=org.h2.Driver --spring.jpa.hibernate.ddl-auto=create-drop"
```

Chạy test: `mvn test` (test dùng H2, không cần MySQL).

## 4. Tài khoản mẫu

| Tên đăng nhập | Mật khẩu | Vai trò | Ghi chú |
|---|---|---|---|
| admin | admin123 | Quản trị viên | |
| gv01 … gv04 | 123456 | Giảng viên | gv01 dạy CSDL, LTW trong HK1-2026 |
| sv01 … sv10 | 123456 | Sinh viên | sv01 đã đăng ký CSDL (có điểm) và LTW |
| sv10 | 123456 | Sinh viên | **bị khóa sẵn** để thử QĐ9 |

Dòng "Tài khoản dùng thử" ở trang đăng nhập chỉ hiện khi profile `dev` đang bật
(`spring.profiles.active=dev` trong `application.properties`; xóa dòng đó khi nộp/triển khai).

**Sinh viên chỉ đăng ký được khi lịch học kỳ đã chính thức.** Dữ liệu mẫu chưa có lịch, nên khi demo:
đăng nhập `admin` → **Xếp lịch dạy** → **Tự động xếp lịch** → **Lưu chính thức**, sau đó đăng nhập sinh viên.

Dữ liệu mẫu có 3 học kỳ: `HK2-2025` (02/02–07/06/2026, đã có điểm), `HK1-2026` (07/09/2026–10/01/2027,
6 môn được mở) và `HK2-2026` (01/02–06/06/2027, chưa có phân công).

**Học kỳ hiện tại** tính theo ngày hôm nay: học kỳ đang diễn ra, hoặc học kỳ sắp tới khi đang nghỉ giữa hai học kỳ.
Thêm/sửa thời gian học kỳ ở menu **Học kỳ** (`/admin/hoc-ky`). Học kỳ đã qua chỉ được xem lịch.

**Thời gian học** (gán cứng trong `common/TietHoc.java`): tiết 1 bắt đầu 07:00, tiết 6 bắt đầu 13:00, mỗi tiết
50 phút; 1 tín chỉ = 15 tiết nên mỗi lớp học 15 tuần (mặc định tuần 1–15), mỗi tuần một buổi. Tuần 1 bắt đầu từ ngày
bắt đầu học kỳ (thứ 2); ngày học = ngày bắt đầu + (tuần − 1) × 7 + (thứ − 2).

## 5. Tham số nghiệp vụ

```properties
# app.hoc-ky-hien-tai=HK1-2026 # bỏ comment để cố định học kỳ hiện tại (mặc định tính theo ngày)
app.tin-chi-toi-da=24          # QĐ5
app.page-size=10               # số dòng mỗi trang
```

## 6. Ánh xạ màn hình ↔ use case

| Màn hình | URL | Use case |
|---|---|---|
| MH01 Đăng nhập | `/login` | UC01 |
| MH02 Đổi mật khẩu | `/doi-mat-khau` | UC02 |
| MH03–MH05 Trang lỗi 403/404/500 | `templates/error/` | – |
| MH06 Trang chủ sinh viên | `/sv` | UC03, UC07 (tổng quan) |
| MH07 Đăng ký môn học | `/sv/dang-ky` | UC03, UC04, UC05, UC06 |
| MH08 Kết quả học tập | `/sv/ket-qua` | UC07 |
| MH09 Trang chủ giảng viên | `/gv` | UC08 (tổng quan) |
| MH10 Môn được phân công | `/gv/phan-cong` | UC08 |
| MH11 Danh sách SV & nhập điểm | `/gv/nhap-diem/{maMH}` | UC08, UC09 |
| MH12 Tổng quan | `/admin` | – |
| MH13 Quản lý khoa | `/admin/khoa` | UC11 |
| MH14 Quản lý lớp | `/admin/lop` | UC11 |
| MH15 Quản lý sinh viên | `/admin/sinh-vien` | UC12 |
| MH16 Quản lý giảng viên | `/admin/giang-vien` | UC13 |
| MH17 Quản lý môn học | `/admin/mon-hoc` | UC14 |
| MH18 Phân công giảng dạy | `/admin/phan-cong` | UC15 |
| MH19 Quản lý tài khoản | `/admin/tai-khoan` | UC10 |
| MH20 Xếp lịch dạy | `/admin/xep-lich?hocKy=` | Xếp lịch |
| MH21 Lịch học (SV) | `/sv/lich-hoc?tuan=` | Xếp lịch (tuần 0 = cả học kỳ) |
| MH22 Lịch dạy (GV) | `/gv/lich-day?tuan=` | Xếp lịch (tuần 0 = cả học kỳ) |
| MH23 Quản lý học kỳ | `/admin/hoc-ky` | Thời gian học kỳ |

## 7. Quy định nghiệp vụ được kiểm tra ở tầng service

| Mã | Nơi kiểm tra |
|---|---|
| QĐ1, QĐ2 | khóa ngoại NOT NULL; `SinhVienService.luu` kiểm tra lớp thuộc khoa đã chọn |
| QĐ3 → QĐ4 → QĐ5 | `DangKyService.dangKy` (theo đúng thứ tự, dừng ở lỗi đầu tiên) |
| QĐ6 | `DangKyService.huy` |
| QĐ7 | `DiemService.luuDiem` (0–10, chỉ GV được phân công); trang nhập điểm trả 403 nếu không được phân công |
| QĐ8 | `MonHocForm` (`@Min/@Max`) và `MonHocService.luu` |
| QĐ9 | `CustomUserDetails.enabled = TrangThai`; khóa/mở khóa ở `TaiKhoanService` |
| QĐ10 – trùng lịch | `DangKyService.dangKy`: "Trùng lịch với môn {TenMH} (Thứ x, tiết a–b, tuần c–d)." Chỉ đăng ký lớp đã có lịch chính thức |
| RB sức chứa khi đăng ký | `DangKyService.dangKy`: sĩ số lớp không vượt sức chứa phòng – "Lớp đã đủ {SucChua} sinh viên, vui lòng chọn lớp khác." |

**Ràng buộc xếp lịch** – `LichHocService.kiemTra`, dùng chung cho tự động xếp và sửa tay:

| Ràng buộc | Thông báo khi vi phạm |
|---|---|
| Buổi học không vắt qua sáng/chiều | "Buổi 3 tiết bắt đầu tiết 4 bị vắt qua hai buổi…" |
| Tuần học nằm trong học kỳ | "Lớp học tuần 5–19 nhưng học kỳ chỉ có 18 tuần." |
| Học kỳ: ngày bắt đầu là thứ 2, không trùng thời gian học kỳ khác | `HocKyService.luu` |
| Một giảng viên không có hai buổi giao nhau | "GV01 đã dạy CTDL thứ 3 tiết 1–4." |
| Một phòng không có hai buổi giao nhau | "Phòng A101 đã có lớp CTDL (GV01) thứ 3 tiết 1–4." |
| Sức chứa phòng ≥ số SV đã đăng ký lớp | "Phòng A101 chỉ có 40 chỗ, lớp CSDL (GV01) có 45 sinh viên." |
| Sinh viên đã đăng ký hai lớp thì hai lớp không giao nhau | "Sinh viên SV08 đã học NLKT thứ 2 tiết 1–3." |
| Xóa phân công | `PhanCongService.xoa`: chặn khi đã có lịch chính thức hoặc lớp đã có sinh viên đăng ký |
| Xóa bản ghi đang dùng | `xoa(...)` của từng service |

## 8. Cấu trúc mã nguồn

```
src/main/java/com/dangkymonhoc/
├── common/       dữ liệu cố định gán cứng: Quyen (enum quyền), TietHoc (giờ tiết, 15 tiết/tín chỉ)
├── config/       AppProperties, SecurityConfig, DataSeeder
├── entity/       11 bảng (+ DangKyId, PhanCongId dùng @EmbeddedId)
├── repository/   truy vấn JPQL (JOIN FETCH / projection để tránh N+1)
├── service/      nghiệp vụ, ném BusinessException khi vi phạm quy định
├── controller/   AuthController, HomeController, admin/, sv/, gv/
├── dto/          form object và dòng hiển thị
├── exception/    BusinessException, GlobalExceptionHandler
└── security/     CustomUserDetails, CustomUserDetailsService, RoleBasedSuccessHandler
src/main/resources/templates/  layout/, fragments/, auth/, error/, admin/, sv/, gv/
database/dang_ky_mon_hoc.sql   script tạo CSDL + dữ liệu mẫu
```

## 9. Xếp lịch dạy (MH20)

- Quy ước: mỗi phân công học 1 buổi/tuần, số tiết = số tín chỉ. Sáng tiết 1–5, chiều tiết 6–10, một buổi không
  vắt qua hai buổi. Hai buổi giao nhau khi cùng thứ và khoảng tiết chồng lên nhau.
- **Tự động xếp** (`LichHocService.tuDongXep`, tham lam, không ngẫu nhiên nên chạy lại ra cùng kết quả):
  1. Xóa buổi nháp chưa ghim; buổi đã ghim và buổi chính thức giữ nguyên.
  2. Sắp phân công chưa có lịch: số tiết giảm dần → giảng viên nhiều phân công hơn trước.
  3. Mỗi phân công duyệt thứ (ngày giảng viên đang ít buổi nhất trước) → tiết bắt đầu hợp lệ → phòng nhỏ nhất
     đủ chỗ, lấy vị trí đầu tiên không vi phạm ràng buộc. Không xếp được thì ghi lý do vào mục "Chưa xếp được".
- **Sửa tay**: bấm thẻ trên lưới hoặc nút Sửa. Đổi thứ/tiết/phòng thì buổi tự được ghim. Vi phạm ràng buộc thì
  modal giữ nguyên và hiện lý do (ví dụ "GV01 đã dạy CSDL thứ 2 tiết 1–3."). Sau khi chính thức, sửa phải xác nhận.
- **Lưu chính thức**: chỉ bật khi mọi phân công đã có lịch và không còn xung đột.
- Giảng viên chỉ thấy và nhập điểm cho sinh viên lớp mình (`DANG_KY.MaGV`).

## 10. Checklist màn hình

- [x] MH01 Đăng nhập · MH02 Đổi mật khẩu · MH03–MH05 Trang lỗi
- [x] MH06 Trang chủ SV · MH07 Đăng ký môn học (chọn lớp, QĐ10) · MH08 Kết quả học tập
- [x] MH09 Trang chủ GV · MH10 Môn được phân công · MH11 Danh sách & nhập điểm (theo lớp)
- [x] MH12 Tổng quan · MH13 Khoa · MH14 Lớp · MH15 Sinh viên · MH16 Giảng viên · MH17 Môn học
- [x] MH18 Phân công giảng dạy (cột lịch học, chặn xóa khi đã chính thức) · MH19 Tài khoản
- [x] MH20 Xếp lịch dạy · MH21 Lịch học (SV) · MH22 Lịch dạy (GV)

## 11. Đổi sang SQL Server

1. Trong `pom.xml`, thay `mysql-connector-j` bằng:
   ```xml
   <dependency>
       <groupId>com.microsoft.sqlserver</groupId>
       <artifactId>mssql-jdbc</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```
2. Tạo database `dang_ky_mon_hoc` trước (SQL Server không hỗ trợ `createDatabaseIfNotExist`).
3. Sửa `application.properties`:
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=dang_ky_mon_hoc;encrypt=true;trustServerCertificate=true
   spring.datasource.username=sa
   spring.datasource.password=MatKhauCuaBan
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect
   # Lưu chuỗi tiếng Việt bằng NVARCHAR
   spring.jpa.properties.hibernate.use_nationalized_character_data=true
   ```
4. Script `database/dang_ky_mon_hoc.sql` viết cho MySQL. Nếu muốn dùng script trên SQL Server: đổi
   `VARCHAR` của cột tên sang `NVARCHAR`, bỏ `ENGINE/CHARSET/COLLATE`, đổi `BIT(1)` thành `BIT`,
   thay `REGEXP '^HK[123]-[0-9]{4}$'` bằng `LIKE 'HK[123]-[0-9][0-9][0-9][0-9]'`, thêm tiền tố `N'...'`
   cho chuỗi tiếng Việt. Hoặc đơn giản để `ddl-auto=update` tự tạo bảng và `DataSeeder` nạp dữ liệu.
