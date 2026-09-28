-- =====================================================================
--  ĐỒ ÁN: HỆ THỐNG QUẢN LÝ KHO THỰC PHẨM ĐÔNG LẠNH
--  Database: MySQL 8.0  |  Backend dự kiến: Java Spring Boot + JPA
--  Bám theo yêu cầu chức năng FR1..FR7 và use case UC1..UC8
-- =====================================================================

DROP DATABASE IF EXISTS quanly_kholanh;
CREATE DATABASE quanly_kholanh
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE quanly_kholanh;

-- =====================================================================
--  1. NGƯỜI DÙNG  (FR1 - Xác thực & phân quyền, UC1 + UC8)
-- =====================================================================
CREATE TABLE nguoi_dung (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    ten_dang_nhap   VARCHAR(50)  NOT NULL,
    mat_khau        VARCHAR(255) NOT NULL,          -- lưu mật khẩu đã mã hoá (BCrypt)
    ho_ten          VARCHAR(100) NOT NULL,
    vai_tro         ENUM('THU_KHO','QUAN_LY') NOT NULL,
    trang_thai      TINYINT      NOT NULL DEFAULT 1, -- 1 = hoạt động, 0 = khoá
    ngay_tao        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_nguoidung_tendangnhap (ten_dang_nhap)
) ENGINE=InnoDB;

-- =====================================================================
--  2. SẢN PHẨM  (FR2 - Danh mục sản phẩm, UC2)
--     Có thêm nhiệt độ bảo quản (đặc thù thực phẩm đông lạnh) và tồn tối thiểu
-- =====================================================================
CREATE TABLE san_pham (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    ma_san_pham     VARCHAR(20)  NOT NULL,
    ten_san_pham    VARCHAR(150) NOT NULL,
    don_vi_tinh     VARCHAR(20)  NOT NULL,          -- kg, thùng, gói...
    nhiet_do_min    DECIMAL(5,2),                   -- nhiệt độ bảo quản thấp nhất (°C)
    nhiet_do_max    DECIMAL(5,2),                   -- nhiệt độ bảo quản cao nhất (°C)
    ton_toi_thieu   INT          NOT NULL DEFAULT 0,-- ngưỡng cảnh báo tồn tối thiểu (FR5)
    mo_ta           VARCHAR(255),
    trang_thai      TINYINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sanpham_ma (ma_san_pham)
) ENGINE=InnoDB;

-- =====================================================================
--  3. VỊ TRÍ KHO  (FR3/FR5 - lưu vị trí & tra tồn theo vị trí)
-- =====================================================================
CREATE TABLE vi_tri_kho (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    ma_vi_tri           VARCHAR(20)  NOT NULL,
    ten_vi_tri          VARCHAR(100) NOT NULL,      -- VD: Kho A - Kệ 1
    nhiet_do_hien_tai   DECIMAL(5,2),               -- nhiệt độ đo được của khu vực
    mo_ta               VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_vitri_ma (ma_vi_tri)
) ENGINE=InnoDB;

-- =====================================================================
--  4. LÔ HÀNG  (FR3 - nhập theo lô, FR4 - FEFO, FR5 - tồn, FR6 - cảnh báo)
--     Đây là bảng "trái tim" của hệ thống: mỗi lô có hạn dùng riêng.
--     so_luong_ton = số còn lại của lô, giảm dần mỗi lần xuất.
-- =====================================================================
CREATE TABLE lo_hang (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    ma_lo           VARCHAR(30)  NOT NULL,
    san_pham_id     BIGINT       NOT NULL,
    vi_tri_id       BIGINT,
    ngay_san_xuat   DATE,
    han_su_dung     DATE         NOT NULL,          -- dùng để sắp xếp FEFO & cảnh báo
    so_luong_nhap   INT          NOT NULL,          -- số lượng ban đầu khi nhập
    so_luong_ton    INT          NOT NULL,          -- số lượng còn tồn hiện tại
    ngay_tao        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_lohang_ma (ma_lo),
    KEY idx_lohang_sanpham (san_pham_id),
    KEY idx_lohang_hansudung (han_su_dung),          -- tăng tốc truy vấn FEFO & cảnh báo
    CONSTRAINT fk_lohang_sanpham FOREIGN KEY (san_pham_id) REFERENCES san_pham(id),
    CONSTRAINT fk_lohang_vitri   FOREIGN KEY (vi_tri_id)   REFERENCES vi_tri_kho(id),
    CONSTRAINT ck_lohang_soluong CHECK (so_luong_ton >= 0)
) ENGINE=InnoDB;

