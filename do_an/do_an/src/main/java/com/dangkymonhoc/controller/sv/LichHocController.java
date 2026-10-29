package com.dangkymonhoc.controller.sv;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dangkymonhoc.dto.BuoiHocRow;
import com.dangkymonhoc.entity.HocKy;
import com.dangkymonhoc.security.CustomUserDetails;
import com.dangkymonhoc.service.DangKyService;
import com.dangkymonhoc.service.HocKyService;
import com.dangkymonhoc.service.LichHocService;

import lombok.RequiredArgsConstructor;

/**
 * MH21 Lịch học của sinh viên: lưới tuần chỉ đọc, chỉ hiện lịch chính thức. Mặc định xem tuần hiện tại với ngày
 * cụ thể; tuần 0 là thời khóa biểu chung cả học kỳ.
 */
@Controller
@RequiredArgsConstructor
public class LichHocController {

    private final LichHocService lichHocService;
    private final DangKyService dangKyService;
    private final HocKyService hocKyService;

    @GetMapping("/sv/lich-hoc")
    public String lichHoc(@RequestParam(required = false) String hocKy, @RequestParam(required = false) Integer tuan,
                          @AuthenticationPrincipal CustomUserDetails user, Model model) {
        String hk = hocKyService.chuanHoa(hocKy);
        HocKy thongTin = hocKyService.tim(hk).orElse(null);
        List<BuoiHocRow> tatCa = lichHocService.lichCuaSinhVien(user.getMaSV(), hk);
        int t = thongTin == null ? 0
                : tuan == null ? LichHocService.tuanMacDinh(thongTin) : Math.max(0, Math.min(tuan, thongTin.getSoTuan()));
        List<BuoiHocRow> buoi = LichHocService.locTheoTuan(tatCa, t);

        model.addAttribute("hocKy", hk);
        model.addAttribute("dsHocKy", hocKyService.sapXep(dangKyService.hocKyDaDangKy(user.getMaSV())));
        model.addAttribute("thongTinHocKy", thongTin);
        model.addAttribute("tuan", t);
        model.addAttribute("ngayThu", t > 0 ? LichHocService.ngayTrongTuan(thongTin, t) : null);
        model.addAttribute("cot", LichHocService.chiaCot(buoi));
        model.addAttribute("buoi", buoi);
        model.addAttribute("tatCa", tatCa);
        model.addAttribute("daCongBo", lichHocService.daCongBo(hk));
        return "sv/lich-hoc";
    }
}
