package com.example.demo.model;

public class Staff {
    private Long id;
    private String name;
    private String email;
    private Long projectId;
    private String projectName;

    public Staff() {}

    public Staff(Long id, String name, String email, Long projectId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.projectId = projectId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public String getProjectName() { return projectName; }
	public void setProjectName(String projectName) { this.projectName = projectName; }
}