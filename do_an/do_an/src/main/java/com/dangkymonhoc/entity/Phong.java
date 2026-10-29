package com.dangkymonhoc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Phòng học dùng khi xếp lịch (MH20). */
@Entity
@Table(name = "PHONG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Phong {

    @Id
    @Column(name = "MaPhong", length = 10)
    private String maPhong;

    @Column(name = "TenPhong", length = 50, nullable = false)
    private String tenPhong;

    /** Sức chứa phải lớn hơn hoặc bằng số sinh viên đã đăng ký lớp học trong phòng. */
    @Column(name = "SucChua", nullable = false)
    private Integer sucChua;
}
