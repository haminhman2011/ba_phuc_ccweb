package com.dangkymonhoc.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Khóa chính của PHAN_CONG: (MaGV, MaMH, HocKy). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PhanCongId implements Serializable {

    @Column(name = "MaGV", length = 10)
    private String maGV;

    @Column(name = "MaMH", length = 10)
    private String maMH;

    @Column(name = "HocKy", length = 10)
    private String hocKy;
}
