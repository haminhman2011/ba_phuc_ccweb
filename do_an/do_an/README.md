# Hệ thống đăng ký môn học

Ứng dụng web cho sinh viên đăng ký/hủy đăng ký môn học, giảng viên nhập điểm và phòng đào tạo quản lý
danh mục, phân công giảng dạy, tài khoản.

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
Script tạo database `dang_ky_mon_hoc`, 9 bảng và dữ liệu mẫu. **Script xóa và tạo lại bảng.**

**Cách 2 – Để ứng dụng tự tạo:** chỉ cần MySQL đang chạy. Nhờ `createDatabaseIfNotExist=true` và
`ddl-auto=update`, Hibernate tự tạo bảng, `DataSeeder` nạp dữ liệu mẫu khi bảng `QUYEN` còn rỗng.
Cách này **không** tạo các ràng buộc `CHECK` (chúng vẫn được kiểm tra ở tầng service).

Thông tin kết nối nằm trong `src/main/resources/application.properties`
(mặc định `root`, mật khẩu rỗng, cổng 3306). Sửa lại cho đúng máy của bạn.

## 3. Chạy ứng dụng

```bash
mvn spring-boot:run
```

Mở <http://localhost:8080>.

Chạy thử **không cần MySQL** (CSDL H2 trong bộ nhớ, mất dữ liệu khi tắt):

```bash
mvn spring-boot:run -Dspring-boot.run.useTestClasspath=true -Dspring-boot.run.profiles=h2,dev
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

Dữ liệu mẫu có 2 học kỳ: `HK1-2026` (hiện tại, 6 môn được mở) và `HK2-2025` (đã có điểm) để màn hình
kết quả học tập có dữ liệu nhóm theo học kỳ.

## 5. Tham số nghiệp vụ

```properties
app.hoc-ky-hien-tai=HK1-2026   # học kỳ mặc định ở mọi màn hình
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

## 7. Quy định nghiệp vụ được kiểm tra ở tầng service

| Mã | Nơi kiểm tra |
|---|---|
| QĐ1, QĐ2 | khóa ngoại NOT NULL; `SinhVienService.luu` kiểm tra lớp thuộc khoa đã chọn |
| QĐ3 → QĐ4 → QĐ5 | `DangKyService.dangKy` (theo đúng thứ tự, dừng ở lỗi đầu tiên) |
| QĐ6 | `DangKyService.huy` |
| QĐ7 | `DiemService.luuDiem` (0–10, chỉ GV được phân công); trang nhập điểm trả 403 nếu không được phân công |
| QĐ8 | `MonHocForm` (`@Min/@Max`) và `MonHocService.luu` |
| QĐ9 | `CustomUserDetails.enabled = TrangThai`; khóa/mở khóa ở `TaiKhoanService` |
| Xóa bản ghi đang dùng | `xoa(...)` của từng service |

## 8. Cấu trúc mã nguồn

```
src/main/java/com/dangkymonhoc/
├── config/       AppProperties, SecurityConfig, DataSeeder
├── entity/       9 bảng (+ DangKyId, PhanCongId dùng @EmbeddedId)
├── repository/   truy vấn JPQL (JOIN FETCH / projection để tránh N+1)
├── service/      nghiệp vụ, ném BusinessException khi vi phạm quy định
├── controller/   AuthController, HomeController, admin/, sv/, gv/
├── dto/          form object và dòng hiển thị
├── exception/    BusinessException, GlobalExceptionHandler
└── security/     CustomUserDetails, CustomUserDetailsService, RoleBasedSuccessHandler
src/main/resources/templates/  layout/, fragments/, auth/, error/, admin/, sv/, gv/
database/dang_ky_mon_hoc.sql   script tạo CSDL + dữ liệu mẫu
```

## 9. Đổi sang SQL Server

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
