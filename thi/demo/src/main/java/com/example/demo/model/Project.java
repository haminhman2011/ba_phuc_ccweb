package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private Long id;
    private String name;
    private List<Staff> staffs = new ArrayList<>();

    public Project() {}

    public Project(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Staff> getStaffs() { return staffs; }
    public void setStaffs(List<Staff> staffs) { this.staffs = staffs; }
}