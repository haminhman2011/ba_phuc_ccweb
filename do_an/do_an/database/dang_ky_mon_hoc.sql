-- =============================================================================
--  HỆ THỐNG ĐĂNG KÝ MÔN HỌC - Cơ sở dữ liệu MySQL 8
--  9 bảng: KHOA, LOP, SINH_VIEN, GIANG_VIEN, MON_HOC, DANG_KY, PHAN_CONG, QUYEN, TAI_KHOAN
--
--  Cách dùng: mở bằng MySQL Workbench / phpMyAdmin rồi chạy toàn bộ, hoặc
--     mysql -u root -p < database/dang_ky_mon_hoc.sql
--  CHÚ Ý: script XÓA và TẠO LẠI các bảng, dữ liệu cũ sẽ mất.
--
--  Tài khoản mẫu (mật khẩu đã băm BCrypt):
--     admin / admin123      (Quản trị viên)
--     gv01..gv04 / 123456   (Giảng viên)
--     sv01..sv10 / 123456   (Sinh viên; sv10 bị khóa sẵn để thử QĐ9)
-- =============================================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS dang_ky_mon_hoc
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE dang_ky_mon_hoc;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS TAI_KHOAN;
DROP TABLE IF EXISTS DANG_KY;
DROP TABLE IF EXISTS PHAN_CONG;
DROP TABLE IF EXISTS QUYEN;
DROP TABLE IF EXISTS MON_HOC;
DROP TABLE IF EXISTS GIANG_VIEN;
DROP TABLE IF EXISTS SINH_VIEN;
DROP TABLE IF EXISTS LOP;
DROP TABLE IF EXISTS KHOA;
SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- 5.4.1. KHOA
-- -----------------------------------------------------------------------------
CREATE TABLE KHOA (
    MaKhoa   VARCHAR(10)  NOT NULL,
    TenKhoa  VARCHAR(100) NOT NULL,
    CONSTRAINT PK_KHOA PRIMARY KEY (MaKhoa)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5.4.2. LOP  (QĐ1: mỗi lớp thuộc đúng một khoa)
-- -----------------------------------------------------------------------------
CREATE TABLE LOP (
    MaLop    VARCHAR(10) NOT NULL,
    TenLop   VARCHAR(50) NOT NULL,
    KhoaHoc  VARCHAR(10) NULL,
    MaKhoa   VARCHAR(10) NOT NULL,
    CONSTRAINT PK_LOP PRIMARY KEY (MaLop),
    CONSTRAINT FK_LOP_KHOA FOREIGN KEY (MaKhoa) REFERENCES KHOA (MaKhoa)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5.4.3. SINH_VIEN  (QĐ1: mỗi sinh viên thuộc đúng một lớp)
-- -----------------------------------------------------------------------------
CREATE TABLE SINH_VIEN (
    MaSV      VARCHAR(10) NOT NULL,
    HoTen     VARCHAR(50) NOT NULL,
    NgaySinh  DATE        NULL,
    GioiTinh  VARCHAR(5)  NULL,
    MaLop     VARCHAR(10) NOT NULL,
    CONSTRAINT PK_SINH_VIEN PRIMARY KEY (MaSV),
    CONSTRAINT FK_SV_LOP FOREIGN KEY (MaLop) REFERENCES LOP (MaLop),
    CONSTRAINT CK_SV_GIOITINH CHECK (GioiTinh IN ('Nam', 'Nữ'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5.4.4. GIANG_VIEN  (QĐ2: mỗi giảng viên thuộc đúng một khoa)
-- -----------------------------------------------------------------------------
CREATE TABLE GIANG_VIEN (
    MaGV    VARCHAR(10) NOT NULL,
    HoTen   VARCHAR(50) NOT NULL,
    HocVi   VARCHAR(30) NULL,
    MaKhoa  VARCHAR(10) NOT NULL,
    CONSTRAINT PK_GIANG_VIEN PRIMARY KEY (MaGV),
    CONSTRAINT FK_GV_KHOA FOREIGN KEY (MaKhoa) REFERENCES KHOA (MaKhoa)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5.4.5. MON_HOC  (QĐ2: mỗi môn do đúng một khoa phụ trách; QĐ8: 1–6 tín chỉ)
-- -----------------------------------------------------------------------------
CREATE TABLE MON_HOC (
    MaMH      VARCHAR(10)  NOT NULL,
    TenMH     VARCHAR(100) NOT NULL,
    SoTinChi  INT          NOT NULL,
    MaKhoa    VARCHAR(10)  NOT NULL,
    CONSTRAINT PK_MON_HOC PRIMARY KEY (MaMH),
    CONSTRAINT FK_MH_KHOA FOREIGN KEY (MaKhoa) REFERENCES KHOA (MaKhoa),
    CONSTRAINT CK_MH_SOTINCHI CHECK (SoTinChi BETWEEN 1 AND 6)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5.4.6. DANG_KY  (QĐ4: khóa chính (MaSV, MaMH, HocKy) chặn đăng ký trùng; QĐ7: điểm 0–10)
-- -----------------------------------------------------------------------------
CREATE TABLE DANG_KY (
    MaSV        VARCHAR(10) NOT NULL,
    MaMH        VARCHAR(10) NOT NULL,
    HocKy       VARCHAR(10) NOT NULL,
    NgayDangKy  DATE        NULL DEFAULT (CURRENT_DATE),
    Diem        FLOAT       NULL,
    CONSTRAINT PK_DANG_KY PRIMARY KEY (MaSV, MaMH, HocKy),
    CONSTRAINT FK_DK_SV FOREIGN KEY (MaSV) REFERENCES SINH_VIEN (MaSV),
    CONSTRAINT FK_DK_MH FOREIGN KEY (MaMH) REFERENCES MON_HOC (MaMH),
    CONSTRAINT CK_DK_DIEM CHECK (Diem IS NULL OR (Diem >= 0 AND Diem <= 10)),
    CONSTRAINT CK_DK_HOCKY CHECK (HocKy REGEXP '^HK[123]-[0-9]{4}$')
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5.4.7. PHAN_CONG  (môn có phân công trong học kỳ = môn được mở, QĐ3)
-- -----------------------------------------------------------------------------
CREATE TABLE PHAN_CONG (
    MaGV   VARCHAR(10) NOT NULL,
    MaMH   VARCHAR(10) NOT NULL,
    HocKy  VARCHAR(10) NOT NULL,
    CONSTRAINT PK_PHAN_CONG PRIMARY KEY (MaGV, MaMH, HocKy),
    CONSTRAINT FK_PC_GV FOREIGN KEY (MaGV) REFERENCES GIANG_VIEN (MaGV),
    CONSTRAINT FK_PC_MH FOREIGN KEY (MaMH) REFERENCES MON_HOC (MaMH),
    CONSTRAINT CK_PC_HOCKY CHECK (HocKy REGEXP '^HK[123]-[0-9]{4}$')
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5.4.8. QUYEN
-- -----------------------------------------------------------------------------
CREATE TABLE QUYEN (
    MaQuyen   VARCHAR(10) NOT NULL,
    TenQuyen  VARCHAR(50) NOT NULL,
    CONSTRAINT PK_QUYEN PRIMARY KEY (MaQuyen)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5.4.9. TAI_KHOAN  (QĐ9: mỗi tài khoản một quyền; gắn tối đa một SV hoặc một GV)
-- -----------------------------------------------------------------------------
CREATE TABLE TAI_KHOAN (
    TenDangNhap  VARCHAR(30)  NOT NULL,
    MatKhau      VARCHAR(255) NOT NULL,
    TrangThai    BIT(1)       NOT NULL DEFAULT 1,
    MaQuyen      VARCHAR(10)  NOT NULL,
    MaSV         VARCHAR(10)  NULL,
    MaGV         VARCHAR(10)  NULL,
    CONSTRAINT PK_TAI_KHOAN PRIMARY KEY (TenDangNhap),
    CONSTRAINT UQ_TK_MASV UNIQUE (MaSV),
    CONSTRAINT UQ_TK_MAGV UNIQUE (MaGV),
    CONSTRAINT FK_TK_QUYEN FOREIGN KEY (MaQuyen) REFERENCES QUYEN (MaQuyen),
    CONSTRAINT FK_TK_SV FOREIGN KEY (MaSV) REFERENCES SINH_VIEN (MaSV),
    CONSTRAINT FK_TK_GV FOREIGN KEY (MaGV) REFERENCES GIANG_VIEN (MaGV),
    CONSTRAINT CK_TK_CHU CHECK (MaSV IS NULL OR MaGV IS NULL)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- =============================================================================
--  DỮ LIỆU MẪU (giống DataSeeder.java)
-- =============================================================================

INSERT INTO QUYEN (MaQuyen, TenQuyen) VALUES
    ('ADMIN', 'Quản trị viên'),
    ('GV',    'Giảng viên'),
    ('SV',    'Sinh viên');

INSERT INTO KHOA (MaKhoa, TenKhoa) VALUES
    ('CNTT', 'Công nghệ thông tin'),
    ('KT',   'Kinh tế');

INSERT INTO LOP (MaLop, TenLop, KhoaHoc, MaKhoa) VALUES
    ('CNTT01', 'Công nghệ thông tin 01', 'K2024', 'CNTT'),
    ('CNTT02', 'Công nghệ thông tin 02', 'K2025', 'CNTT'),
    ('KT01',   'Kinh tế 01',             'K2024', 'KT');

INSERT INTO SINH_VIEN (MaSV, HoTen, NgaySinh, GioiTinh, MaLop) VALUES
    ('SV01', 'Nguyễn Văn An',   '2005-03-15', 'Nam', 'CNTT01'),
    ('SV02', 'Trần Thị Bình',   '2005-07-22', 'Nữ',  'CNTT01'),
    ('SV03', 'Lê Hoàng Cường',  '2005-11-02', 'Nam', 'CNTT01'),
    ('SV04', 'Phạm Thị Dung',   '2005-01-30', 'Nữ',  'CNTT01'),
    ('SV05', 'Hoàng Minh Đức',  '2006-04-12', 'Nam', 'CNTT02'),
    ('SV06', 'Vũ Thị Hà',       '2006-09-08', 'Nữ',  'CNTT02'),
    ('SV07', 'Đặng Quốc Huy',   '2006-12-19', 'Nam', 'CNTT02'),
    ('SV08', 'Bùi Thị Lan',     '2005-05-05', 'Nữ',  'KT01'),
    ('SV09', 'Đỗ Văn Minh',     '2005-08-25', 'Nam', 'KT01'),
    ('SV10', 'Ngô Thị Ngọc',    '2005-10-10', 'Nữ',  'KT01');

INSERT INTO GIANG_VIEN (MaGV, HoTen, HocVi, MaKhoa) VALUES
    ('GV01', 'Trần Văn Hùng',  'Tiến sĩ',     'CNTT'),
    ('GV02', 'Nguyễn Thị Mai', 'Thạc sĩ',     'CNTT'),
    ('GV03', 'Lê Văn Nam',     'Thạc sĩ',     'KT'),
    ('GV04', 'Phạm Thị Oanh',  'Phó giáo sư', 'KT');

INSERT INTO MON_HOC (MaMH, TenMH, SoTinChi, MaKhoa) VALUES
    ('CSDL', 'Cơ sở dữ liệu',                  3, 'CNTT'),
    ('CTDL', 'Cấu trúc dữ liệu và giải thuật', 4, 'CNTT'),
    ('LTW',  'Lập trình web',                  3, 'CNTT'),
    ('LTJ',  'Lập trình Java',                 3, 'CNTT'),
    ('MMT',  'Mạng máy tính',                  3, 'CNTT'),
    ('PTTK', 'Phân tích thiết kế hệ thống',    3, 'CNTT'),
    ('KTVM', 'Kinh tế vi mô',                  2, 'KT'),
    ('NLKT', 'Nguyên lý kế toán',              3, 'KT');

-- HK1-2026 (học kỳ hiện tại): mở 6 môn; MMT và PTTK chưa mở
INSERT INTO PHAN_CONG (MaGV, MaMH, HocKy) VALUES
    ('GV01', 'CSDL', 'HK1-2026'),
    ('GV02', 'CSDL', 'HK1-2026'),
    ('GV01', 'LTW',  'HK1-2026'),
    ('GV02', 'LTJ',  'HK1-2026'),
    ('GV02', 'CTDL', 'HK1-2026'),
    ('GV03', 'KTVM', 'HK1-2026'),
    ('GV04', 'NLKT', 'HK1-2026'),
-- HK2-2025 (học kỳ trước)
    ('GV01', 'CTDL', 'HK2-2025'),
    ('GV02', 'MMT',  'HK2-2025'),
    ('GV03', 'KTVM', 'HK2-2025');

INSERT INTO DANG_KY (MaSV, MaMH, HocKy, NgayDangKy, Diem) VALUES
-- SV01: CSDL đã có điểm (không hủy được - QĐ6), LTW chưa có điểm
    ('SV01', 'CSDL', 'HK1-2026', '2026-09-05', 8.5),
    ('SV01', 'LTW',  'HK1-2026', '2026-09-05', NULL),
    ('SV02', 'CSDL', 'HK1-2026', '2026-09-06', NULL),
    ('SV02', 'LTJ',  'HK1-2026', '2026-09-06', NULL),
    ('SV03', 'CSDL', 'HK1-2026', '2026-09-06', NULL),
    ('SV03', 'LTW',  'HK1-2026', '2026-09-07', NULL),
    ('SV04', 'LTW',  'HK1-2026', '2026-09-07', NULL),
    ('SV05', 'LTJ',  'HK1-2026', '2026-09-08', NULL),
    ('SV08', 'KTVM', 'HK1-2026', '2026-09-08', NULL),
    ('SV08', 'NLKT', 'HK1-2026', '2026-09-08', NULL),
    ('SV09', 'NLKT', 'HK1-2026', '2026-09-09', NULL),
-- HK2-2025: đã có điểm
    ('SV01', 'CTDL', 'HK2-2025', '2026-01-10', 7.5),
    ('SV01', 'KTVM', 'HK2-2025', '2026-01-10', 4.0),
    ('SV01', 'MMT',  'HK2-2025', '2026-01-11', 6.0),
    ('SV02', 'CTDL', 'HK2-2025', '2026-01-12', 9.0),
    ('SV08', 'KTVM', 'HK2-2025', '2026-01-12', 6.5);

-- Mật khẩu: admin123 và 123456 (BCrypt)
INSERT INTO TAI_KHOAN (TenDangNhap, MatKhau, TrangThai, MaQuyen, MaSV, MaGV) VALUES
    ('admin', '$2a$10$AnRMDIgJYoPDrwiCRR4nIOJ24rQXz.oDlxtVbxsbqpmVR9ervSvca', 1, 'ADMIN', NULL, NULL),
    ('gv01',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 1, 'GV', NULL, 'GV01'),
    ('gv02',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 1, 'GV', NULL, 'GV02'),
    ('gv03',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 1, 'GV', NULL, 'GV03'),
    ('gv04',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 1, 'GV', NULL, 'GV04'),
    ('sv01',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 1, 'SV', 'SV01', NULL),
    ('sv02',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 1, 'SV', 'SV02', NULL),
    ('sv03',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 1, 'SV', 'SV03', NULL),
    ('sv04',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 1, 'SV', 'SV04', NULL),
    ('sv05',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 1, 'SV', 'SV05', NULL),
    ('sv06',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 1, 'SV', 'SV06', NULL),
    ('sv07',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 1, 'SV', 'SV07', NULL),
    ('sv08',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 1, 'SV', 'SV08', NULL),
    ('sv09',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 1, 'SV', 'SV09', NULL),
    ('sv10',  '$2a$10$24aTu/vQQ898.L2l3CWYHOh2Xd2u0yqm.EkIHTBoHwsc6UcvLzXym', 0, 'SV', 'SV10', NULL);