-- =====================================================================
--  5. PHIẾU NHẬP  (FR3 - Nhập kho, UC3)
-- =====================================================================
CREATE TABLE phieu_nhap (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    ma_phieu        VARCHAR(30)  NOT NULL,
    ngay_nhap       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nguoi_nhap_id   BIGINT       NOT NULL,          -- thủ kho thực hiện
    nha_cung_cap    VARCHAR(150),
    ghi_chu         VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_phieunhap_ma (ma_phieu),
    KEY idx_phieunhap_ngay (ngay_nhap),
    CONSTRAINT fk_phieunhap_nguoi FOREIGN KEY (nguoi_nhap_id) REFERENCES nguoi_dung(id)
) ENGINE=InnoDB;

-- =====================================================================
--  6. CHI TIẾT NHẬP  (mỗi dòng ứng với 1 lô được nhập trong phiếu)
-- =====================================================================
CREATE TABLE chi_tiet_nhap (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    phieu_nhap_id   BIGINT       NOT NULL,
    lo_hang_id      BIGINT       NOT NULL,
    so_luong        INT          NOT NULL,
    don_gia         DECIMAL(12,2) DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_ctnhap_phieu (phieu_nhap_id),
    CONSTRAINT fk_ctnhap_phieu FOREIGN KEY (phieu_nhap_id) REFERENCES phieu_nhap(id),
    CONSTRAINT fk_ctnhap_lo    FOREIGN KEY (lo_hang_id)    REFERENCES lo_hang(id),
    CONSTRAINT ck_ctnhap_soluong CHECK (so_luong > 0)
) ENGINE=InnoDB;

-- =====================================================================
--  7. PHIẾU XUẤT  (FR4 - Xuất kho FEFO, UC4)
--     loai_xuat phân biệt xuất bán và xuất huỷ (hàng quá hạn) - xem UC4 luồng phụ 4a
-- =====================================================================
CREATE TABLE phieu_xuat (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    ma_phieu        VARCHAR(30)  NOT NULL,
    ngay_xuat       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nguoi_xuat_id   BIGINT       NOT NULL,
    loai_xuat       ENUM('XUAT_BAN','XUAT_HUY') NOT NULL DEFAULT 'XUAT_BAN',
    ly_do           VARCHAR(255),                   -- lý do huỷ (nếu XUAT_HUY)
    ghi_chu         VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_phieuxuat_ma (ma_phieu),
    KEY idx_phieuxuat_ngay (ngay_xuat),
    CONSTRAINT fk_phieuxuat_nguoi FOREIGN KEY (nguoi_xuat_id) REFERENCES nguoi_dung(id)
) ENGINE=InnoDB;

-- =====================================================================
--  8. CHI TIẾT XUẤT  (mỗi dòng ứng với việc trừ hàng từ 1 lô cụ thể)
--     Nhờ tách theo lô nên báo cáo hàng huỷ do quá hạn (FR7) truy được về đúng lô.
-- =====================================================================
CREATE TABLE chi_tiet_xuat (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    phieu_xuat_id   BIGINT       NOT NULL,
    lo_hang_id      BIGINT       NOT NULL,
    so_luong        INT          NOT NULL,
    don_gia         DECIMAL(12,2) DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_ctxuat_phieu (phieu_xuat_id),
    CONSTRAINT fk_ctxuat_phieu FOREIGN KEY (phieu_xuat_id) REFERENCES phieu_xuat(id),
    CONSTRAINT fk_ctxuat_lo    FOREIGN KEY (lo_hang_id)    REFERENCES lo_hang(id),
    CONSTRAINT ck_ctxuat_soluong CHECK (so_luong > 0)
) ENGINE=InnoDB;

-- =====================================================================
--  9. CẢNH BÁO  (FR6 - cảnh báo hạn dùng, FR5 - cảnh báo tồn tối thiểu, UC6)
--     Bản ghi do tác nhân "Hệ thống (thời gian)" sinh ra định kỳ.
-- =====================================================================
CREATE TABLE canh_bao (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    lo_hang_id      BIGINT,
    san_pham_id     BIGINT,
    loai_canh_bao   ENUM('SAP_HET_HAN','QUA_HAN','TON_TOI_THIEU') NOT NULL,
    noi_dung        VARCHAR(255) NOT NULL,
    ngay_canh_bao   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    da_xu_ly        TINYINT      NOT NULL DEFAULT 0, -- 0 = chưa xử lý, 1 = đã xử lý
    PRIMARY KEY (id),
    KEY idx_canhbao_lo (lo_hang_id),
    CONSTRAINT fk_canhbao_lo FOREIGN KEY (lo_hang_id) REFERENCES lo_hang(id),
    CONSTRAINT fk_canhbao_sp FOREIGN KEY (san_pham_id) REFERENCES san_pham(id)
) ENGINE=InnoDB;


