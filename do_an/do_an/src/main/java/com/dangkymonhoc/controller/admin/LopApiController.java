package com.dangkymonhoc.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dangkymonhoc.dto.LopOption;
import com.dangkymonhoc.service.LopService;

import lombok.RequiredArgsConstructor;

/** Nạp lại select lớp theo khoa (bộ lọc và form sinh viên). */
@RestController
@RequiredArgsConstructor
public class LopApiController {

    private final LopService lopService;

    @GetMapping("/api/lop")
    public List<LopOption> lopTheoKhoa(@RequestParam(defaultValue = "") String maKhoa) {
        return lopService.theoKhoa(maKhoa);
    }
}
