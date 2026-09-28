package com.manha.lab0910.controller;

import com.manha.lab0910.entity.SanPham;
import com.manha.lab0910.service.SanPhamService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/san-pham")
public class SanPhamController {

    // Danh sách đơn vị tính cho ô chọn trong form
    private static final List<String> DON_VI_TINH = List.of("kg", "thùng", "gói", "hộp", "cái", "lít");

    private final SanPhamService sanPhamService;

    public SanPhamController(SanPhamService sanPhamService) {
        this.sanPhamService = sanPhamService;
    }

    // Màn hình danh sách + ô tìm kiếm
    @GetMapping
    public String danhSach(@RequestParam(required = false) String tuKhoa, Model model) {
        model.addAttribute("danhSach", sanPhamService.timKiem(tuKhoa));
        model.addAttribute("tuKhoa", tuKhoa);
        return "san-pham/danh-sach";
    }

    @GetMapping("/them")
    public String formThem(Model model) {
        model.addAttribute("sanPham", new SanPham());
        model.addAttribute("dsDonViTinh", DON_VI_TINH);
        return "san-pham/form";
    }

    @GetMapping("/sua/{id}")
    public String formSua(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("sanPham", sanPhamService.timTheoId(id));
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("loi", e.getMessage());
            return "redirect:/san-pham";
        }
        model.addAttribute("dsDonViTinh", DON_VI_TINH);
        return "san-pham/form";
    }

    /**
     * Lưu cả thêm mới lẫn sửa. Phân biệt bằng việc sanPham.id có giá trị hay chưa.
     * @Valid chạy các ràng buộc khai trên entity, lỗi đổ vào BindingResult.
     */
    @PostMapping("/luu")
    public String luu(@Valid @ModelAttribute("sanPham") SanPham sanPham,
                      BindingResult ketQua,
                      Model model,
                      RedirectAttributes ra) {

        boolean themMoi = sanPham.getId() == null;

        // (3a) Mã sản phẩm không được trùng với sản phẩm khác
        if (!ketQua.hasFieldErrors("maSanPham")
                && sanPhamService.maDaTonTai(sanPham.getMaSanPham(), sanPham.getId())) {
            ketQua.rejectValue("maSanPham", "trung", "Mã sản phẩm đã tồn tại");
        }

        // (3b) Nhập cả hai nhiệt độ thì min phải <= max
        if (sanPham.getNhietDoMin() != null && sanPham.getNhietDoMax() != null
                && sanPham.getNhietDoMin().compareTo(sanPham.getNhietDoMax()) > 0) {
            ketQua.rejectValue("nhietDoMin", "khoangNhietDo",
                    "Nhiệt độ min phải nhỏ hơn hoặc bằng max");
        }

        // Còn lỗi thì quay lại form, giữ nguyên dữ liệu người dùng vừa gõ
        if (ketQua.hasErrors()) {
            model.addAttribute("dsDonViTinh", DON_VI_TINH);
            return "san-pham/form";
        }

        sanPhamService.luu(sanPham);
        ra.addFlashAttribute("thongBao",
                themMoi ? "Thêm sản phẩm thành công" : "Cập nhật sản phẩm thành công");
        return "redirect:/san-pham";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            sanPhamService.xoa(id);
            ra.addFlashAttribute("thongBao", "Đã xoá sản phẩm");
        } catch (IllegalStateException | IllegalArgumentException e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/san-pham";
    }

    @PostMapping("/doi-trang-thai/{id}")
    public String doiTrangThai(@PathVariable Long id, RedirectAttributes ra) {
        try {
            SanPham sanPham = sanPhamService.doiTrangThai(id);
            ra.addFlashAttribute("thongBao", sanPham.getTrangThai() == 1
                    ? "Đã mở bán lại sản phẩm " + sanPham.getMaSanPham()
                    : "Đã ngừng kinh doanh sản phẩm " + sanPham.getMaSanPham());
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/san-pham";
    }
}
