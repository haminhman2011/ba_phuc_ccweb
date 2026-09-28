package com.manha.lab0910.controller;

import com.manha.lab0910.form.DongBaoCaoHuy;
import com.manha.lab0910.form.DongBaoCaoTon;
import com.manha.lab0910.service.BaoCaoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/bao-cao")
public class BaoCaoController {

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BaoCaoService baoCaoService;

    public BaoCaoController(BaoCaoService baoCaoService) {
        this.baoCaoService = baoCaoService;
    }

    @GetMapping
    public String xemBaoCao(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false, defaultValue = "NXT") String loaiBaoCao,
            Model model) {

        // Lần đầu vào trang chưa chọn gì: mặc định lấy tháng hiện tại
        if (tuNgay == null && denNgay == null) {
            denNgay = LocalDate.now();
            tuNgay = denNgay.withDayOfMonth(1);
        }

        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);
        model.addAttribute("loaiBaoCao", loaiBaoCao);

        // Validate: phải nhập đủ và từ ngày không được sau đến ngày
        if (tuNgay == null || denNgay == null) {
            model.addAttribute("loi", "Vui lòng chọn đầy đủ từ ngày và đến ngày");
            return "bao-cao/index";
        }
        if (tuNgay.isAfter(denNgay)) {
            model.addAttribute("loi", "Khoảng thời gian không hợp lệ");
            return "bao-cao/index";
        }

        if ("HUY".equals(loaiBaoCao)) {
            model.addAttribute("dsHuy", baoCaoService.baoCaoHangHuy(tuNgay, denNgay));
        } else {
            model.addAttribute("dsTon", baoCaoService.baoCaoNhapXuatTon(tuNgay, denNgay));
        }

        return "bao-cao/index";
    }

    /**
     * Xuất file CSV (mở được bằng Excel).
     * Ghi thêm BOM ở đầu file, không có nó Excel đọc tiếng Việt ra ký tự lạ.
     */
    @GetMapping("/xuat-file")
    public void xuatFile(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(defaultValue = "NXT") String loaiBaoCao,
            HttpServletResponse response) throws IOException {

        String tenFile = ("HUY".equals(loaiBaoCao) ? "bao-cao-hang-huy-" : "bao-cao-nxt-")
                + tuNgay + "_" + denNgay + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + tenFile + "\"");

        PrintWriter out = response.getWriter();
        out.write('\ufeff');   // BOM cho Excel hiểu đây là UTF-8

        if ("HUY".equals(loaiBaoCao)) {
            out.println("Mã lô;Sản phẩm;Số lượng huỷ;Hạn dùng;Ngày huỷ;Lý do");
            List<DongBaoCaoHuy> ds = baoCaoService.baoCaoHangHuy(tuNgay, denNgay);
            for (DongBaoCaoHuy d : ds) {
                out.println(String.join(";",
                        boc(d.getMaLo()),
                        boc(d.getTenSanPham()),
                        String.valueOf(d.getSoLuongHuy()),
                        d.getHanSuDung() != null ? d.getHanSuDung().format(NGAY) : "",
                        d.getNgayHuy() != null ? d.getNgayHuy().toLocalDate().format(NGAY) : "",
                        boc(d.getLyDo())));
            }
        } else {
            out.println("Mã SP;Tên sản phẩm;ĐVT;Tồn đầu;Nhập trong kỳ;Xuất trong kỳ;Tồn cuối");
            List<DongBaoCaoTon> ds = baoCaoService.baoCaoNhapXuatTon(tuNgay, denNgay);
            for (DongBaoCaoTon d : ds) {
                out.println(String.join(";",
                        boc(d.getMaSanPham()),
                        boc(d.getTenSanPham()),
                        boc(d.getDonViTinh()),
                        String.valueOf(d.getTonDau()),
                        String.valueOf(d.getNhapTrongKy()),
                        String.valueOf(d.getXuatTrongKy()),
                        String.valueOf(d.getTonCuoi())));
            }
        }
        out.flush();
    }

    // Dữ liệu có dấu ; hoặc " sẽ làm lệch cột, phải bọc trong dấu nháy kép
    private String boc(String s) {
        if (s == null) {
            return "";
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
