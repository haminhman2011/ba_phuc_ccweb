package com.dangkymonhoc.controller.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dangkymonhoc.dto.BangXepLich;
import com.dangkymonhoc.dto.BuoiHocRow;
import com.dangkymonhoc.dto.KetQuaXepLich;
import com.dangkymonhoc.dto.LichHocForm;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.service.HocKyService;
import com.dangkymonhoc.service.LichHocService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** MH20 Xếp lịch dạy. */
@Controller
@RequestMapping("/admin/xep-lich")
@RequiredArgsConstructor
public class XepLichController {

    private final LichHocService lichHocService;
    private final HocKyService hocKyService;

    @GetMapping
    public String trang(@RequestParam(required = false) String hocKy, @RequestParam(defaultValue = "") String loc,
                        Model model) {
        String hk = hocKyService.chuanHoa(hocKy);
        LichHocForm form = new LichHocForm();
        form.setHocKy(hk);
        model.addAttribute("form", form);
        napTrang(model, hk, loc);
        return "admin/xep-lich";
    }

    @PostMapping("/tu-dong")
    public String tuDong(@RequestParam String hocKy, @RequestParam(defaultValue = "") String loc,
                         RedirectAttributes ra) {
        String hk = hocKyService.chuanHoa(hocKy);
        KetQuaXepLich kq = lichHocService.tuDongXep(hk);
        int soChuaXep = kq.chuaXep().size();
        if (kq.soBuoiXep() == 0 && soChuaXep == 0) {
            ra.addFlashAttribute("successMessage", "Mọi phân công đã có lịch, không có buổi nào cần xếp.");
        } else if (soChuaXep == 0) {
            ra.addFlashAttribute("successMessage", "Đã xếp " + kq.soBuoiXep() + " buổi học.");
        } else {
            ra.addFlashAttribute("errorMessage", "Đã xếp " + kq.soBuoiXep() + " buổi học. Còn " + soChuaXep
                    + " phân công chưa xếp được, xem mục \"Chưa xếp được\".");
        }
        return veTrang(ra, hk, loc);
    }

    @PostMapping("/xoa-nhap")
    public String xoaNhap(@RequestParam String hocKy, @RequestParam(defaultValue = "") String loc,
                          RedirectAttributes ra) {
        String hk = hocKyService.chuanHoa(hocKy);
        int soXoa = lichHocService.xoaBanNhap(hk);
        ra.addFlashAttribute("successMessage", "Đã xóa " + soXoa + " buổi nháp của học kỳ " + hk + ".");
        return veTrang(ra, hk, loc);
    }

    @PostMapping("/chinh-thuc")
    public String chinhThuc(@RequestParam String hocKy, @RequestParam(defaultValue = "") String loc,
                            RedirectAttributes ra) {
        String hk = hocKyService.chuanHoa(hocKy);
        try {
            int so = lichHocService.luuChinhThuc(hk);
            ra.addFlashAttribute("successMessage", "Đã lưu chính thức " + so + " buổi học. Sinh viên đã có thể "
                    + "xem lịch và đăng ký môn học kỳ " + hk + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return veTrang(ra, hk, loc);
    }

    @PostMapping("/luu")
    public String luu(@Valid @ModelAttribute("form") LichHocForm form, BindingResult result,
                      @RequestParam(defaultValue = "") String loc, Model model, RedirectAttributes ra) {
        String hk = hocKyService.chuanHoa(form.getHocKy());
        form.setHocKy(hk);
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", result.getAllErrors().get(0).getDefaultMessage());
        } else {
            try {
                ra.addFlashAttribute("successMessage", lichHocService.luuBuoi(form));
                return veTrang(ra, hk, loc);
            } catch (BusinessException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }
        // Vi phạm ràng buộc: giữ modal mở, hiện lý do cụ thể
        model.addAttribute("openModal", true);
        napTrang(model, hk, loc);
        return "admin/xep-lich";
    }

    private String veTrang(RedirectAttributes ra, String hocKy, String loc) {
        ra.addAttribute("hocKy", hocKy);
        if (!loc.isBlank()) {
            ra.addAttribute("loc", loc);
        }
        return "redirect:/admin/xep-lich";
    }

    private void napTrang(Model model, String hocKy, String loc) {
        BangXepLich bang = lichHocService.bangXepLich(hocKy);
        List<BuoiHocRow> hienThi = lichHocService.loc(bang.buoi(), loc);
        model.addAttribute("cot", LichHocService.chiaCot(hienThi));
        model.addAttribute("hocKy", hocKy);
        model.addAttribute("loc", loc);
        model.addAttribute("dsHocKy", hocKyService.tatCa());
        model.addAttribute("thongTinHocKy", hocKyService.lay(hocKy));
        model.addAttribute("bang", bang);
        model.addAttribute("choPhepXep", lichHocService.choPhepXep(hocKy));
        model.addAttribute("buoi", hienThi);
        model.addAttribute("giangViens", lichHocService.giangVienTrongHocKy(hocKy));
        model.addAttribute("mauGV", lichHocService.mauTheoGiangVien(hocKy));
        model.addAttribute("phongs", lichHocService.dsPhong());
    }
}
