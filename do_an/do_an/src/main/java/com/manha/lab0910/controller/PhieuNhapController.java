package com.manha.lab0910.controller;

import com.manha.lab0910.entity.PhieuNhap;
import com.manha.lab0910.form.DongNhapForm;
import com.manha.lab0910.form.PhieuNhapForm;
import com.manha.lab0910.service.PhieuNhapService;
import com.manha.lab0910.service.SanPhamService;
import com.manha.lab0910.service.ViTriKhoService;
import com.manha.lab0910.service.NguoiDungChiTiet;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/phieu-nhap")
public class PhieuNhapController {

    private static final Logger log = LoggerFactory.getLogger(PhieuNhapController.class);

    private final PhieuNhapService phieuNhapService;
    private final SanPhamService sanPhamService;
    private final ViTriKhoService viTriKhoService;

    public PhieuNhapController(PhieuNhapService phieuNhapService,
                               SanPhamService sanPhamService,
                               ViTriKhoService viTriKhoService) {
        this.phieuNhapService = phieuNhapService;
        this.sanPhamService = sanPhamService;
        this.viTriKhoService = viTriKhoService;
    }

    // Ô nhập bỏ trống gửi lên là chuỗi rỗng "", đổi thành null cho gọn
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String form(Model model) {
        PhieuNhapForm form = new PhieuNhapForm();
        form.getChiTiet().add(new DongNhapForm());   // mở sẵn 1 dòng cho đỡ phải bấm thêm
        model.addAttribute("phieuNhapForm", form);
        napDuLieuChon(model);
        return "phieu-nhap/form";
    }

    @PostMapping("/luu")
    public String luu(@Valid @ModelAttribute("phieuNhapForm") PhieuNhapForm form,
                      BindingResult ketQua,
                      @AuthenticationPrincipal NguoiDungChiTiet nguoiDung,
                      Model model,
                      RedirectAttributes ra) {

        // Xoá các dòng rỗng hoàn toàn (người dùng bấm thêm dòng rồi bỏ trống)
        // trước khi kiểm tra, nếu không sẽ báo lỗi oan cho dòng họ không định nhập.
        // Chỉ làm được khi form chưa có lỗi ràng buộc nào ở dòng đó.

        // (4a) Phiếu phải có ít nhất một dòng hàng
        if (form.getChiTiet() == null || form.getChiTiet().isEmpty()) {
            ketQua.reject("khongCoDong", "Phiếu nhập phải có ít nhất một dòng hàng");
        }

        kiemTraTungDong(form, ketQua);

        if (ketQua.hasErrors()) {
            napDuLieuChon(model);
            return "phieu-nhap/form";
        }

        // (4c) Hàng đã hoặc sắp hết hạn: cảnh báo và bắt xác nhận lại, chưa lưu vội
        List<String> loHetHan = timLoHetHan(form);
        if (!loHetHan.isEmpty() && !form.isXacNhanHetHan()) {
            model.addAttribute("canhBaoHetHan",
                    "Hàng đã/sắp hết hạn — xác nhận vẫn nhập? (" + String.join(", ", loHetHan) + ")");
            napDuLieuChon(model);
            return "phieu-nhap/form";
        }

        try {
            PhieuNhap phieu = phieuNhapService.luuPhieuNhap(form, nguoiDung.getId());
            ra.addFlashAttribute("thongBao",
                    "Nhập kho thành công. Mã phiếu: " + phieu.getMaPhieu());
            return "redirect:/phieu-nhap";
        } catch (RuntimeException e) {
            // Lỗi ngoài dự tính (mất kết nối DB, đụng ràng buộc...).
            // Cả transaction đã rollback nên kho không dính dữ liệu dở.
            // Ghi log đầy đủ cho mình xem, còn người dùng chỉ thấy câu chung chung (mục 8 đặc tả).
            log.error("Lỗi khi lưu phiếu nhập", e);
            model.addAttribute("loiHeThong", "Đã xảy ra lỗi, vui lòng thử lại");
            napDuLieuChon(model);
            return "phieu-nhap/form";
        }
    }

    /** Kiểm tra các quy tắc liên quan tới từng dòng: mã lô trùng, ngày SX sau hạn dùng. */
    private void kiemTraTungDong(PhieuNhapForm form, BindingResult ketQua) {
        if (form.getChiTiet() == null) {
            return;
        }

        Set<String> maLoTrongPhieu = new HashSet<>();

        for (int i = 0; i < form.getChiTiet().size(); i++) {
            DongNhapForm dong = form.getChiTiet().get(i);
            if (dong == null) {
                continue;
            }

            // (4b) Mã lô đã có trong DB, hoặc bị gõ trùng hai lần trong cùng phiếu
            if (dong.getMaLo() != null && !dong.getMaLo().isBlank()) {
                String maLo = dong.getMaLo().trim();
                if (phieuNhapService.maLoDaTonTai(maLo)) {
                    ketQua.rejectValue("chiTiet[" + i + "].maLo", "trung", "Mã lô đã tồn tại");
                } else if (!maLoTrongPhieu.add(maLo.toLowerCase())) {
                    ketQua.rejectValue("chiTiet[" + i + "].maLo", "trungTrongPhieu",
                            "Mã lô bị trùng với dòng khác trong phiếu");
                }
            }

            // Id gửi lên phải trỏ tới bản ghi có thật, nếu không sẽ gãy khoá ngoại lúc lưu
            if (dong.getSanPhamId() != null && !sanPhamService.tonTai(dong.getSanPhamId())) {
                ketQua.rejectValue("chiTiet[" + i + "].sanPhamId", "khongTonTai",
                        "Sản phẩm không còn tồn tại, vui lòng chọn lại");
            }
            if (dong.getViTriId() != null && !viTriKhoService.tonTai(dong.getViTriId())) {
                ketQua.rejectValue("chiTiet[" + i + "].viTriId", "khongTonTai",
                        "Vị trí không còn tồn tại, vui lòng chọn lại");
            }

            // (4d) Ngày sản xuất không được sau hạn dùng
            if (dong.getNgaySanXuat() != null && dong.getHanSuDung() != null
                    && dong.getNgaySanXuat().isAfter(dong.getHanSuDung())) {
                ketQua.rejectValue("chiTiet[" + i + "].ngaySanXuat", "ngaySaiThuTu",
                        "Ngày sản xuất phải trước hạn dùng");
            }
        }
    }

    /** Danh sách mã lô có hạn dùng <= hôm nay, dùng cho cảnh báo 4c. */
    private List<String> timLoHetHan(PhieuNhapForm form) {
        LocalDate homNay = LocalDate.now();
        List<String> ketQua = new ArrayList<>();
        for (DongNhapForm dong : form.getChiTiet()) {
            if (dong != null && dong.getHanSuDung() != null && !dong.getHanSuDung().isAfter(homNay)) {
                ketQua.add(dong.getMaLo());
            }
        }
        return ketQua;
    }

    private void napDuLieuChon(Model model) {
        model.addAttribute("dsSanPham", sanPhamService.laySanPhamDangKinhDoanh());
        model.addAttribute("dsViTri", viTriKhoService.layTatCa());
        model.addAttribute("maPhieuDuKien", phieuNhapService.sinhMaPhieuMoi());
        model.addAttribute("homNay", LocalDate.now());
    }
}
