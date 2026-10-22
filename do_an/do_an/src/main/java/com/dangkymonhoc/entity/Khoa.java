package com.dangkymonhoc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "KHOA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Khoa {

    @Id
    @Column(name = "MaKhoa", length = 10)
    private String maKhoa;

    @Column(name = "TenKhoa", length = 100, nullable = false)
    private String tenKhoa;
}
