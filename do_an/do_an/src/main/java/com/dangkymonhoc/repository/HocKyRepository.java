package com.dangkymonhoc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dangkymonhoc.entity.HocKy;

public interface HocKyRepository extends JpaRepository<HocKy, String> {

    @Query("select h from HocKy h order by h.ngayBatDau")
    List<HocKy> findAllTheoNgay();
}
