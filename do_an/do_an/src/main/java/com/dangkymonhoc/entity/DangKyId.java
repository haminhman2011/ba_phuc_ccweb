package com.dangkymonhoc.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Khóa chính của DANG_KY: (MaSV, MaMH, HocKy). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DangKyId implements Serializable {

    @Column(name = "MaSV", length = 10)
    private String maSV;

    @Column(name = "MaMH", length = 10)
    private String maMH;

    @Column(name = "HocKy", length = 10)
    private String hocKy;
}
