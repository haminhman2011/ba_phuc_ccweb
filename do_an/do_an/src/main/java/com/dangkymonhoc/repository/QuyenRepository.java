package com.dangkymonhoc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dangkymonhoc.entity.Quyen;

public interface QuyenRepository extends JpaRepository<Quyen, String> {

    @Query("select q from Quyen q order by q.maQuyen")
    List<Quyen> findAllSorted();
}
