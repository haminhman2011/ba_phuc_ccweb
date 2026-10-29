/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 100432
 Source Host           : localhost:3306
 Source Schema         : dang_ky_mon_hoc

 Target Server Type    : MySQL
 Target Server Version : 100432
 File Encoding         : 65001

 Date: 29/10/2026 15:42:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dang_ky
-- ----------------------------
DROP TABLE IF EXISTS `dang_ky`;
CREATE TABLE `dang_ky`  (
  `HocKy` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `Diem` float NULL DEFAULT NULL,
  `NgayDangKy` date NULL DEFAULT NULL,
  `MaMH` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `MaSV` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `MaGV` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`HocKy`, `MaMH`, `MaSV`) USING BTREE,
  INDEX `FK_DK_MH`(`MaMH`) USING BTREE,
  INDEX `FK_DK_SV`(`MaSV`) USING BTREE,
  INDEX `FK_DK_PC`(`HocKy`, `MaGV`, `MaMH`) USING BTREE,
  CONSTRAINT `FK_DK_MH` FOREIGN KEY (`MaMH`) REFERENCES `mon_hoc` (`MaMH`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK_DK_PC` FOREIGN KEY (`HocKy`, `MaGV`, `MaMH`) REFERENCES `phan_cong` (`HocKy`, `MaGV`, `MaMH`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK_DK_SV` FOREIGN KEY (`MaSV`) REFERENCES `sinh_vien` (`MaSV`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dang_ky
-- ----------------------------
INSERT INTO `dang_ky` VALUES ('HK1-2026', 8.5, '2026-09-05', 'CSDL', 'SV01', 'GV01');
INSERT INTO `dang_ky` VALUES ('HK1-2026', 7.3, '2026-09-06', 'CSDL', 'SV02', 'GV01');
INSERT INTO `dang_ky` VALUES ('HK1-2026', NULL, '2026-09-06', 'CSDL', 'SV03', 'GV01');
INSERT INTO `dang_ky` VALUES ('HK1-2026', NULL, '2026-09-08', 'KTVM', 'SV08', 'GV03');
INSERT INTO `dang_ky` VALUES ('HK1-2026', NULL, '2026-09-06', 'LTJ', 'SV02', 'GV02');
INSERT INTO `dang_ky` VALUES ('HK1-2026', NULL, '2026-09-08', 'LTJ', 'SV05', 'GV02');
INSERT INTO `dang_ky` VALUES ('HK1-2026', 9, '2026-09-05', 'LTW', 'SV01', 'GV01');
INSERT INTO `dang_ky` VALUES ('HK1-2026', NULL, '2026-09-07', 'LTW', 'SV03', 'GV01');
INSERT INTO `dang_ky` VALUES ('HK1-2026', NULL, '2026-09-07', 'LTW', 'SV04', 'GV01');
INSERT INTO `dang_ky` VALUES ('HK1-2026', NULL, '2026-09-08', 'NLKT', 'SV08', 'GV04');
INSERT INTO `dang_ky` VALUES ('HK1-2026', NULL, '2026-09-09', 'NLKT', 'SV09', 'GV04');
INSERT INTO `dang_ky` VALUES ('HK2-2025', 7.5, '2026-01-10', 'CTDL', 'SV01', 'GV01');
INSERT INTO `dang_ky` VALUES ('HK2-2025', 9, '2026-01-12', 'CTDL', 'SV02', 'GV01');
INSERT INTO `dang_ky` VALUES ('HK2-2025', 4, '2026-01-10', 'KTVM', 'SV01', 'GV03');
INSERT INTO `dang_ky` VALUES ('HK2-2025', 6.5, '2026-01-12', 'KTVM', 'SV08', 'GV03');
INSERT INTO `dang_ky` VALUES ('HK2-2025', 6, '2026-01-11', 'MMT', 'SV01', 'GV02');

-- ----------------------------
-- Table structure for giang_vien
-- ----------------------------
DROP TABLE IF EXISTS `giang_vien`;
CREATE TABLE `giang_vien`  (
  `MaGV` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `HoTen` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `HocVi` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `MaKhoa` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`MaGV`) USING BTREE,
  INDEX `FK_GV_KHOA`(`MaKhoa`) USING BTREE,
  CONSTRAINT `FK_GV_KHOA` FOREIGN KEY (`MaKhoa`) REFERENCES `khoa` (`MaKhoa`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of giang_vien
-- ----------------------------
INSERT INTO `giang_vien` VALUES ('GV01', 'Trần Văn Hùng', 'Tiến sĩ', 'CNTT');
INSERT INTO `giang_vien` VALUES ('GV02', 'Nguyễn Thị Mai', 'Thạc sĩ', 'CNTT');
INSERT INTO `giang_vien` VALUES ('GV03', 'Lê Văn Nam', 'Thạc sĩ', 'KT');
INSERT INTO `giang_vien` VALUES ('GV04', 'Phạm Thị Oanh', 'Phó giáo sư', 'KT');
INSERT INTO `giang_vien` VALUES ('GV05', 'Test', NULL, 'CNTT');

-- ----------------------------
-- Table structure for hoc_ky
-- ----------------------------
DROP TABLE IF EXISTS `hoc_ky`;
CREATE TABLE `hoc_ky`  (
  `MaHK` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `NgayBatDau` date NOT NULL,
  `NgayKetThuc` date NOT NULL,
  PRIMARY KEY (`MaHK`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hoc_ky
-- ----------------------------
INSERT INTO `hoc_ky` VALUES ('HK1-2026', '2026-09-07', '2027-01-10');
INSERT INTO `hoc_ky` VALUES ('HK2-2025', '2026-02-02', '2026-06-07');
INSERT INTO `hoc_ky` VALUES ('HK2-2026', '2027-02-01', '2027-06-06');

-- ----------------------------
-- Table structure for khoa
-- ----------------------------
DROP TABLE IF EXISTS `khoa`;
CREATE TABLE `khoa`  (
  `MaKhoa` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TenKhoa` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`MaKhoa`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of khoa
-- ----------------------------
INSERT INTO `khoa` VALUES ('CNTT', 'Công nghệ thông tin');
INSERT INTO `khoa` VALUES ('KT', 'Kinh tế');

-- ----------------------------
-- Table structure for lich_hoc
-- ----------------------------
DROP TABLE IF EXISTS `lich_hoc`;
CREATE TABLE `lich_hoc`  (
  `MaLich` int NOT NULL AUTO_INCREMENT,
  `Ghim` bit(1) NOT NULL,
  `SoTiet` int NOT NULL,
  `Thu` int NOT NULL,
  `TietBatDau` int NOT NULL,
  `TrangThai` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `HocKy` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `MaGV` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `MaMH` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `MaPhong` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TuanBatDau` int NOT NULL,
  `TuanKetThuc` int NOT NULL,
  PRIMARY KEY (`MaLich`) USING BTREE,
  INDEX `FK_LH_PC`(`HocKy`, `MaGV`, `MaMH`) USING BTREE,
  INDEX `FK_LH_PHONG`(`MaPhong`) USING BTREE,
  CONSTRAINT `FK_LH_PC` FOREIGN KEY (`HocKy`, `MaGV`, `MaMH`) REFERENCES `phan_cong` (`HocKy`, `MaGV`, `MaMH`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK_LH_PHONG` FOREIGN KEY (`MaPhong`) REFERENCES `phong` (`MaPhong`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lich_hoc
-- ----------------------------
INSERT INTO `lich_hoc` VALUES (1, b'0', 4, 2, 1, 'CHINH_THUC', 'HK1-2026', 'GV02', 'CTDL', 'A101', 1, 15);
INSERT INTO `lich_hoc` VALUES (2, b'0', 3, 3, 1, 'CHINH_THUC', 'HK1-2026', 'GV02', 'CSDL', 'A101', 1, 15);
INSERT INTO `lich_hoc` VALUES (3, b'0', 3, 4, 1, 'CHINH_THUC', 'HK1-2026', 'GV02', 'LTJ', 'A101', 1, 15);
INSERT INTO `lich_hoc` VALUES (4, b'0', 3, 2, 1, 'CHINH_THUC', 'HK1-2026', 'GV01', 'CSDL', 'A102', 1, 15);
INSERT INTO `lich_hoc` VALUES (5, b'0', 3, 3, 2, 'CHINH_THUC', 'HK1-2026', 'GV01', 'LTW', 'A102', 1, 15);
INSERT INTO `lich_hoc` VALUES (6, b'0', 3, 2, 1, 'CHINH_THUC', 'HK1-2026', 'GV04', 'NLKT', 'B201', 1, 15);
INSERT INTO `lich_hoc` VALUES (7, b'0', 2, 2, 4, 'CHINH_THUC', 'HK1-2026', 'GV03', 'KTVM', 'A102', 1, 15);
INSERT INTO `lich_hoc` VALUES (11, b'0', 4, 2, 1, 'NHAP', 'HK2-2026', 'GV02', 'CTDL', 'A101', 1, 15);
INSERT INTO `lich_hoc` VALUES (12, b'0', 3, 2, 1, 'NHAP', 'HK2-2026', 'GV01', 'CSDL', 'A102', 1, 15);
INSERT INTO `lich_hoc` VALUES (13, b'0', 3, 3, 1, 'NHAP', 'HK2-2026', 'GV01', 'LTW', 'A101', 1, 15);
INSERT INTO `lich_hoc` VALUES (14, b'0', 3, 4, 1, 'NHAP', 'HK2-2026', 'GV01', 'PTTK', 'A101', 1, 15);
INSERT INTO `lich_hoc` VALUES (15, b'0', 3, 3, 1, 'NHAP', 'HK2-2026', 'GV02', 'LTJ', 'A102', 1, 15);
INSERT INTO `lich_hoc` VALUES (16, b'0', 3, 2, 1, 'NHAP', 'HK2-2026', 'GV03', 'MMT', 'B201', 1, 15);
INSERT INTO `lich_hoc` VALUES (17, b'0', 3, 2, 1, 'NHAP', 'HK2-2026', 'GV04', 'NLKT', 'B202', 1, 15);
INSERT INTO `lich_hoc` VALUES (18, b'0', 2, 3, 1, 'NHAP', 'HK2-2026', 'GV03', 'KTVM', 'B201', 1, 15);

-- ----------------------------
-- Table structure for lop
-- ----------------------------
DROP TABLE IF EXISTS `lop`;
CREATE TABLE `lop`  (
  `MaLop` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `KhoaHoc` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `TenLop` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `MaKhoa` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`MaLop`) USING BTREE,
  INDEX `FK_LOP_KHOA`(`MaKhoa`) USING BTREE,
  CONSTRAINT `FK_LOP_KHOA` FOREIGN KEY (`MaKhoa`) REFERENCES `khoa` (`MaKhoa`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lop
-- ----------------------------
INSERT INTO `lop` VALUES ('CNTT01', 'K2024', 'Công nghệ thông tin 01', 'CNTT');
INSERT INTO `lop` VALUES ('CNTT02', 'K2025', 'Công nghệ thông tin 02', 'CNTT');
INSERT INTO `lop` VALUES ('KT01', 'K2024', 'Kinh tế 01', 'KT');

-- ----------------------------
-- Table structure for mon_hoc
-- ----------------------------
DROP TABLE IF EXISTS `mon_hoc`;
CREATE TABLE `mon_hoc`  (
  `MaMH` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `SoTinChi` int NOT NULL,
  `TenMH` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `MaKhoa` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`MaMH`) USING BTREE,
  INDEX `FK_MH_KHOA`(`MaKhoa`) USING BTREE,
  CONSTRAINT `FK_MH_KHOA` FOREIGN KEY (`MaKhoa`) REFERENCES `khoa` (`MaKhoa`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mon_hoc
-- ----------------------------
INSERT INTO `mon_hoc` VALUES ('CSDL', 3, 'Cơ sở dữ liệu', 'CNTT');
INSERT INTO `mon_hoc` VALUES ('CTDL', 4, 'Cấu trúc dữ liệu và giải thuật', 'CNTT');
INSERT INTO `mon_hoc` VALUES ('KTVM', 2, 'Kinh tế vi mô', 'KT');
INSERT INTO `mon_hoc` VALUES ('LTJ', 3, 'Lập trình Java', 'CNTT');
INSERT INTO `mon_hoc` VALUES ('LTW', 3, 'Lập trình web', 'CNTT');
INSERT INTO `mon_hoc` VALUES ('MMT', 3, 'Mạng máy tính', 'CNTT');
INSERT INTO `mon_hoc` VALUES ('NLKT', 3, 'Nguyên lý kế toán', 'KT');
INSERT INTO `mon_hoc` VALUES ('PTTK', 3, 'Phân tích thiết kế hệ thống', 'CNTT');

-- ----------------------------
-- Table structure for phan_cong
-- ----------------------------
DROP TABLE IF EXISTS `phan_cong`;
CREATE TABLE `phan_cong`  (
  `HocKy` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `MaGV` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `MaMH` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`HocKy`, `MaGV`, `MaMH`) USING BTREE,
  INDEX `FK_PC_GV`(`MaGV`) USING BTREE,
  INDEX `FK_PC_MH`(`MaMH`) USING BTREE,
  CONSTRAINT `FK_PC_GV` FOREIGN KEY (`MaGV`) REFERENCES `giang_vien` (`MaGV`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK_PC_HK` FOREIGN KEY (`HocKy`) REFERENCES `hoc_ky` (`MaHK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK_PC_MH` FOREIGN KEY (`MaMH`) REFERENCES `mon_hoc` (`MaMH`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of phan_cong
-- ----------------------------
INSERT INTO `phan_cong` VALUES ('HK1-2026', 'GV01', 'CSDL');
INSERT INTO `phan_cong` VALUES ('HK1-2026', 'GV01', 'LTW');
INSERT INTO `phan_cong` VALUES ('HK1-2026', 'GV02', 'CSDL');
INSERT INTO `phan_cong` VALUES ('HK1-2026', 'GV02', 'CTDL');
INSERT INTO `phan_cong` VALUES ('HK1-2026', 'GV02', 'LTJ');
INSERT INTO `phan_cong` VALUES ('HK1-2026', 'GV03', 'KTVM');
INSERT INTO `phan_cong` VALUES ('HK1-2026', 'GV04', 'NLKT');
INSERT INTO `phan_cong` VALUES ('HK2-2025', 'GV01', 'CTDL');
INSERT INTO `phan_cong` VALUES ('HK2-2025', 'GV02', 'MMT');
INSERT INTO `phan_cong` VALUES ('HK2-2025', 'GV03', 'KTVM');
INSERT INTO `phan_cong` VALUES ('HK2-2026', 'GV01', 'CSDL');
INSERT INTO `phan_cong` VALUES ('HK2-2026', 'GV01', 'LTW');
INSERT INTO `phan_cong` VALUES ('HK2-2026', 'GV01', 'PTTK');
INSERT INTO `phan_cong` VALUES ('HK2-2026', 'GV02', 'CTDL');
INSERT INTO `phan_cong` VALUES ('HK2-2026', 'GV02', 'LTJ');
INSERT INTO `phan_cong` VALUES ('HK2-2026', 'GV03', 'KTVM');
INSERT INTO `phan_cong` VALUES ('HK2-2026', 'GV03', 'MMT');
INSERT INTO `phan_cong` VALUES ('HK2-2026', 'GV04', 'NLKT');

-- ----------------------------
-- Table structure for phong
-- ----------------------------
DROP TABLE IF EXISTS `phong`;
CREATE TABLE `phong`  (
  `MaPhong` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `SucChua` int NOT NULL,
  `TenPhong` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`MaPhong`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of phong
-- ----------------------------
INSERT INTO `phong` VALUES ('A101', 40, 'Phòng A101');
INSERT INTO `phong` VALUES ('A102', 45, 'Phòng A102');
INSERT INTO `phong` VALUES ('B201', 50, 'Phòng B201');
INSERT INTO `phong` VALUES ('B202', 60, 'Phòng B202');
INSERT INTO `phong` VALUES ('C301', 70, 'Hội trường C301');
INSERT INTO `phong` VALUES ('C302', 80, 'Hội trường C302');

-- ----------------------------
-- Table structure for sinh_vien
-- ----------------------------
DROP TABLE IF EXISTS `sinh_vien`;
CREATE TABLE `sinh_vien`  (
  `MaSV` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `GioiTinh` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `HoTen` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `NgaySinh` date NULL DEFAULT NULL,
  `MaLop` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`MaSV`) USING BTREE,
  INDEX `FK_SV_LOP`(`MaLop`) USING BTREE,
  CONSTRAINT `FK_SV_LOP` FOREIGN KEY (`MaLop`) REFERENCES `lop` (`MaLop`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sinh_vien
-- ----------------------------
INSERT INTO `sinh_vien` VALUES ('SV01', 'Nam', 'Nguyen Van An', '2005-03-15', 'CNTT02');
INSERT INTO `sinh_vien` VALUES ('SV02', 'Nữ', 'Trần Thị Bình', '2005-07-22', 'CNTT01');
INSERT INTO `sinh_vien` VALUES ('SV03', 'Nam', 'Lê Hoàng Cường', '2005-11-02', 'CNTT01');
INSERT INTO `sinh_vien` VALUES ('SV04', 'Nữ', 'Phạm Thị Dung', '2005-01-30', 'CNTT01');
INSERT INTO `sinh_vien` VALUES ('SV05', 'Nam', 'Hoàng Minh Đức', '2006-04-12', 'CNTT02');
INSERT INTO `sinh_vien` VALUES ('SV06', 'Nữ', 'Vũ Thị Hà', '2006-09-08', 'CNTT02');
INSERT INTO `sinh_vien` VALUES ('SV07', 'Nam', 'Đặng Quốc Huy', '2006-12-19', 'CNTT02');
INSERT INTO `sinh_vien` VALUES ('SV08', 'Nữ', 'Bùi Thị Lan', '2005-05-05', 'KT01');
INSERT INTO `sinh_vien` VALUES ('SV09', 'Nam', 'Đỗ Văn Minh', '2005-08-25', 'KT01');
INSERT INTO `sinh_vien` VALUES ('SV10', 'Nữ', 'Ngô Thị Ngọc', '2005-10-10', 'KT01');

-- ----------------------------
-- Table structure for tai_khoan
-- ----------------------------
DROP TABLE IF EXISTS `tai_khoan`;
CREATE TABLE `tai_khoan`  (
  `TenDangNhap` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `MatKhau` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TrangThai` bit(1) NOT NULL,
  `MaGV` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `MaQuyen` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `MaSV` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`TenDangNhap`) USING BTREE,
  INDEX `FK_TK_GV`(`MaGV`) USING BTREE,
  INDEX `FK_TK_QUYEN`(`MaQuyen`) USING BTREE,
  INDEX `FK_TK_SV`(`MaSV`) USING BTREE,
  CONSTRAINT `FK_TK_GV` FOREIGN KEY (`MaGV`) REFERENCES `giang_vien` (`MaGV`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK_TK_SV` FOREIGN KEY (`MaSV`) REFERENCES `sinh_vien` (`MaSV`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tai_khoan
-- ----------------------------
INSERT INTO `tai_khoan` VALUES ('admin', '$2a$10$YALVyGhGOVMmMpVLAXRV4.akxjcbYDp5iHHDLdHqbZ46GbOZUNQmC', b'1', NULL, 'ADMIN', NULL);
INSERT INTO `tai_khoan` VALUES ('gv01', '$2a$10$AbXkvgcJ9VUz6OnzPnfFvensmQf18vl9a98Zxi9E96RoMHTbfgBuS', b'1', 'GV01', 'GV', NULL);
INSERT INTO `tai_khoan` VALUES ('gv02', '$2a$10$AbXkvgcJ9VUz6OnzPnfFvensmQf18vl9a98Zxi9E96RoMHTbfgBuS', b'1', 'GV02', 'GV', NULL);
INSERT INTO `tai_khoan` VALUES ('gv03', '$2a$10$AbXkvgcJ9VUz6OnzPnfFvensmQf18vl9a98Zxi9E96RoMHTbfgBuS', b'1', 'GV03', 'GV', NULL);
INSERT INTO `tai_khoan` VALUES ('gv04', '$2a$10$AbXkvgcJ9VUz6OnzPnfFvensmQf18vl9a98Zxi9E96RoMHTbfgBuS', b'1', 'GV04', 'GV', NULL);
INSERT INTO `tai_khoan` VALUES ('gv05', '$2a$10$Uo4JYA86wWaCz7Nl2/ZvhOXFAnBubshbQHLcnmhiX6Rhy6CPmks.i', b'1', 'GV05', 'GV', NULL);
INSERT INTO `tai_khoan` VALUES ('sv01', '$2a$10$AbXkvgcJ9VUz6OnzPnfFvensmQf18vl9a98Zxi9E96RoMHTbfgBuS', b'1', NULL, 'SV', 'SV01');
INSERT INTO `tai_khoan` VALUES ('sv02', '$2a$10$NFN6/ryqX9yDuF8WqbkZT.zLPEaDc7jUt9Bq0oBp8P89Y.lKapkAG', b'1', NULL, 'SV', 'SV02');
INSERT INTO `tai_khoan` VALUES ('sv03', '$2a$10$AbXkvgcJ9VUz6OnzPnfFvensmQf18vl9a98Zxi9E96RoMHTbfgBuS', b'1', NULL, 'SV', 'SV03');
INSERT INTO `tai_khoan` VALUES ('sv04', '$2a$10$AbXkvgcJ9VUz6OnzPnfFvensmQf18vl9a98Zxi9E96RoMHTbfgBuS', b'1', NULL, 'SV', 'SV04');
INSERT INTO `tai_khoan` VALUES ('sv05', '$2a$10$AbXkvgcJ9VUz6OnzPnfFvensmQf18vl9a98Zxi9E96RoMHTbfgBuS', b'1', NULL, 'SV', 'SV05');
INSERT INTO `tai_khoan` VALUES ('sv06', '$2a$10$AbXkvgcJ9VUz6OnzPnfFvensmQf18vl9a98Zxi9E96RoMHTbfgBuS', b'1', NULL, 'SV', 'SV06');
INSERT INTO `tai_khoan` VALUES ('sv07', '$2a$10$AbXkvgcJ9VUz6OnzPnfFvensmQf18vl9a98Zxi9E96RoMHTbfgBuS', b'1', NULL, 'SV', 'SV07');
INSERT INTO `tai_khoan` VALUES ('sv08', '$2a$10$AbXkvgcJ9VUz6OnzPnfFvensmQf18vl9a98Zxi9E96RoMHTbfgBuS', b'1', NULL, 'SV', 'SV08');
INSERT INTO `tai_khoan` VALUES ('sv09', '$2a$10$AbXkvgcJ9VUz6OnzPnfFvensmQf18vl9a98Zxi9E96RoMHTbfgBuS', b'1', NULL, 'SV', 'SV09');
INSERT INTO `tai_khoan` VALUES ('sv10', '$2a$10$AbXkvgcJ9VUz6OnzPnfFvensmQf18vl9a98Zxi9E96RoMHTbfgBuS', b'0', NULL, 'SV', 'SV10');

SET FOREIGN_KEY_CHECKS = 1;
