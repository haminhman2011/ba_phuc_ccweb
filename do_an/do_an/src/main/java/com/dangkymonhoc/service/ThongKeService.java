package com.dangkymonhoc.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.dto.TopMonRow;
import com.dangkymonhoc.entity.MonHoc;
import com.dangkymonhoc.repository.DangKyRepository;
import com.dangkymonhoc.repository.GiangVienRepository;
import com.dangkymonhoc.repository.MonHocRepository;
import com.dangkymonhoc.repository.PhanCongRepository;
import com.dangkymonhoc.repository.SinhVienRepository;

import lombok.RequiredArgsConstructor;

/** Số liệu cho trang tổng quan của quản trị viên (MH12). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThongKeService {

    private final SinhVienRepository sinhVienRepository;
    private final GiangVienRepository giangVienRepository;
    private final MonHocRepository monHocRepository;
    private final DangKyRepository dangKyRepository;
    private final PhanCongRepository phanCongRepository;

    public long soSinhVien() {
        return sinhVienRepository.count();
    }

    public long soGiangVien() {
        return giangVienRepository.count();
    }

    public long soMonHoc() {
        return monHocRepository.count();
    }

    public long soMonDuocMo(String hocKy) {
        return phanCongRepository.countMonDuocMo(hocKy);
    }

    public long soLuotDangKy(String hocKy) {
        return dangKyRepository.countByHocKy(hocKy);
    }

    public List<TopMonRow> topMon(String hocKy, int soLuong) {
        return dangKyRepository.topMon(hocKy, PageRequest.of(0, soLuong));
    }

    public List<MonHoc> monMoChuaCoDangKy(String hocKy) {
        return phanCongRepository.findMonMoChuaCoDangKy(hocKy);
    }
}
