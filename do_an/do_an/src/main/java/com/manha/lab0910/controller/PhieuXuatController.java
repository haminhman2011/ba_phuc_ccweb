package com.manha.lab0910.controller;

import com.manha.lab0910.entity.PhieuXuat;
import com.manha.lab0910.form.DongPhanBo;
import com.manha.lab0910.form.PhieuXuatForm;
import com.manha.lab0910.service.NguoiDungChiTiet;
import com.manha.lab0910.service.PhieuXuatService;
import com.manha.lab0910.service.SanPhamService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/phieu-xuat")
public class PhieuXuatController {

    private static final Logger log = LoggerFactory.getLogger(PhieuXuatController.class);

    private final PhieuXuatService phieuXuatService;
    private final SanPhamService sanPhamService;

    public PhieuXuatController(PhieuXuatService phieuXuatService, SanPhamService sanPhamService) {
        this.phieuXuatService = phieuXuatService;
        this.sanPhamService = sanPhamService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    /** Bước 1: màn hình nhập yêu cầu xuất. */
    @GetMapping
    public String form(Model model) {
        model.addAttribute("phieuXuatForm", new PhieuXuatForm());
        napDuLieuChon(model);
        return "phieu-xuat/form";
    }

    /**
     * Bước 2: tính bảng phân bổ FEFO và hiện cho người dùng xem.
     * Chưa ghi gì xuống DB ở bước này.
     */
    @PostMapping("/tinh-phan-bo")
    public String tinhPhanBo(@Valid @ModelAttribute("phieuXuatForm") PhieuXuatForm form,
                             BindingResult ketQua,
                             Model model) {

        kiemTra(form, ketQua);

        if (ketQua.hasErrors()) {
            napDuLieuChon(model);
            return "phieu-xuat/form";
        }

        List<DongPhanBo> phanBo = phieuXuatService.tinhPhanBoFEFO(form.getSanPhamId(), form.getSoLuong());

        // (4a) Xuất bán mà FEFO lại chạm vào lô quá hạn thì chặn, bắt chuyển sang xuất huỷ
        if (form.getLoaiXuat() == PhieuXuat.LoaiXuat.XUAT_BAN) {
            for (DongPhanBo dong : phanBo) {
                if (dong.isQuaHan()) {
                    ketQua.rejectValue("loaiXuat", "quaHan", "Lô đã quá hạn, chỉ được xuất huỷ");
                    napDuLieuChon(model);
                    return "phieu-xuat/form";
                }
            }
        }

        model.addAttribute("phanBo", phanBo);
        model.addAttribute("tenSanPham", sanPhamService.timTheoId(form.getSanPhamId()).getTenSanPham());
        napDuLieuChon(model);
        return "phieu-xuat/form";
    }

    /** Bước 3: người dùng bấm "Xác nhận xuất" thì mới thật sự ghi xuống DB. */
    @PostMapping("/xac-nhan")
    public String xacNhan(@Valid @ModelAttribute("phieuXuatForm") PhieuXuatForm form,
                          BindingResult ketQua,
                          @AuthenticationPrincipal NguoiDungChiTiet nguoiDung,
                          Model model,
                          RedirectAttributes ra) {

        kiemTra(form, ketQua);

        if (ketQua.hasErrors()) {
            napDuLieuChon(model);
            return "phieu-xuat/form";
        }

        try {
            PhieuXuat phieu = phieuXuatService.luuPhieuXuat(form, nguoiDung.getId());
            ra.addFlashAttribute("thongBao",
                    "Xuất kho thành công. Mã phiếu: " + phieu.getMaPhieu());
            return "redirect:/phieu-xuat";
        } catch (IllegalStateException e) {
            // Lỗi nghiệp vụ: không đủ tồn, lô quá hạn, tồn vừa bị đổi...
            model.addAttribute("loi", e.getMessage());
            napDuLieuChon(model);
            return "phieu-xuat/form";
        } catch (RuntimeException e) {
            log.error("Lỗi khi lưu phiếu xuất", e);
            model.addAttribute("loi", "Đã xảy ra lỗi, vui lòng thử lại");
            napDuLieuChon(model);
            return "phieu-xuat/form";
        }
    }

    /** Các quy tắc dùng chung cho cả bước tính phân bổ lẫn bước xác nhận. */
    private void kiemTra(PhieuXuatForm form, BindingResult ketQua) {
        // Lý do bắt buộc khi xuất huỷ
        if (form.getLoaiXuat() == PhieuXuat.LoaiXuat.XUAT_HUY
                && (form.getLyDo() == null || form.getLyDo().isBlank())) {
            ketQua.rejectValue("lyDo", "batBuoc", "Vui lòng nhập lý do huỷ");
        }

        if (form.getSanPhamId() != null && !sanPhamService.tonTai(form.getSanPhamId())) {
            ketQua.rejectValue("sanPhamId", "khongTonTai", "Sản phẩm không còn tồn tại, vui lòng chọn lại");
            return;
        }

        // (3a) Không cho xuất quá tồn
        if (form.getSanPhamId() != null && form.getSoLuong() != null && form.getSoLuong() > 0) {
            int tongTon = phieuXuatService.tongTonHienCo(form.getSanPhamId());
            if (form.getSoLuong() > tongTon) {
                ketQua.rejectValue("soLuong", "khongDuTon", "Không đủ tồn kho (còn " + tongTon + ")");
            }
        }
    }

    private void napDuLieuChon(Model model) {
        model.addAttribute("dsSanPham", sanPhamService.laySanPhamDangKinhDoanh());
        model.addAttribute("maPhieuDuKien", phieuXuatService.sinhMaPhieuMoi());
    }
}
