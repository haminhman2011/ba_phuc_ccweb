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
@Table(name = "QUYEN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Quyen {

    public static final String ADMIN = "ADMIN";
    public static final String GV = "GV";
    public static final String SV = "SV";

    @Id
    @Column(name = "MaQuyen", length = 10)
    private String maQuyen;

    @Column(name = "TenQuyen", length = 50, nullable = false)
    private String tenQuyen;
}
