package com.dangkymonhoc.controller.gv;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dangkymonhoc.dto.KetQuaLuuDiem;
import com.dangkymonhoc.dto.NhapDiemForm;
import com.dangkymonhoc.dto.NhapDiemRow;
import com.dangkymonhoc.security.CustomUserDetails;
import com.dangkymonhoc.service.DiemService;
import com.dangkymonhoc.service.HocKyService;

import lombok.RequiredArgsConstructor;

/** MH11 Danh sách sinh viên và nhập điểm (UC08, UC09). */
@Controller
@RequestMapping("/gv/nhap-diem/{maMH}")
@RequiredArgsConstructor
public class NhapDiemController {

    private final DiemService diemService;
    private final HocKyService hocKyService;

    @GetMapping
    public String trang(@PathVariable String maMH, @RequestParam(required = false) String hocKy,
                        @AuthenticationPrincipal CustomUserDetails user, Model model) {
        String hk = hocKyService.chuanHoa(hocKy);
        kiemTraPhanCong(user, maMH, hk);
        napTrang(model, maMH, hk, diemService.bangDiem(user.getMaGV(), maMH, hk, null, null));
        return "gv/nhap-diem";
    }

    @PostMapping
    public String luu(@PathVariable String maMH, @RequestParam String hocKy,
                      @ModelAttribute NhapDiemForm form,
                      @AuthenticationPrincipal CustomUserDetails user, Model model, RedirectAttributes ra) {
        String hk = hocKyService.chuanHoa(hocKy);
        kiemTraPhanCong(user, maMH, hk);
        Map<String, String> nhap = form.toMap();
        KetQuaLuuDiem kq = diemService.luuDiem(user.getMaGV(), maMH, hk, nhap);
        if (!kq.hopLe()) {
            // Không lưu gì: hiển thị lại form với giá trị đã nhập và đánh dấu ô sai
            model.addAttribute("errorMessage",
                    "Có " + kq.oLoi().size() + " điểm không hợp lệ, vui lòng kiểm tra lại.");
            napTrang(model, maMH, hk, diemService.bangDiem(user.getMaGV(), maMH, hk, nhap, kq.oLoi()));
            return "gv/nhap-diem";
        }
        ra.addFlashAttribute("successMessage", "Đã lưu điểm cho " + kq.soDaLuu() + " sinh viên.");
        ra.addAttribute("hocKy", hk);
        return "redirect:/gv/nhap-diem/" + maMH;
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> xuatCsv(@PathVariable String maMH, @RequestParam(required = false) String hocKy,
                                          @AuthenticationPrincipal CustomUserDetails user) {
        String hk = hocKyService.chuanHoa(hocKy);
        kiemTraPhanCong(user, maMH, hk);
        String tenFile = "diem_" + maMH + "_" + hk + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(tenFile, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(diemService.xuatCsv(user.getMaGV(), maMH, hk));
    }

    /** QĐ7: chỉ giảng viên được phân công môn trong học kỳ mới xem/nhập điểm, ngược lại trả 403. */
    private void kiemTraPhanCong(CustomUserDetails user, String maMH, String hocKy) {
        if (!diemService.duocPhanCong(user.getMaGV(), maMH, hocKy)) {
            throw new AccessDeniedException("Bạn không được phân công dạy môn này.");
        }
    }

    private void napTrang(Model model, String maMH, String hocKy, List<NhapDiemRow> rows) {
        model.addAttribute("monHoc", diemService.layMonHoc(maMH));
        model.addAttribute("hocKy", hocKy);
        model.addAttribute("rows", rows);
        model.addAttribute("daNhap", rows.stream().filter(r -> r.getDiem() != null && !r.getDiem().isBlank()).count());
        model.addAttribute("oLoi", rows.stream().filter(NhapDiemRow::isLoi).map(NhapDiemRow::getMaSV).toList());
    }
}
