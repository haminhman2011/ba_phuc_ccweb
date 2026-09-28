package com.manha.lab0910.controller;

import com.manha.lab0910.service.SanPhamService;
import com.manha.lab0910.service.TonKhoService;
import com.manha.lab0910.service.ViTriKhoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/ton-kho")
public class TonKhoController {

    private final TonKhoService tonKhoService;
    private final SanPhamService sanPhamService;
    private final ViTriKhoService viTriKhoService;

    public TonKhoController(TonKhoService tonKhoService,
                            SanPhamService sanPhamService,
                            ViTriKhoService viTriKhoService) {
        this.tonKhoService = tonKhoService;
        this.sanPhamService = sanPhamService;
        this.viTriKhoService = viTriKhoService;
    }

    @GetMapping
    public String xem(@RequestParam(required = false) Long sanPhamId,
                      @RequestParam(required = false) Long viTriId,
                      @RequestParam(required = false) String tinhTrang,
                      @RequestParam(required = false, defaultValue = "TONG_HOP") String cheDoXem,
                      Model model) {

        if ("CHI_TIET".equals(cheDoXem)) {
            model.addAttribute("dsChiTiet", tonKhoService.xemChiTiet(sanPhamId, viTriId, tinhTrang));
        } else {
            // Ở chế độ tổng hợp, lọc theo vị trí hay tình trạng hạn dùng không còn ý nghĩa
            // vì một sản phẩm gồm nhiều lô nằm ở nhiều vị trí với hạn dùng khác nhau.
            model.addAttribute("dsTongHop", tonKhoService.xemTongHop(sanPhamId));
        }

        model.addAttribute("dsSanPham", sanPhamService.timKiem(null));
        model.addAttribute("dsViTri", viTriKhoService.layTatCa());
        model.addAttribute("sanPhamId", sanPhamId);
        model.addAttribute("viTriId", viTriId);
        model.addAttribute("tinhTrang", tinhTrang);
        model.addAttribute("cheDoXem", cheDoXem);
        return "ton-kho/index";
    }
}
