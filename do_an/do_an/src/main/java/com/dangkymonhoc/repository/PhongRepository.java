package com.dangkymonhoc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dangkymonhoc.entity.Phong;

public interface PhongRepository extends JpaRepository<Phong, String> {

    /** Phòng nhỏ trước để thuật toán xếp chọn phòng nhỏ nhất đủ sức chứa. */
    @Query("select p from Phong p order by p.sucChua, p.maPhong")
    List<Phong> findAllSorted();
}
