package com.dangkymonhoc.controller.gv;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dangkymonhoc.dto.BuoiHocRow;
import com.dangkymonhoc.entity.HocKy;
import com.dangkymonhoc.security.CustomUserDetails;
import com.dangkymonhoc.service.DiemService;
import com.dangkymonhoc.service.HocKyService;
import com.dangkymonhoc.service.LichHocService;

import lombok.RequiredArgsConstructor;

/**
 * MH22 Lịch dạy của giảng viên: lưới tuần chỉ đọc, chỉ hiện lịch chính thức. Mặc định xem tuần hiện tại với ngày
 * cụ thể; tuần 0 là thời khóa biểu chung cả học kỳ.
 */
@Controller
@RequiredArgsConstructor
public class LichDayController {

    private final LichHocService lichHocService;
    private final DiemService diemService;
    private final HocKyService hocKyService;

    @GetMapping("/gv/lich-day")
    public String lichDay(@RequestParam(required = false) String hocKy, @RequestParam(required = false) Integer tuan,
                          @AuthenticationPrincipal CustomUserDetails user, Model model) {
        String hk = hocKyService.chuanHoa(hocKy);
        HocKy thongTin = hocKyService.tim(hk).orElse(null);
        List<BuoiHocRow> tatCa = lichHocService.lichCuaGiangVien(user.getMaGV(), hk);
        int t = thongTin == null ? 0
                : tuan == null ? LichHocService.tuanMacDinh(thongTin) : Math.max(0, Math.min(tuan, thongTin.getSoTuan()));
        List<BuoiHocRow> buoi = LichHocService.locTheoTuan(tatCa, t);

        model.addAttribute("hocKy", hk);
        model.addAttribute("dsHocKy", hocKyService.sapXep(diemService.hocKyCuaGiangVien(user.getMaGV())));
        model.addAttribute("thongTinHocKy", thongTin);
        model.addAttribute("tuan", t);
        model.addAttribute("ngayThu", t > 0 ? LichHocService.ngayTrongTuan(thongTin, t) : null);
        model.addAttribute("cot", LichHocService.chiaCot(buoi));
        model.addAttribute("buoi", buoi);
        model.addAttribute("tatCa", tatCa);
        model.addAttribute("daCongBo", lichHocService.daCongBo(hk));
        return "gv/lich-day";
    }
}
