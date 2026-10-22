package com.dangkymonhoc.controller.sv;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dangkymonhoc.dto.TongKetHocTap;
import com.dangkymonhoc.entity.DangKy;
import com.dangkymonhoc.security.CustomUserDetails;
import com.dangkymonhoc.service.DangKyService;
import com.dangkymonhoc.service.HocKyService;
import com.dangkymonhoc.service.SinhVienService;

import lombok.RequiredArgsConstructor;

/** MH08 Kết quả học tập (UC07). */
@Controller
@RequiredArgsConstructor
public class KetQuaController {

    private final DangKyService dangKyService;
    private final HocKyService hocKyService;
    private final SinhVienService sinhVienService;

    /** hocKy rỗng hoặc không truyền = tất cả học kỳ. */
    @GetMapping("/sv/ket-qua")
    public String ketQua(@RequestParam(defaultValue = "") String hocKy,
                         @AuthenticationPrincipal CustomUserDetails user, Model model) {
        String hk = hocKy.isBlank() ? "" : hocKyService.chuanHoa(hocKy);
        List<DangKy> ds = dangKyService.ketQua(user.getMaSV(), hk);
        model.addAttribute("hocKy", hk);
        model.addAttribute("tatCaHocKy", hk.isEmpty());
        model.addAttribute("dsHocKy", hocKyService.sapXep(dangKyService.hocKyDaDangKy(user.getMaSV())));
        model.addAttribute("sinhVien", sinhVienService.layChiTiet(user.getMaSV()));
        model.addAttribute("nhoms", dangKyService.nhomTheoHocKy(ds));
        model.addAttribute("tongKet", TongKetHocTap.tinh(ds));
        return "sv/ket-qua";
    }
}