-- =====================================================================
--  DỮ LIỆU MẪU (để demo & bảo vệ đồ án)
-- =====================================================================

-- Người dùng (mật khẩu mẫu để dạng thô cho dễ test; thực tế nên mã hoá BCrypt)
INSERT INTO nguoi_dung (ten_dang_nhap, mat_khau, ho_ten, vai_tro) VALUES
('thukho',  '123456', 'Nguyễn Văn A', 'THU_KHO'),
('quanly',  '123456', 'Trần Thị B',   'QUAN_LY');

-- Sản phẩm
INSERT INTO san_pham (ma_san_pham, ten_san_pham, don_vi_tinh, nhiet_do_min, nhiet_do_max, ton_toi_thieu) VALUES
('SP001', 'Cá basa phi lê',    'kg',    -18.00, -15.00, 50),
('SP002', 'Tôm sú đông lạnh',  'kg',    -20.00, -18.00, 30),
('SP003', 'Kem hộp vani',      'hộp',   -22.00, -18.00, 100),
('SP004', 'Xúc xích đông lạnh','gói',   -18.00, -12.00, 40);

-- Vị trí kho
INSERT INTO vi_tri_kho (ma_vi_tri, ten_vi_tri, nhiet_do_hien_tai) VALUES
('VT01', 'Kho A - Kệ 1', -18.00),
('VT02', 'Kho A - Kệ 2', -20.00),
('VT03', 'Kho B - Kệ 1', -18.00);

-- Lô hàng (chú ý han_su_dung khác nhau để minh hoạ FEFO & cảnh báo)
INSERT INTO lo_hang (ma_lo, san_pham_id, vi_tri_id, ngay_san_xuat, han_su_dung, so_luong_nhap, so_luong_ton) VALUES
('LO-CA-001',  1, 1, '2026-08-01', '2026-09-22', 100, 100), -- sắp hết hạn (cảnh báo)
('LO-CA-002',  1, 1, '2026-09-01', '2026-12-01', 120, 120),
('LO-TOM-001', 2, 2, '2026-08-15', '2026-09-19', 80,  80),  -- rất sát hạn
('LO-KEM-001', 3, 3, '2026-07-01', '2027-01-01', 200, 200),
('LO-XX-001',  4, 1, '2026-06-01', '2026-09-10', 60,  60);  -- đã quá hạn (chỉ được xuất huỷ)

-- Một phiếu nhập mẫu
INSERT INTO phieu_nhap (ma_phieu, nguoi_nhap_id, nha_cung_cap, ghi_chu) VALUES
('PN-0001', 1, 'Công ty Thủy sản Miền Tây', 'Nhập đầu kỳ');
INSERT INTO chi_tiet_nhap (phieu_nhap_id, lo_hang_id, so_luong, don_gia) VALUES
(1, 1, 100, 45000),
(1, 2, 120, 45000);


-- =====================================================================
--  MỘT SỐ CÂU TRUY VẤN NGHIỆP VỤ MẪU (tham khảo khi code Spring Boot)
-- =====================================================================

-- (FR4) Lấy danh sách lô còn hàng của 1 sản phẩm, sắp xếp FEFO (hết hạn trước ra trước):
-- SELECT * FROM lo_hang
-- WHERE san_pham_id = ? AND so_luong_ton > 0
-- ORDER BY han_su_dung ASC;

-- (FR5) Tồn kho hiện tại theo từng sản phẩm:
-- SELECT sp.ma_san_pham, sp.ten_san_pham, SUM(l.so_luong_ton) AS tong_ton
-- FROM san_pham sp LEFT JOIN lo_hang l ON l.san_pham_id = sp.id
-- GROUP BY sp.id;

-- (FR5) Sản phẩm dưới mức tồn tối thiểu:
-- SELECT sp.ten_san_pham, SUM(l.so_luong_ton) AS tong_ton, sp.ton_toi_thieu
-- FROM san_pham sp LEFT JOIN lo_hang l ON l.san_pham_id = sp.id
-- GROUP BY sp.id HAVING tong_ton < sp.ton_toi_thieu;

-- (FR6) Các lô sắp hết hạn trong 7 ngày tới (còn hàng):
-- SELECT * FROM lo_hang
-- WHERE so_luong_ton > 0
--   AND han_su_dung BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY);

-- (FR6) Các lô đã quá hạn (còn hàng - phải xuất huỷ):
-- SELECT * FROM lo_hang WHERE so_luong_ton > 0 AND han_su_dung < CURDATE();
