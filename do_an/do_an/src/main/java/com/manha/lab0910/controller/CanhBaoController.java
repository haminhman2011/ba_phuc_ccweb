package com.manha.lab0910.controller;

import com.manha.lab0910.entity.CanhBao;
import com.manha.lab0910.service.CanhBaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/canh-bao")
public class CanhBaoController {

    private final CanhBaoService canhBaoService;

    public CanhBaoController(CanhBaoService canhBaoService) {
        this.canhBaoService = canhBaoService;
    }

    @GetMapping
    public String danhSach(@RequestParam(required = false) String loai,
                           @RequestParam(required = false) Integer daXuLy,
                           Model model) {

        CanhBao.LoaiCanhBao loaiLoc = null;
        if (loai != null && !loai.isBlank()) {
            try {
                loaiLoc = CanhBao.LoaiCanhBao.valueOf(loai);
            } catch (IllegalArgumentException e) {
                loaiLoc = null;   // gõ bậy trên URL thì coi như không lọc
            }
        }

        model.addAttribute("danhSach", canhBaoService.timKiem(loaiLoc, daXuLy));
        model.addAttribute("loai", loai);
        model.addAttribute("daXuLy", daXuLy);
        model.addAttribute("nguongNgay", canhBaoService.getNguongNgay());
        model.addAttribute("soChuaXuLy", canhBaoService.demChuaXuLy());
        return "canh-bao/danh-sach";
    }

    /**
     * Chạy quét ngay, không cần chờ tới giờ của job.
     * Chủ yếu để demo và để thử sau khi vừa nhập hàng mới.
     */
    @PostMapping("/quet")
    public String quetNgay(RedirectAttributes ra) {
        int soMoi = canhBaoService.quetVaSinhCanhBao();
        ra.addFlashAttribute("thongBao", soMoi > 0
                ? "Đã quét xong, sinh thêm " + soMoi + " cảnh báo mới"
                : "Đã quét xong, không có cảnh báo mới");
        return "redirect:/canh-bao";
    }

    @PostMapping("/xu-ly/{id}")
    public String danhDauDaXuLy(@PathVariable Long id, RedirectAttributes ra) {
        try {
            canhBaoService.danhDauDaXuLy(id);
            ra.addFlashAttribute("thongBao", "Đã đánh dấu cảnh báo là đã xử lý");
        } catch (IllegalStateException | IllegalArgumentException e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/canh-bao";
    }
}
