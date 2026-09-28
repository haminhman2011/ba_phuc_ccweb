package com.manha.lab0910.controller;

import com.manha.lab0910.entity.NguoiDung;
import com.manha.lab0910.form.NguoiDungForm;
import com.manha.lab0910.service.NguoiDungChiTiet;
import com.manha.lab0910.service.NguoiDungService;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/nguoi-dung")
public class NguoiDungController {

    private final NguoiDungService nguoiDungService;

    public NguoiDungController(NguoiDungService nguoiDungService) {
        this.nguoiDungService = nguoiDungService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String danhSach(Model model) {
        model.addAttribute("danhSach", nguoiDungService.layTatCa());
        return "nguoi-dung/danh-sach";
    }

    @GetMapping("/them")
    public String formThem(Model model) {
        model.addAttribute("nguoiDungForm", new NguoiDungForm());
        model.addAttribute("dsVaiTro", NguoiDung.VaiTro.values());
        return "nguoi-dung/form";
    }

    @GetMapping("/sua/{id}")
    public String formSua(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            NguoiDung nd = nguoiDungService.timTheoId(id);
            NguoiDungForm form = new NguoiDungForm();
            form.setId(nd.getId());
            form.setTenDangNhap(nd.getTenDangNhap());
            form.setHoTen(nd.getHoTen());
            form.setVaiTro(nd.getVaiTro());
            form.setTrangThai(nd.getTrangThai());
            // Cố ý không đổ mật khẩu ra form: trong DB chỉ có chuỗi BCrypt,
            // và cũng không nên hiển thị lại mật khẩu ở bất kỳ đâu.
            model.addAttribute("nguoiDungForm", form);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("loi", e.getMessage());
            return "redirect:/nguoi-dung";
        }
        model.addAttribute("dsVaiTro", NguoiDung.VaiTro.values());
        return "nguoi-dung/form";
    }

    @PostMapping("/luu")
    public String luu(@Valid @ModelAttribute("nguoiDungForm") NguoiDungForm form,
                      BindingResult ketQua,
                      @AuthenticationPrincipal NguoiDungChiTiet dangDangNhap,
                      Model model,
                      RedirectAttributes ra) {

        boolean themMoi = form.getId() == null;

        // Mật khẩu chỉ bắt buộc khi tạo mới; khi sửa để trống là giữ nguyên
        if (themMoi && (form.getMatKhau() == null || form.getMatKhau().isBlank())) {
            ketQua.rejectValue("matKhau", "batBuoc", "Vui lòng nhập mật khẩu");
        } else if (form.getMatKhau() != null && !form.getMatKhau().isBlank()
                && form.getMatKhau().length() < 6) {
            ketQua.rejectValue("matKhau", "quaNgan", "Mật khẩu phải từ 6 ký tự trở lên");
        }

        if (!ketQua.hasFieldErrors("tenDangNhap")
                && nguoiDungService.tenDangNhapDaTonTai(form.getTenDangNhap(), form.getId())) {
            ketQua.rejectValue("tenDangNhap", "trung", "Tên đăng nhập đã tồn tại");
        }

        // Không cho tự khoá tài khoản mình đang đăng nhập
        if (!themMoi && form.getId().equals(dangDangNhap.getId())
                && form.getTrangThai() != null && form.getTrangThai() == 0) {
            ketQua.rejectValue("trangThai", "tuKhoa", "Không thể khoá tài khoản của chính bạn");
        }

        if (ketQua.hasErrors()) {
            model.addAttribute("dsVaiTro", NguoiDung.VaiTro.values());
            return "nguoi-dung/form";
        }

        nguoiDungService.luu(form);
        ra.addFlashAttribute("thongBao",
                themMoi ? "Thêm tài khoản thành công" : "Cập nhật tài khoản thành công");
        return "redirect:/nguoi-dung";
    }

    @PostMapping("/doi-trang-thai/{id}")
    public String doiTrangThai(@PathVariable Long id,
                               @AuthenticationPrincipal NguoiDungChiTiet dangDangNhap,
                               RedirectAttributes ra) {
        if (id.equals(dangDangNhap.getId())) {
            ra.addFlashAttribute("loi", "Không thể khoá tài khoản của chính bạn");
            return "redirect:/nguoi-dung";
        }
        try {
            NguoiDung nd = nguoiDungService.doiTrangThai(id);
            ra.addFlashAttribute("thongBao", nd.getTrangThai() == 1
                    ? "Đã mở khoá tài khoản " + nd.getTenDangNhap()
                    : "Đã khoá tài khoản " + nd.getTenDangNhap());
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/nguoi-dung";
    }
}
